package com.lon.mobilemonitor.core;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;

/*
 * 含有多个时间点的信号幅度
 */
public class SignalAmpl {

	List<SignalAmplPoint> listAmpl=new ArrayList<SignalAmplPoint>();
	
	public SignalAmpl()
	{
		
	}
	
	public SignalAmpl(float ampl,long millisTime)
	{
		listAmpl.add(new SignalAmplPoint(ampl, millisTime));
	}
	
	public void addAmpl(float ampl,long millisTime)
	{
		listAmpl.add(new SignalAmplPoint(ampl, millisTime));
	}
	
	public int getCount()
	{
		return listAmpl.size();
	}
	
	public SignalAmplPoint getAmplPoint(int index) {
		
		return listAmpl.get(index);
	}
	
	
	
	
	
}
