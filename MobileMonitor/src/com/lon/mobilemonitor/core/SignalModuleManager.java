package com.lon.mobilemonitor.core;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;

public class SignalModuleManager {

	private List<SignalModule> modulesList = new ArrayList<SignalModule>();
//	static final String[] portNames = new String[] { "/dev/s3c2410_serial1",
//			"/dev/s3c2410_serial2", "/dev/s3c2410_serial3" };
	static final String[] portNames = new String[] { "/dev/ttyO2",
		"/dev/ttyO3", "/dev/ttyO4" };
	static final int Baudrate = 576000;

	private static SignalModuleManager singleton=null;
	
	private SignalModuleManager() {
		// TODO Auto-generated constructor stub
		for (int i = 0; i < portNames.length; i++) {
			SignalModule module=new SignalModule(portNames[i], Baudrate);
			module.run();
			modulesList.add(module);
		}
	}

	public static SignalModuleManager getInstance() {
		if(singleton==null)
		{
			singleton=new SignalModuleManager();
		}
		return singleton;
	}

	public SignalChannel getChannel(int num) {
		
		int modIndex=num/3;
		int chIndex=num%3;
		SignalModule module= getModule(modIndex);
		if(module!=null)
		{
			return module.getChannel(chIndex);
		}
		return null;
	}

	public SignalModule getModule(int num) {
		if(num>=0 && num<modulesList.size())
		{
			return modulesList.get(num);
		}
		return null;
	}
	
	public int getModuleNum() {
		return modulesList.size();
	}
	
	public void stop() {
		
		for(SignalModule module : modulesList)
		{
			module.stop();
		}
	}

}
