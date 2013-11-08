package com.lon.mobilemonitor.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class CalManager {

	
	private static CalManager calManager=null;
	private CalManager(){}
	
	
	ArrayList<CalUnit> calUnits=new ArrayList<CalUnit>();
	
	public static CalManager getInstance()
	{
		if(calManager==null)
		{
			calManager=new CalManager();
		}
		return calManager;
	}
	
	public void ReadCalData(File file)
	{
		if(file.exists()==false) return;
		calUnits.clear();
		
		try {
			BufferedReader bufRead = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String str;
			while ((str = bufRead.readLine()) != null) {
				
				str=str.trim();
				if(str.isEmpty()) continue;
				if(str.startsWith("CAL="))
				{
					str=str.substring(4);
					
					String[] paramString=str.split(",");
					if(paramString.length<6) continue;
					String id=paramString[0];
					int channel=Integer.parseInt(paramString[1].trim());
					
					int pCnt=(paramString.length-2)/2;
					
					CalUnit calUnit=new CalUnit(id, channel);
					
					for(int i=0;i<pCnt;i++)
					{
						float testVal=Float.parseFloat(paramString[2+2*i].trim());
						float realVal=Float.parseFloat(paramString[2+2*i+1].trim());
						calUnit.addCalData(testVal, realVal);
					}
					
					calUnits.add(calUnit);
					
					
				
				}
				
			}
			bufRead.close();
		} catch (IOException ioe) {
		}
		
		
	}
	
	public float getRealAmpl(String id,int channel,float ampl)
	{
		for(CalUnit unit:calUnits)
		{
			if(unit.id.equals(id)&&(unit.channel==channel))
			{
				return unit.getRealValue(ampl);
			}
		}
		return ampl;
	}
	
	
}


class CalUnit
{
	String id="";
	int channel;
	
	int calNum=0;
	float[] realData=new float[10];
	float[] testData=new float[10];
	
	public CalUnit(String id,int channel)
	{
		this.id=id;
		this.channel=channel;
	}
	
	public void addCalData(float testVal,float realVal)
	{
		realData[calNum]=realVal;
		testData[calNum]=testVal;
		calNum++;
	}
	
	public float getRealValue(float val)
	{
		if(calNum<2) return val;
		
		int matchIndex=-1;
		if(val<=testData[0])
		{
			matchIndex=0;
		}
		else if(val>=testData[calNum-1])
		{
			matchIndex=calNum-2;
		}
		else
		{
			for(int i=1;i<calNum;i++)
			{
				if(val<testData[i])
				{
					matchIndex=i-1;
					break;
				}
			}
		}
		
		if(matchIndex>=0&&matchIndex<=calNum-2)
		{
			float k=(realData[matchIndex]-realData[matchIndex+1])/(testData[matchIndex]-testData[matchIndex+1]);
			float b=realData[matchIndex]-k*testData[matchIndex];
			return (k*val)+b;
		}
		return val;
	}
	
	
	
	
	
}