package com.lon.mobilemonitor.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import android_serialport_api.SerialPort;

///采集模块
public class SignalModule {

	private SerialPort serialPort;
	OutputStream serialOutputStream=null;
	private FrameManager frameManager;

	private Thread threadRcv; // 查询参数的读取
	private ChannelCollection channels; // 信号通道

	public SignalModule(String portName, int baudrate) {
		// TODO Auto-generated constructor stub
		try {
			this.serialPort = new SerialPort(new File(portName), baudrate, 0);
			this.serialOutputStream=serialPort.getOutputStream();
			frameManager = new FrameManager(serialPort);

			channels = new ChannelCollection(this, 3);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public SignalChannel getChannel(int index) {
		
		return channels.getChannel(index);
	}
	
	class FrameRcv implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			IFrameFilter filter = frameManager.createFilter();
			try {
				while (Thread.currentThread().isInterrupted()==false) {
					byte[] frame = filter.getFrame(-1);

					if (frame == null)
						continue;

					channels.processFrame(frame);
				}

			} finally {
				frameManager.removeFilter(filter);
			}

		}

	}

	public void run()
	{
		if(threadRcv==null || threadRcv.isInterrupted())
		{
			threadRcv=new Thread(new FrameRcv());
			threadRcv.start();
		}
		//开启通道
		channels.run();
	}
	public void stop()
	{
		if(threadRcv!=null && threadRcv.isInterrupted()==false)
		{
			threadRcv.interrupt();
		}
		//停止通道
		channels.stop();
	}
	
	public void sendFrame(byte[] frame) {
		if(serialOutputStream==null)return;
        int length = 10 + frame.length;
        byte[] totalFrame = new byte[length];
        //帧头
        totalFrame[0] = (byte)0xaa;
        totalFrame[1] = (byte)0xaa;
        //版本 1
        totalFrame[2] = 0x01;
        //数据长度
        totalFrame[3] = (byte)(frame.length & 0xff);
        totalFrame[4] = (byte)((frame.length >> 8) & 0xff);
        //长度的反码
        totalFrame[5] = (byte)((~totalFrame[3]) & 0xff);
        totalFrame[6] = (byte)((~totalFrame[4]) & 0xff);
        //数据
        int checkSum = 0;
        for (int i = 0; i < frame.length; i++)
        {
            totalFrame[7 + i] = frame[i];
            checkSum += frame[i];
        }
        totalFrame[7 + frame.length] = (byte)(checkSum & 0xff);
        totalFrame[8 + frame.length] = (byte)'\r';
        totalFrame[9 + frame.length] = (byte)'\n';

        try {
			serialOutputStream.write(totalFrame);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ChannelCollection {

	ArrayList<SignalChannel> listChannels = new ArrayList<SignalChannel>();

	public ChannelCollection(SignalModule module, int channelNum) {
		// TODO Auto-generated constructor stub
		for (int i = 0; i < channelNum; i++) {
			listChannels.add(new SignalChannel(module, i));
		}
	}
	
	public SignalChannel getChannel(int index) {
		if(index>=0 && index<listChannels.size())
		{
			return listChannels.get(index);
		}
		return null;
	}

	public void processFrame(byte[] frame) {
		byte cmd = frame[7];

		switch (cmd) {
		case 0:
		case 1:
		case 2:
		case 3: {
			byte channel = frame[8];
			if (channel >= 0 && channel < listChannels.size()) {
				 listChannels.get(channel).putFrame(frame);
			}
		}
			break;
		}

	}

	public void run() {
		for(SignalChannel channel:listChannels)
		{
			channel.run();
		}
	}

	public void stop() {
		for(SignalChannel channel:listChannels)
		{
			channel.stop();
		}
	}

}
