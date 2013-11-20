package com.lon.mobilemonitor.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;

public class SignalModuleManager {

	private List<SignalModule> modulesList = new ArrayList<SignalModule>();
	static final String[] portNames1 = new String[] { "/dev/s3c2410_serial1",
			"/dev/s3c2410_serial2", "/dev/s3c2410_serial3" };
	static final String[] portNames2 = new String[] { "/dev/ttyO2",
		"/dev/ttyO3", "/dev/ttyO4" };
	static final int Baudrate = 576000;

	private static SignalModuleManager singleton=null;
	
	private SignalModuleManager() {
		// TODO Auto-generated constructor stub
		//加入串口设备是否存在的自动 判断
		
		File serial1=new File(portNames1[0]);
		File serial2=new File(portNames2[0]);
		if(serial1.exists())
		{
			for (int i = 0; i < portNames1.length; i++) {
				SignalModule module=new SignalModule(portNames1[i], Baudrate,i);
				module.run();
				modulesList.add(module);
			}
		}
		else if(serial2.exists())
		{
			for (int i = 0; i < portNames2.length; i++) {
				SignalModule module=new SignalModule(portNames2[i], Baudrate,i);
				module.run();
				modulesList.add(module);
			}
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
