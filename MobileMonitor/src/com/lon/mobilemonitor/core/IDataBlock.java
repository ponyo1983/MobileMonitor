package com.lon.mobilemonitor.core;

public interface IDataBlock {

	float[] getBlock(int timeout);

    int getSampleRate();
}
