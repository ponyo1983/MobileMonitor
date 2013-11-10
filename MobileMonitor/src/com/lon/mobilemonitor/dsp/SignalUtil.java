package com.lon.mobilemonitor.dsp;

public class SignalUtil {
	
    public float calAmpl(float[] data,int offset, int cnt)
    {
        float sum = 0;
        for (int i = 0; i < cnt; i++)
        {
            sum += (data[offset + i] * data[offset + i]);
        }

        return (float)Math.sqrt(sum / cnt);
    }
    
    /*
     * 计算直流的信号
     * */
    public float calDCAmpl(float[] data,int offset,int cnt)
    {
    	float sum=0;
    	for(int i=0;i<cnt;i++)
    	{
    		sum+=data[offset+i];
    	}
    	return sum/cnt;
    }
    
    public float  calACAmpl(float[] data,int offset,int cnt)
    {
    	float dcAmpl=calDCAmpl(data, offset, cnt);
    	float sum = 0;
        for (int i = 0; i < cnt; i++)
        {
            sum += ((data[offset + i]-dcAmpl) * (data[offset + i]-dcAmpl));
        }

        return (float)Math.sqrt(sum / cnt);
    
    }
    public void calDCACAmpl(float[] data,int offset,int cnt,float[] result)
    {
    	float dcAmpl=calDCAmpl(data, offset, cnt);
    	result[0]=dcAmpl;
    	float sum = 0;
        for (int i = 0; i < cnt; i++)
        {
            sum += ((data[offset + i]-dcAmpl) * (data[offset + i]-dcAmpl));
        }

        result[1]= (float)Math.sqrt(sum / cnt);
    }

}
