package com.lon.mobilemonitor.core;

import java.text.DecimalFormat;

import android.util.Log;

public class SignalSingle implements ISignal{

	private float acAmpl=0;
	private float dcAmpl=0;
	private float freq=0;
	
	private float[] rawData;
	private float[] spectrumData;
	
	public SignalSingle() {
		// TODO Auto-generated constructor stub
	}
	public SignalSingle(float freq,float acAmpl,float dcAmpl) {
		// TODO Auto-generated constructor stub
		this.freq=freq;
		this.acAmpl=acAmpl;
		this.dcAmpl=dcAmpl;
	}
	
	public float getFrequcy()
	{
		return freq;
	}
	public void putRawData(float[] data)
	{
		rawData=data;
	}
	
	public void putSpectrumData(float[] data)
	{
		spectrumData=data;
	}
	
	@Override
	public SignalType getSignalType() {
		// TODO Auto-generated method stub
		return SignalType.SignalSingle;
	}

	@Override
	public float getACAmpl() {
		// TODO Auto-generated method stub
		return acAmpl;
	}

	@Override
	public float getDCAmpl() {
		// TODO Auto-generated method stub
		return dcAmpl;
	}
	@Override
	public String getSignalInfo() {
		// TODO Auto-generated method stub
		
		
		StringBuilder sb=new StringBuilder();
		sb.append("µ¥Æµ--");
		DecimalFormat df=new DecimalFormat();
		df.applyPattern("0.000");
		sb.append("·ù¶È["+df.format(acAmpl)+ "]");
		df.applyPattern("0.00");
		sb.append("  ÆµÂÊ["+df.format(freq)+ "]");
		
		return sb.toString();
	}
	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub
		
		SignalSingle destSignal=(SignalSingle)dest;
		destSignal.acAmpl=acAmpl;
		destSignal.freq=freq;
		destSignal.dcAmpl=dcAmpl;
	
		destSignal.rawData=rawData;
		
		destSignal.spectrumData=spectrumData;
		
	}
	@Override
	public float[] getRawData() {
		// TODO Auto-generated method stub
		return rawData;
	}
	
	public float[] getSpectrumData()
	{
		return spectrumData;
	}
	
}
