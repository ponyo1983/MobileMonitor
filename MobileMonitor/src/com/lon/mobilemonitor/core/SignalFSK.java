package com.lon.mobilemonitor.core;

import java.text.DecimalFormat;

public class SignalFSK implements ISignal {

	
	
	float acAmpl=0;
	float dcAmpl=0;
	float freqCarrier=0; //ÔØÆµ
	float freqLower=0; //µÍÆµ
	
	float[] rawData=null;
	float[] spectrumData=null;
	public SignalFSK() {
		// TODO Auto-generated constructor stub
	}
	
	public SignalFSK(float acAmpl,float carrier,float lower) {
		// TODO Auto-generated constructor stub
		this.acAmpl=acAmpl;
		this.freqCarrier=carrier;
		this.freqLower=lower;
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
		return SignalType.SignalFSK;
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
		if(freqCarrier>1700-50 && freqCarrier< 2600+50)
		{
			sb.append("UM71--");
		}
		else if(freqCarrier>550-50 && freqCarrier< 850+50)
		{
			sb.append("¹ú²úÒÆÆµ--");
		}
		DecimalFormat df=new DecimalFormat();
		df.applyPattern("0.000");
		sb.append("·ù¶È["+df.format(acAmpl)+ "]");
		df.applyPattern("0.00");
		sb.append("  ÔØÆµ["+df.format(freqCarrier)+ "]");
		sb.append("  µÍÆµ["+df.format(freqLower)+ "]");
		return sb.toString();
	}


	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub
		
		SignalFSK fsk=(SignalFSK)dest;
		fsk.acAmpl=acAmpl;
		fsk.dcAmpl=dcAmpl;
		fsk.freqCarrier=freqCarrier;
		fsk.freqLower=freqLower;
		fsk.rawData=rawData;
		fsk.spectrumData=spectrumData;
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
