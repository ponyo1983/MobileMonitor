package com.lon.mobilemonitor.core;

public class SignalNULL implements ISignal{

	@Override
	public SignalType getSignalType() {
		// TODO Auto-generated method stub
		return SignalType.SignalNULL;
	}

	@Override
	public float getACAmpl() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getDCAmpl() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getSignalInfo() {
		// TODO Auto-generated method stub
		return "Œﬁ–≈∫≈ ‰»Î";
	}

	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float[] getRawData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] getSpectrumData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SignalAmpl getAmpl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnit() {
		// TODO Auto-generated method stub
		return null;
	}

}
