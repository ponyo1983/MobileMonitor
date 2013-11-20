package com.lon.mobilemonitor.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.lon.mobilemonitor.dsp.DSPUtil;
import com.lon.mobilemonitor.dsp.FFTPlan;
import com.lon.mobilemonitor.dsp.SignalUtil;

import android.R.bool;
import android.R.integer;
import android.util.Log;
import android.widget.ListView;

///信号通道
public class SignalChannel {

	private SignalModule module;
	private ArrayList<DataBlock> listDataBlocks = new ArrayList<DataBlock>(); // 数据块
	private ArrayList<WorkMode> listWorkModes = new ArrayList<WorkMode>(); // 工作模式列表

	private WorkMode currentWorkMode = null;// 当前工作模式

	private int channelNum = 0;

	private boolean needSpectrum=false;
	
	byte[] OneSampleFrame=new byte[16*1024]; //这个地方需要完善，假定采样率固定为8000,多余的表示模式
	
	int OneSampleLength=0;
	
	int prevSampleNum=-100;
	
	ArrayList<ISignal> listSignals = new ArrayList<ISignal>();

	ISignal currentSignal;

	ReentrantLock lock = new ReentrantLock();
	Condition condition = lock.newCondition();

	Thread threadCheck;
	Thread threadDSP;
	
	private List<ISignalChangedListener> signalChangedListeners=new ArrayList<ISignalChangedListener>();
	
	private List<IWorkModeChangedListener> workModeChangedListeners=new ArrayList<IWorkModeChangedListener>();

	/*
	 * @param SignalModule 信号模块
	 * 
	 * @param num 信号通道的编号 从0开始
	 */
	public SignalChannel(SignalModule module, int channelNum) {
		// TODO Auto-generated constructor stub
		this.module = module;
		this.channelNum = channelNum;
		listSignals.add(new SignalSingle()); // 单频信号
		listSignals.add(new SignalFSK());// 移频和UM71信号
		listSignals.add(new SignalUnknown());// 未知信号
		listSignals.add(new SignalNULL());// 小信号
	}

	public int getChannelNum() {
		return channelNum;
	}

	public WorkMode getCurrentMode() {
		return currentWorkMode;
	}

	public List<WorkMode> getModeList() {
		return listWorkModes;
	}

	public ISignal getCurrentSignal() {
		return currentSignal;
	}

	public void putFrame(byte[] frame) {

		lock.lock();
		try {
			condition.signal();
		} finally {
			lock.unlock();
		}

		int cmd = frame[7];
		switch (cmd) {
		case 0: // 所有工作模式
		{
			// 移除所有工作模式
			synchronized (listWorkModes) {
				listWorkModes.clear();

				byte num = frame[9]; // 工作模式个数
				for (int i = 0; i < num; i++) {
					int index = 10 + i * 77; //加入模块的ID号
					ByteBuffer byteBuffer = ByteBuffer.wrap(frame, index, 77);
					byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
					
					byte mode = byteBuffer.get();
					byte[] modID=new byte[10];
					//模块ID
					byteBuffer.get(modID);
					StringBuilder sbBuilder=new StringBuilder();
					for(int j=0;j<10;j++)
					{
						String string=Integer.toHexString(modID[j]&0xff);
						if(string.length()<2)
						{
							sbBuilder.append("0");
							sbBuilder.append(string);
						}
						else
						{
							sbBuilder.append(string);
						}
						
					}
					
					//模式描述
					byte[] desc=new byte[40];
					byteBuffer.get(desc);
					int byteCount=0;
					for(int j=0;j<40;j++)
					{
						if(desc[j]==0) break;
						byteCount++;
					}
					String descriptor=new String(desc, 0, byteCount, Charset.forName("GBK"));
					int sampleRate = byteBuffer.getInt();
					int transformNum = byteBuffer.getShort();
					int adMax = byteBuffer.getShort();
					int adMin = byteBuffer.getShort();
					float upper = byteBuffer.getFloat();
					float lower = byteBuffer.getFloat();

					int sLen = 0;
					for (int j = 0; j < 8; j++) {
						if (frame[index + 69 + j] == 0)
							break;
						sLen++;
					}
					String unit = new String(frame, index + 69, sLen);
					String strModID=sbBuilder.toString().toUpperCase();
					WorkMode workMode = new WorkMode(mode, sampleRate,
							transformNum, adMax, adMin, upper, lower, unit,descriptor,strModID);
					listWorkModes.add(workMode);
					Log.e("模块ID", strModID);
				}

			}

		}
			break;
		case 1: // 工作模式改变
		{
			synchronized (listWorkModes) {
				Log.e("data", "模式改变");
				int mode = frame[9] & 0xff;
				if (mode == 0xff) {
					currentWorkMode = null;
					
				} else {

					for (int i = 0; i < listWorkModes.size(); i++) {
						WorkMode wkMode = listWorkModes.get(i);
						if ((wkMode.getMode() & 0xff) == mode) {
							currentWorkMode = wkMode;
							break;
						}
					}
				}
				WorkModeChanged(currentWorkMode); //触发模式改变事件
				int sampleRate = 0;
				if (currentWorkMode != null) {
					sampleRate = currentWorkMode.getSampleRate();
				}
				for (DataBlock block : listDataBlocks) {
					block.setSampleRate(sampleRate);
				}
			}
		}
			break;
		case 2: // 当前工作模式
		{

		}
			break;
		case 3: // 采集数据
		{
			if (currentWorkMode != null) {
				int sampleNum = ((frame[9] & 0xff) << 0)
						+ ((frame[10] & 0xff) << 8)
						+ ((frame[11] & 0xff) << 16)
						+ ((frame[12] & 0xff) << 24);
				int dataLength = (frame[3] & 0xff) + ((frame[4] & 0xff) << 8);
				currentWorkMode.getModeInfo(OneSampleFrame, 8000*2); //获取模式信息
				parseOneSample(sampleNum,frame,17,dataLength-10);
				float[] realData = currentWorkMode.getRealData(frame, 17,
						dataLength - 10);

				synchronized (listDataBlocks) {

					for (DataBlock block : listDataBlocks) {
						block.putSampleData(realData, sampleNum);
					}
				}
			}
		}
			break;
		}

	}
	
	private void parseOneSample(int sampleNum,byte[] buffer,int offset,int length)
	{
		if(prevSampleNum+1!=sampleNum)
		{
			Log.e("MISS:"+(module.getModuleNum()*3+ channelNum), String.valueOf(sampleNum));
			
			OneSampleLength=0; //重置
		}
		prevSampleNum=sampleNum;
		for(int i=0;i<length;i++)
		{
			OneSampleFrame[OneSampleLength]=buffer[i+offset];
			OneSampleLength++;
			if(OneSampleLength>=8000*2) //找到一帧
			{
				FileManager.getInstance().putSampleData(module.getModuleNum()*3+channelNum, OneSampleFrame);
				OneSampleLength=0;
			}
		}
	}
	
	public void sendFrame(byte[] frame) {
		this.module.sendFrame(frame);
	}

	public void run() {
		if (threadCheck == null || threadCheck.isInterrupted()) {
			threadCheck = new Thread(new SelfCheck());
			threadCheck.start();
		}
		if (threadDSP == null || threadDSP.isInterrupted()) {
			threadDSP = new Thread(new SignalDSP());
			threadDSP.start();
		}
	}

	public void stop() {
		if (threadCheck != null && !threadCheck.isInterrupted()) {
			threadCheck.interrupt();
		}
		if (threadDSP != null && !threadDSP.isInterrupted()) {
			threadDSP.interrupt();
		}
	}

	public IDataBlock addDataBlock(int timeSegmnet, int timeLength) {

		if (timeSegmnet > timeLength) {
			timeSegmnet = timeLength;
		}
		DataBlock block = null;
		synchronized (listDataBlocks) {
			int sampleRate = 0;
			if (currentWorkMode != null) {
				sampleRate = currentWorkMode.getSampleRate();
			}
			block = new DataBlock(timeSegmnet, timeLength, sampleRate);

			listDataBlocks.add(block);
		}

		return block;
	}

	public void removeDataBlock(IDataBlock block) {

		if (DataBlock.class.isInstance(block)) {
			DataBlock iBlock = DataBlock.class.cast(block);
			synchronized (listDataBlocks) {
				listDataBlocks.remove(iBlock);
			}
		}
	}

	private void queryAllWorkMode() {
		byte[] frame = new byte[2];
		frame[0] = (byte) 0x80;
		frame[1] = (byte) channelNum;
		module.sendFrame(frame);

	}

	public void setWorkMode(byte mode) {
		byte[] frame = new byte[3];

		frame[0] = (byte)0x81;
		frame[1] = (byte) channelNum;
		frame[2] = mode;

		sendFrame(frame);
	}

	private void WorkModeChanged(WorkMode mode)
	{
		synchronized (workModeChangedListeners) {
			for(IWorkModeChangedListener listener:workModeChangedListeners)
			{
				listener.onWorkModeChanged(mode);
			}
		}
	}
	private void setSignal(ISignal signal) {
		if(signal==null)
		{
			currentSignal=null; 
			synchronized (signalChangedListeners) {
				for(ISignalChangedListener listener:signalChangedListeners)
				{
					listener.onSignalChanged(null);
				}
			}
			return;
		}
		SignalType signalType = signal.getSignalType();
		for (ISignal s : listSignals) {
			if (s.getSignalType() == signalType) {
				signal.copyTo(s);

				currentSignal = s;
				synchronized (signalChangedListeners) {
					for(ISignalChangedListener listener:signalChangedListeners)
					{
						listener.onSignalChanged(currentSignal);
					}
				}
				break;
			}
		}
	}
	
	public void addSignalChangedListener(ISignalChangedListener listener) {
		synchronized (signalChangedListeners) {
			signalChangedListeners.add(listener);
		}
		
	}
	
	public void removeSignalChangedListener(ISignalChangedListener listener) {
		synchronized (signalChangedListeners) {
			signalChangedListeners.remove(listener);
		}
	}
	
	public void addWorkModeChangedListener(IWorkModeChangedListener listener)
	{
		synchronized (workModeChangedListeners) {
			workModeChangedListeners.add(listener);
		}
	}
	
	public void remodeWorkModeChangedListner(IWorkModeChangedListener listener)
	{
		synchronized (workModeChangedListeners) {
			workModeChangedListeners.remove(listener);
		}
	}
	
	

	/*
	 * 自身参数检测
	 */
	class SelfCheck implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean alive = false;
			while (!Thread.currentThread().isInterrupted()) {

				lock.lock();
				try {
					alive = condition.await(2000, TimeUnit.MILLISECONDS);

				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
				if (!alive) // 模块移除
				{
					
					currentWorkMode = null;
					setSignal(null);
					listWorkModes.clear();
					queryAllWorkMode();
				} else {

					if (currentWorkMode == null) {
						if (channelNum == 0) {
							Log.e("data", "无模式");
						}
						boolean modeFind = false;
						synchronized (listWorkModes) {
							if (listWorkModes.size() > 0) {
								WorkMode mode = listWorkModes.get(0);
								setWorkMode(mode.getMode());
								modeFind = true;
								try {
									Thread.sleep(1000); // 给下位机1秒时间响应
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						if (modeFind == false) {
							queryAllWorkMode();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}

		}

	}

	
	private float getRealAmpl(float ampl)
	{
		if(currentWorkMode!=null)
		{
			float realAmpl=CalManager.getInstance().getRealAmpl(currentWorkMode.moduleID, channelNum, ampl);
			return realAmpl;
		}
		return ampl;
	}
	
	/*
	 * 采集信号的数字处理
	 */
	class SignalDSP implements Runnable {

		FFTPlan fftPlan;
		SignalUtil amplTool = new SignalUtil();
		DSPUtil util = new DSPUtil();
		float[] data1 = null;
		float[] data2 = null;
		float[] dataSpectrum=null;
		float[] peakVal = new float[5];
		int[] peakIndex = new int[5];

		float[] amplDense=new float[25];
		
		float[] dcacAmpl=new float[2];
		float[] amplTemp=new float[16*1024];
		@Override
		public void run() {
			// TODO Auto-generated method stub

			IDataBlock dataBlock = addDataBlock(1000, 1000);
			
			
			try {
				while (!Thread.currentThread().isInterrupted()) {
					float[] sampleData = dataBlock.getBlock(-1);
					SignalModuleManager manager=SignalModuleManager.getInstance();
					synchronized (manager) {
						
						
						long millisTime=System.currentTimeMillis(); //获取系统时间
						
						if (sampleData == null)
							continue;
						
						
						
						if (currentWorkMode == null)
							continue; // 不太可能
						
						int sampleRate = currentWorkMode.getSampleRate();
						if (sampleData.length != sampleRate)
							continue;
						//判断信号是否很小，很小就直接忽略
						float Upper=currentWorkMode.upper;
						float sampleMaxVal=Float.MIN_VALUE;
						for(int i=0;i<sampleData.length;i++)
						{
							if(sampleData[i]>sampleMaxVal)
							{
								sampleMaxVal=sampleData[i];
							}
						}
						if(sampleMaxVal<0.03f*Upper){
							SignalChannel.this.setSignal(new SignalNULL()); //无信号输入
							 continue;
							}
						
					
						//计算交直流幅度
						amplTool.calDCACAmpl(sampleData, 0, sampleRate, dcacAmpl);
						
						dcacAmpl[0]=getRealAmpl(dcacAmpl[0]);
						dcacAmpl[1]=getRealAmpl(dcacAmpl[1]);
						
						SignalAmpl signalAmplA=new SignalAmpl();
						//最多每秒计算25次,因为测量的最小的信号的频率是25Hz
						int amplCnt=sampleRate/25;
						boolean largeChanged=false;
						for(int i=0;i<25;i++)
						{
							amplDense[i]=amplTool.calAmpl(sampleData, i*amplCnt,
									amplCnt);
							amplDense[i]=getRealAmpl(amplDense[i]);
							//signalAmplA.addAmpl(amplDense[i], millisTime+i*40);
							if(i>0) //计算这1秒内的最大的变化的斜率
							{
								if(Math.abs(amplDense[i]-amplDense[i-1])>0.1f)
								{
									largeChanged=true;
								}
							}
						}
						if(largeChanged)
						{
							for(int i=0;i<25;i++)
							{
								signalAmplA.addAmpl(amplDense[i], millisTime+i*40);
							}
						}
						else
						{
							signalAmplA.addAmpl(amplDense[24], millisTime+24*40); //加入最后一个点
						}
						
						//每秒钟计算
//						float signalAmpl = amplTool.calAmpl(sampleData, 0,
//								sampleRate);
						float signalAmpl=amplDense[24];
						
						//看看幅度是不是很小
						
						if (fftPlan != null && fftPlan.getFFTNum() != sampleRate) {
							fftPlan = null;
							System.gc();
						}
						if (fftPlan == null) {
							fftPlan = new FFTPlan(sampleRate);
						}
						if (data1 != null && data1.length < sampleData.length * 2) {
							data1 = null;
							System.gc();
						}
						if (data2 != null && data2.length < sampleData.length * 2) {
							data2 = null;
							System.gc();
						}
						if(dataSpectrum!=null && dataSpectrum.length<sampleData.length/2)
						{
							dataSpectrum=null;
						}
						if (data1 == null) {
							data1 = new float[sampleData.length * 2];
						}
						if (data2 == null) {
							data2 = new float[sampleData.length * 2];
						}
						if(dataSpectrum==null)
						{
							dataSpectrum=new float[sampleData.length/2]; //实数的频谱是对称的
						}
						System.arraycopy(sampleData, 0, data1, 0, sampleData.length);
						fftPlan.realForward(data1, 0);
						
						//计算频谱
						if (needSpectrum) {
							int spectrumLen = sampleData.length / 2;
							for (int i = 0; i < spectrumLen; i++) {
								dataSpectrum[i] = (float) Math.sqrt(data1[i * 2]
										* data1[i * 2] + data1[i * 2 + 1]
										* data1[i * 2 + 1]);
							}
						}
						int ignoreLow = 5;
						util.findComplexPeaks(data1,amplTemp, ignoreLow, sampleRate / 2,
								peakVal, peakIndex); // 忽略直流信息

						if (peakIndex[0] < 0)
							continue; // 不存在极点

						boolean isSingle = false;
						if (peakIndex[1] > 0) {
							float rate = (float) Math.abs((peakVal[0] - peakVal[1])
									/ peakVal[0]);
							if (rate >0.98f) {
								isSingle = true;
							}
						}
						if (isSingle) {
//							SignalSingle signal = new SignalSingle(peakIndex[0]
//									+ ignoreLow, signalAmpl, 0);
							SignalSingle signal=new SignalSingle(peakIndex[0]+ignoreLow, signalAmplA,currentWorkMode.getUnit());
							signal.setDCAmpl(dcacAmpl[0]);
							signal.setACAmpl(dcacAmpl[1]);
							signal.putRawData(sampleData);
							signal.putSpectrumData(dataSpectrum);
							SignalChannel.this.setSignal(signal);
							Log.e("单频-Time", String.valueOf(System.currentTimeMillis()-millisTime));
							continue;
						}

						if (peakIndex[peakIndex.length - 1] < 0) //未知信号
						{
							SignalUnknown signal = new SignalUnknown(signalAmplA,currentWorkMode.getUnit());
							signal.setDCAmpl(dcacAmpl[0]);
							signal.setACAmpl(dcacAmpl[1]);
							signal.putRawData(sampleData);
							signal.putSpectrumData(dataSpectrum);
							SignalChannel.this.setSignal(signal);
							Log.e("Unknown-Time", String.valueOf(System.currentTimeMillis()-millisTime));
							continue; // 不是移频 和UM71信号
						}

						boolean matchYP = true;
						boolean matchUM71 = true;
						float freqShift = 0;
						int underSampleCount = 1;
						for (int i = 0; i < peakIndex.length; i++) {
							if ((peakIndex[i] + ignoreLow < 1600 - 200)
									|| (peakIndex[i] + ignoreLow > 2600 + 200)) {
								matchUM71 = false;
							}
							if ((peakIndex[i] + ignoreLow < 550 - 100)
									|| (peakIndex[i] + ignoreLow > 850 + 100)) {
								matchYP = false;
							}
						}
						if (matchYP) {
							freqShift = (peakIndex[0] + peakIndex[1] + 2 * ignoreLow) / 2f;
							underSampleCount = 30;
						}
						if (matchUM71) {
							freqShift = peakIndex[0] + ignoreLow - 40;
							underSampleCount = 40;
						}

						if (matchUM71 == false && matchYP == false) //未知信号
						{
							SignalUnknown signal = new SignalUnknown(signalAmplA,currentWorkMode.getUnit());
							signal.setDCAmpl(dcacAmpl[0]);
							signal.setACAmpl(dcacAmpl[1]);
							signal.putRawData(sampleData);
							signal.putSpectrumData(dataSpectrum);
							SignalChannel.this.setSignal(signal);
							Log.e("Unknown-Time", String.valueOf(System.currentTimeMillis()-millisTime));
							continue;
						}
						for (int i = 0; i < sampleData.length; i++) {
							data2[i * 2] = sampleData[i];
							data2[i * 2 + 1] = 0;
						}

						// 频谱搬移
						util.shiftSignal(data2, freqShift, sampleRate);
						//Thread.sleep(1);
						// 滤波
						util.complexLowFilter(data2, data1);
						//Thread.sleep(1);
						// 欠采样
						util.underSample(data1, underSampleCount);
						//Thread.sleep(1);
						// 频谱分析
						fftPlan.complexForward(data1, 0);
						//Thread.sleep(1);
						float freqCarrier = 0;
						float freqLow = 0;

						if (matchYP) {
							int signalLength = data1.length / 2;
							util.findComplexPeaks(data1,amplTemp, 0, signalLength / 2,
									peakVal, peakIndex);
							freqLow = Math.abs(peakIndex[0] - peakIndex[1]) * 1f
									/ underSampleCount;
							float indexLeft = (peakIndex[0] + peakIndex[1]) / 2f;

							util.findComplexPeaks(data1,amplTemp, signalLength / 2,
									signalLength, peakVal, peakIndex);
							float indexRight = signalLength / 2
									- (peakIndex[0] + peakIndex[1]) / 2f;

							freqCarrier = freqShift + (indexRight - indexLeft + 2)
									/ 2f / underSampleCount;
						}
						if (matchUM71) {
							util.findComplexPeaks(data1, peakVal, peakIndex);
							freqCarrier = freqShift + peakIndex[0] * 1f
									/ underSampleCount;
							freqLow = Math.abs(peakIndex[1] - peakIndex[2]) / 2f
									/ underSampleCount;
						}

						SignalFSK signalFSK = new SignalFSK(signalAmplA,
								freqCarrier, freqLow,currentWorkMode.getUnit());
						//signalFSK.setDCAmpl(dcacAmpl[0]);
						//signalFSK.setACAmpl(dcacAmpl[1]);
						signalFSK.putRawData(sampleData);
						signalFSK.putSpectrumData(dataSpectrum);
						SignalChannel.this.setSignal(signalFSK);
						
						
						
						Log.e("UM71-Time", String.valueOf(System.currentTimeMillis()-millisTime));
						
						
					}
					
					

				}
			}  finally {
				removeDataBlock(dataBlock);
			}

		}

	}

}
