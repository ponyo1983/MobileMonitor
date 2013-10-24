package com.lon.mobilemonitor.dsp;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FFTPlan {

	int fftNum=0;
	FloatFFT_1D fftTool;
	public FFTPlan(int num) {
		// TODO Auto-generated constructor stub
		this.fftNum=num;
		fftTool=new FloatFFT_1D(num);
		
	}
	public int getFFTNum() {
		return fftNum;
	}
	
	public void realForward(float[] data,int offset) {
		fftTool.realForward(data, offset);
	}
	
	public void complexForward(float[] data,int offset) {
		fftTool.complexForward(data,offset);
	}
}
