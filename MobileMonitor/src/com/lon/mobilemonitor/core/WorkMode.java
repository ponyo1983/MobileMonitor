package com.lon.mobilemonitor.core;

import java.nio.charset.Charset;

///信号通道的工作模式
public class WorkMode {
	private byte mode=-1; //无效模式
	private int sampleRate=0;
	private int transferCount=0;
	int adMax=0;
	int adMin=0;
	float upper=0;
	float lower=0;
	String unit="";
	String descriptor=""; //工作模式的描述
	public WorkMode(byte mode, int sampleRate, int transferCount, int adMax,
			int adMin, float upper, float lower, String unit,String descriptor) {
		// TODO Auto-generated constructor stub
		this.mode=mode;
		this.sampleRate=sampleRate;
		this.transferCount=transferCount;
		this.adMax=adMax;
		this.adMin=adMin;
		this.upper=upper;
		this.lower=lower;
		this.unit=unit;
		this.descriptor=descriptor;
		
	}
	
	public byte getMode() {
		return mode;
	}
	
	public int  getSampleRate() {
		return sampleRate;
	}
	public int getTranferCount() {
		return transferCount;
	}
	
	public float getUpper()
	{
		return upper;
	}
	
	public float getLower()
	{
		return lower;
	}
	
	public String getUnit()
	{
		return unit;
	}
	
	public String getDescriptor() {
		return descriptor;
	}
	
	public float[] getRealData(byte[] sampleData,int offset,int sLength) {
		int length = sLength / 2;
        float[] data = new float[length];
        for (int i = 0; i < length; i++)
        {
            int sample = (sampleData[offset + i * 2]&0xff) + ((sampleData[offset + i * 2 + 1]&0xff) << 8);
            data[i] = lower + (sample - adMin) *(upper-lower)/ (adMax - adMin);
        }

        return data;
	}
	
	public void getModeInfo(byte[] buffer,int offset)
	{
		buffer[offset]=mode;
		
		byte[] gbk=descriptor.getBytes(Charset.forName("GBK"));
		//模式描述
		for(int i=0;i<40;i++)
		{
			if(i<gbk.length)
			{
				buffer[offset+1+i]=gbk[i];
			}
			else {
				buffer[offset+1+i]=0;
			}
		}
		//采样率
		for(int i=0;i<4;i++)
		{
			buffer[41+i+offset]=(byte)(sampleRate>>(i*8)&0xff);
		}
		//每次上传次数
		buffer[45+offset]=(byte)(transferCount&0xff);
		buffer[46+offset]=(byte)((transferCount>>8)&0xff);
		
		buffer[47+offset]=(byte)(adMax&0xff);
		buffer[48+offset]=(byte)((adMax>>8)&0xff);
		
		buffer[49+offset]=(byte)(adMin&0xff);
		buffer[50+offset]=(byte)((adMin>>8)&0xff);
		
		
		int intUpper=Float.floatToIntBits(upper);
		for(int i=0;i<4;i++)
		{
			buffer[51+i+offset]=(byte)(intUpper>>(i*8)&0xff);
		}
		int intLower=Float.floatToIntBits(lower);
		for(int i=0;i<4;i++)
		{
			buffer[55+i+offset]=(byte)(intLower>>(i*8)&0xff);
		}
		
		byte[] ascii=unit.getBytes(Charset.forName("US-ASCII"));
		for(int i=0;i<8;i++)
		{
			if(i<ascii.length)
			{
				buffer[59+offset]=ascii[i];
			}
			else
			{
				buffer[59+offset]=0;
			}
		}
		
	}
	
	
}
