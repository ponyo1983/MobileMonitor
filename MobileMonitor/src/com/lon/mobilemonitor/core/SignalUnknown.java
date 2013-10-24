package com.lon.mobilemonitor.core;

import java.text.DecimalFormat;

public class SignalUnknown implements ISignal {

	
	private float acAmpl=0;
	private float dcAmpl=0;

	private float[] rawData;
	private float[] spectrumData;
	
	public SignalUnknown(){}
	
	public SignalUnknown(float ampl)
	{
		this.acAmpl=ampl;
	}
	
	
	public void putRawData(float[] data)
	{
		this.rawData=data;
	}
	
	public void putSpectrumData(float[] data)
	{
		this.spectrumData=data;
	}
	
	
	
	@Override
	public SignalType getSignalType() {
		// TODO Auto-generated method stub
		return SignalType.SignalUnknown;
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
		sb.append("Î´ÖªÐÅºÅ--");
		DecimalFormat df=new DecimalFormat();
		df.applyPattern("0.000");
		sb.append("·ù¶È["+df.format(acAmpl)+ "]");
		
		return sb.toString();
	}

	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub
		SignalUnknown unkown=(SignalUnknown)dest;
		unkown.acAmpl=acAmpl;
		unkown.dcAmpl=dcAmpl;
		unkown.rawData=rawData;
		unkown.spectrumData=spectrumData;
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
