package com.lon.mobilemonitor.core;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

///���ݿ�
public class DataBlock implements IDataBlock {

	LinkedList<float[]> listFrame = new LinkedList<float[]>();
	int sampleRate = 0; // Ϊ0����ͨ������������δ֪

	int prevIndex = Integer.MAX_VALUE;

	float[] segBuffer;
	int segLength = 0;

	float[] totalBuffer;
	int totalIndex = 0;

	int timeSegment = 0;
	int timeLength = 0;

	ReentrantLock lock = new ReentrantLock();
	Condition condition = lock.newCondition();

	public DataBlock(int timeSegment, int timeLength, int sampleRate) {
		// TODO Auto-generated constructor stub
		this.timeSegment = timeSegment;
		this.timeLength = timeLength;
		this.sampleRate = sampleRate;
	}

	public int getTimeSegment() {
		return timeSegment;
	}

	public int getTimeLength() {
		return timeLength;
	}

	@Override
	public float[] getBlock(int timeout) {
		// TODO Auto-generated method stub
		float[] frame = null;
		lock.lock();
		try {

			if (listFrame.size() > 0) {
				frame = listFrame.removeFirst();
			}
			if (frame == null && timeout != 0) {
				try {
					if (timeout > 0) {

						condition.await(timeout, TimeUnit.MILLISECONDS);

					} else {
						condition.await();
					}
				} catch (InterruptedException e) {
					// TODO: handle exception
				}

			}
			if (listFrame.size() > 0) {
				frame = listFrame.removeFirst();
			}
		} finally {
			lock.unlock();
		}
		return frame;
	}

	@Override
	public int getSampleRate() {
		// TODO Auto-generated method stub
		return sampleRate;
	}

	public void setSampleRate(int rate) {
		if (sampleRate != rate && rate > 0) {
			synchronized (this) {
				int length = timeLength * rate / 1000;
				totalBuffer = new float[length];

				int segLength = timeSegment * rate / 1000;

				segBuffer = new float[segLength];
				this.segLength = 0;

				sampleRate = rate;
			}

		}
	}

	/*
	 * @param data �ɼ�������
	 * 
	 * @param sampleIndex ���ݵ���ţ���0��ʼ������
	 */
	public void putSampleData(float[] data, int sampleIndex) {
		synchronized (this) {

			if (sampleRate > 0) {
				if (sampleIndex == (prevIndex + 1)) // ����֡����
				{
					prevIndex = sampleIndex;
				} else {
					// ���¿�ʼ����
					prevIndex = sampleIndex;
					segLength = 0;
					totalIndex = totalBuffer.length - segBuffer.length;

				}
				for (int i = 0; i < data.length; i++) {
					segBuffer[segLength] = data[i];
					segLength++;
					if (segLength >= segBuffer.length) // ���һ֡
					{
						for (int j = 0; j < segLength; j++) {
							totalBuffer[totalIndex] = segBuffer[j];
							totalIndex = (totalIndex + 1) % totalBuffer.length;
						}
						int tmpIndex = (totalIndex + 1) % totalBuffer.length;

						float[] sampleFrame = new float[totalBuffer.length];

						for (int j = 0; j < totalBuffer.length; j++) {
							sampleFrame[j] = totalBuffer[tmpIndex];
							tmpIndex = (tmpIndex + 1) % totalBuffer.length;
						}
						segLength = 0;

						lock.lock();
						try {
							if (listFrame.size() > 100) {
								listFrame.removeFirst();
							}
							listFrame.addLast(sampleFrame);
							condition.signal();
						} finally {
							lock.unlock();
						}

					}
				}

			}
		}
	}
}
