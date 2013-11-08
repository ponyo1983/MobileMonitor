package com.lon.mobilemonitor.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;

import android.util.Log;


public class StoreFile {
	
	
	final int FileHeaderLength=1024;
	final String FileTag="MOBILE";
	final int Version=1; //�洢�ļ��汾��
	
	RandomAccessFile fileStream;
	String storeFileName="";
	long MaxFileSize=0; //����������ļ��ĳߴ�

	final int  SampleRate=8000; //�̶������� 8000
	
	
	long startIndex=FileHeaderLength; //�ļ���¼��ʼ����
	long endIndex=FileHeaderLength; //�ļ���¼
	
	
	public StoreFile(RandomAccessFile file,String fileName,long maxFileSize)
	{
		this.fileStream=file;
		this.storeFileName=fileName;
		this.MaxFileSize=maxFileSize;
		
		checkFileInfo();
	}
	
	private void checkFileInfo()
	{
		boolean headerOK=false;
		try {
			byte[] buffer = new byte[FileHeaderLength];
			if (fileStream.length() >= FileHeaderLength) {
				

				fileStream.seek(0);
				fileStream.read(buffer, 0, FileHeaderLength);
				
				String fileTag=new String(buffer, 0, 6, Charset.forName("US-ASCII"));
				
				if(fileTag.equals(FileTag)) //�ļ�TAGһ��
				{
					if(buffer[6]==Version) //�汾��һ��
					{
						int fileNameLength=0;
						for(int i=0;i<25;i++)
						{
							if(buffer[7+i]==0) break;
							fileNameLength++;
						}
						String fileName=new String(buffer, 7, fileNameLength, Charset.forName("US-ASCII"));
						if(fileName.equals(storeFileName)) //�ļ���һ��
						{
							
							headerOK=true;
							startIndex=0;
							for(int i=0;i<8;i++)
							{
								startIndex|=((long)(buffer[32+i]&0xff))<<(i*8);
							}
							
							endIndex=0;
							for(int i=0;i<8;i++)
							{
								endIndex|=((long)(buffer[40+i]&0xff))<<(i*8);
							}
							
							Log.e("����", String.valueOf(endIndex));
							
							
						}
					}
				}
				
				
			}
			if(headerOK==false) //�ؽ��ļ�ͷ��Ϣ
			{
				Arrays.fill(buffer, (byte)0);
				//file TAG
				byte[] ascii=FileTag.getBytes(Charset.forName("US-ASCII"));
				for(int i=0;i<6;i++)
				{
					buffer[i]=ascii[i];
				}
				//Version
				buffer[6]=Version;
				//store file Name
				ascii=storeFileName.getBytes(Charset.forName("US-ASCII"));
				for(int i=0;i<ascii.length;i++)
				{
					buffer[7+i]=ascii[i];
				}
				//��ʼλ��
				long index=FileHeaderLength;
				for(int i=0;i<8;i++)
				{
					buffer[32+i]=(byte)((index>>(i*8))&0xff);
				}
				
				//��ֹλ��
				for(int i=0;i<8;i++)
				{
					buffer[40+i]=(byte)((index>>(i*8))&0xff);
				}
				//������
				buffer[48]=(byte)(SampleRate&0xff);
				buffer[49]=(byte)((SampleRate>>8)&0xff);
				buffer[50]=2;
				fileStream.seek(0);
				fileStream.write(buffer);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/*
	 * ÿ��д��16KB������
	 */
	public void writeData(byte[] data)
	{
		int writeSize=16*1024;
		if(data==null || data.length<writeSize) return;
		
		byte[] buffer=new byte[8];
				
		
		try {
			
			if(endIndex+writeSize<MaxFileSize) //δ�����ļ��������
			{
				
				if(endIndex>=startIndex)
				{
					fileStream.seek(endIndex);
					fileStream.write(data, 0, writeSize);
					
					endIndex+=writeSize;
					
					//д�뵽�ļ�ͷ
					fileStream.seek(40);
					
					for(int i=0;i<8;i++)
					{
						buffer[i]=(byte)((endIndex>>(i*8))&0xff);
					}
					fileStream.write(buffer, 0, 8);
					
					fileStream.getFD().sync(); //flush�ļ���U��
					
				}
				else {
					
					fileStream.seek(endIndex);
					fileStream.write(data, 0, writeSize);
					
					
					
					endIndex+=writeSize;
					
					if(startIndex+writeSize>=fileStream.length())
					{
						startIndex=FileHeaderLength;
					}
					else {
						startIndex+=writeSize;
					}
					
					//д�뵽�ļ�ͷ
					fileStream.seek(32);
					
					for(int i=0;i<8;i++)
					{
						buffer[i]=(byte)((startIndex>>(i*8))&0xff);
					}
					fileStream.write(buffer, 0, 8);
					
					for(int i=0;i<8;i++)
					{
						buffer[i]=(byte)((endIndex>>(i*8))&0xff);
					}
					fileStream.write(buffer, 0, 8);
					
					fileStream.getFD().sync(); //flush�ļ���U��
					
				}
				
				
				
			}
			else {
				endIndex=1024;
				if(endIndex==startIndex)
				{
					startIndex+=writeSize*2;
				}
				fileStream.seek(endIndex);
				fileStream.write(data, 0, writeSize);
				endIndex+=writeSize;
				
				//д�뵽�ļ�ͷ
				fileStream.seek(32);
				
				for(int i=0;i<8;i++)
				{
					buffer[i]=(byte)((startIndex>>(i*8))&0xff);
				}
				fileStream.write(buffer, 0, 8);
				
				for(int i=0;i<8;i++)
				{
					buffer[i]=(byte)((endIndex>>(i*8))&0xff);
				}
				fileStream.write(buffer, 0, 8);
				
				fileStream.getFD().sync(); //flush�ļ���U��
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
	
	
	
}
