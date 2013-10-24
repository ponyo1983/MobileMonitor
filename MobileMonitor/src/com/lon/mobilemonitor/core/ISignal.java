package com.lon.mobilemonitor.core;

public interface ISignal {

	public SignalType getSignalType();
	
	/*
	 * 获取交流幅度
	 */
	public float getACAmpl();
	/*
	 * 获取直流幅度
	 */
	public float getDCAmpl();
	
	public String getSignalInfo();
	
	public void copyTo(ISignal dest);
	
	public float[] getRawData();
	public float[] getSpectrumData();
	
}
