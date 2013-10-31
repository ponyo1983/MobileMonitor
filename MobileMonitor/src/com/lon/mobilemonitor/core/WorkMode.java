package com.lon.mobilemonitor.core;

import android.R.string;

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
	
	
}
