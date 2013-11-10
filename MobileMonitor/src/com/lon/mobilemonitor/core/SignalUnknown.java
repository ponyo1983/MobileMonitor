package com.lon.mobilemonitor.core;

import java.text.DecimalFormat;

public class SignalUnknown implements ISignal {

	
	private float acAmpl=0;
	private float dcAmpl=0;

	private float[] rawData;
	private float[] spectrumData;
	
	private SignalAmpl signalAmpl;
	
	private String unit="";
	
	public SignalUnknown(){}
	
	public SignalUnknown(float ampl)
	{
		this.acAmpl=ampl;
	}
	
	public SignalUnknown(SignalAmpl signalAmpl,String unit)
	{
		this.signalAmpl=signalAmpl;
		this.unit=unit;
	}
	
	public void setDCAmpl(float ampl)
	{
		this.dcAmpl=ampl;
	}
	
	
	public void setACAmpl(float ampl)
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
		sb.append("未知信号--");
		DecimalFormat df=new DecimalFormat();
		df.applyPattern("0.000");
//		if(signalAmpl!=null && signalAmpl.getCount()>0)
//		{
//			int last=signalAmpl.getCount()-1;
//			SignalAmplPoint point=signalAmpl.getAmplPoint(last);
//			sb.append("幅度["+df.format(point.getAmpl())+ "]");
//		}
//		else {
//			sb.append("幅度["+df.format(acAmpl)+ "]");
//		}
		sb.append("直流幅度["+df.format(dcAmpl)+unit+ "]");
		sb.append("  交流幅度["+df.format(acAmpl)+unit+ "]");
	
		
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
		unkown.signalAmpl=signalAmpl;
		unkown.unit=unit;
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
	
	public SignalAmpl getAmpl()
	{
		return signalAmpl;
	}
	
	public String getUnit()
	{
		return unit;
	}


}
