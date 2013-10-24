package com.lon.mobilemonitor.dsp;

public class SignalAmpl {
	
    public float calAmpl(float[] data,int offset, int cnt)
    {
        float sum = 0;
        for (int i = 0; i < cnt; i++)
        {
            sum += (data[offset + i] * data[offset + i]);
        }

        return (float)Math.sqrt(sum / cnt);
    }

}
