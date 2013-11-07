package com.lon.mobilemonitor.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.os.StatFs;
import android.util.Log;




/*
 * �ļ�������
 */
public class FileManager {

	
	final String StoreDir="/mnt/usb2";
	
	final String[] StoreFiles=new String[]{
			"m0c0.dat","m0c1.dat","m0c2.dat",
			"m1c0.dat","m1c1.dat","m1c2.dat",
			"m2c0.dat","m2c1.dat","m2c2.dat"};
	
	final StatFs stat =new StatFs(StoreDir);
	
	RandomAccessFile[] fileStreams=new RandomAccessFile[9];
	StoreFile[] storeFiles=new StoreFile[9];
	
	Thread storeThread; //
	Thread checkThread; //���U���Ƿ����
	
	
	static FileManager manager=null;
	
	
	LinkedList<ChannelData> listStoreDatas=new LinkedList<ChannelData>();
	ReentrantLock lock = new ReentrantLock();
	Condition condition = lock.newCondition();
	
	
	private FileManager()
	{}
	
	public static FileManager getInstance()
	{
		if(manager==null)
		{
			manager=new FileManager();
		}
		return manager;
	}
	
	
	public void putSampleData(int channelNum,byte[] data)
	{
		ChannelData channelData=new ChannelData(channelNum, data);
		
		lock.lock();
		try {
			if (listStoreDatas.size() > 100) {
				listStoreDatas.removeFirst();
			}
			listStoreDatas.addLast(channelData);
			condition.signal();
		} finally {
			lock.unlock();
		}
		
	}
	
	
	public ChannelData getSampleData(int timeout)
	{
		ChannelData frame = null;
		lock.lock();
		try {

			if ( listStoreDatas.size() > 0) {
				frame = listStoreDatas.removeFirst();
			}
			if (frame == null && timeout != 0) {
				try {
					if (timeout > 0) {

						condition.await(timeout, TimeUnit.MILLISECONDS);

					} else {
						condition.await();
					}
				} catch (InterruptedException e) {
					// TODO: handle exception
				}

			}
			if (listStoreDatas.size() > 0) {
				frame = listStoreDatas.removeFirst();
			}
		} finally {
			lock.unlock();
		}
		return frame;
	}
	
	
	private boolean usbExist()
	{
		boolean exist=false;
		File Usbfile = new File("/proc/partitions");

		if (Usbfile.exists()) {
			try {
				FileReader file = new FileReader(Usbfile);
				BufferedReader br = new BufferedReader(file);
				String strLine = "";
				while ((strLine = br.readLine()) != null) {
					if (strLine.indexOf("sd") > 0) {
						// searchFile("usb",Ufile);
						exist=true;
						break;
					}
				}
				br.close();
				file.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return exist;
	}
	
	
	private void storeData()
	{
		//ɾ�������ļ�
		deleteFile(new File(StoreDir));
		//���������ļ��ߴ�
		 long blockSize = stat.getBlockSize();
		 long totalBlocks = stat.getBlockCount();
		 long mTotalSize = (long)(totalBlocks * blockSize*(1-0.05f)); //��5%������
		 
		 
		 
		 for(int i=0;i<StoreFiles.length;i++)
		 {
			 File file=new File(StoreDir, StoreFiles[i]);
			 if(file.exists()==false)
			 {
				 try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 try {
				fileStreams[i]=new RandomAccessFile(file, "rw");
				Log.e("�洢�߳�", "�½��ļ�");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 storeFiles[i]=new StoreFile(fileStreams[i], StoreFiles[i], mTotalSize/9);
		 }
		 
		 
		 while(Thread.currentThread().isInterrupted()==false)
		 {
			 try {
				ChannelData channelData= getSampleData(-1);
				if(usbExist()==false)
				{
					for(int i=0;i<9;i++)
					{
						if (fileStreams[i] != null) {
							fileStreams[i].close();
							fileStreams[i] = null;
						}
					}
					break;
				}
				if(channelData!=null)
				{
					int channel=channelData.channel;
					if(channel>=0&&channel<10)
					{
						storeFiles[channel].writeData(channelData.data);
					}
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		try {
			for (int i = 0; i < 9; i++) {
				if (fileStreams[i] != null) {
					fileStreams[i].close();
					fileStreams[i] = null;
				}
			}
		} catch (IOException e) {
			// TODO: handle exception
		}
		
	}
	
	
	private void checkUSB()
	{
		if(usbExist())
		{
			if(storeThread==null || storeThread.isAlive()==false)
			{
				try {
					Thread.sleep(5000); //�ȴ�usb�ȶ�
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				storeThread=new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						storeData();
					}
				});
				storeThread.start();
			}
		}
		else
		{
			if(storeThread!=null && storeThread.isAlive())
			{
				storeThread.interrupt();
				Log.e("usbCheck", "��ֹ�߳�");
			}
		}
	}
	
	public void deleteFile(File file) {

		if (file.exists()) { // �ж��ļ��Ƿ����
			if (file.isFile()) { // �ж��Ƿ����ļ�
				String fileName=file.getName();
				boolean delete=true;
				for(int i=0;i<StoreFiles.length;i++)
				{
					if(fileName.equals(StoreFiles[i])) 
					{
						delete=false;
						break;
					}
				}
				if(fileName.equals("MobileMonitor.apk")) 
				{
					delete=false;	
				}
				if(delete)
				{
					file.getAbsoluteFile().delete();
				}
			} else if (file.isDirectory()) { // �����������һ��Ŀ¼
				File files[] = file.listFiles(); // ����Ŀ¼�����е��ļ� files[];
				if (files != null) {
					for (int i = 0; i < files.length; i++) { // ����Ŀ¼�����е��ļ�
						this.deleteFile(files[i]); // ��ÿ���ļ� ������������е���
					}
				}
				file.getAbsoluteFile().delete();
			}
			
		} else {
			//
		}
	}
	
	public void start()
	{
		checkThread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(true)
				{
					try {
						checkUSB();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
		
		checkThread.start();
	}
}

class ChannelData
{
	int channel;
	byte[] data;
	public ChannelData(int channel,byte[] data)
	{
		this.channel=channel;
		if(data!=null && data.length>0)
		{
			this.data=new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
		}
	}
}