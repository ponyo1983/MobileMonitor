package com.lon.mobilemonitor.core;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android_serialport_api.SerialPort;

///串口数据解析
public class FrameManager {

	enum MatchState {
		MatchNull, MatchHeader, // 遇到帧头
		MatchVersion, // 遇到版本
		MatchLength, // 遇到长度帧
		MatchLengthReversed, // 遇到长度反码
		MatchReceived, // 数据接收完毕
		MatchSumCheck, // 和校验通过
	}

	static final int MaxFrameLength = 16 * 1024; // 最大的数据链路层的帧大小
	static final byte ProtocolVersion = 1; // 协议版本
	final int[] HeaderTag = new int[] { 0xaa, 0xaa, }; // 帧头

	SerialPort serialPort;

	LinkedList<FrameFilter> listFilter = new LinkedList<FrameFilter>();

	Thread readThread; // 读串口数据

	public FrameManager(SerialPort serialPort) {
		// TODO Auto-generated constructor stub
		this.serialPort = serialPort;
		readThread = new Thread(new SerialRead(serialPort));
		readThread.start();

	}

	/*
	 * 添加数据帧过滤
	 */
	public IFrameFilter createFilter() {
		FrameFilter filter = new FrameFilter();

		synchronized (listFilter) {
			listFilter.add(filter);
		}
		return filter;
	}

	public void removeFilter(IFrameFilter filter) {
		if(FrameFilter.class.isInstance(filter))
		{
			FrameFilter frameFilter=	FrameFilter.class.cast(filter);
			synchronized (listFilter) {
				listFilter.remove(frameFilter);
			}
		}
	}

	class FrameFilter implements IFrameFilter {

		LinkedList<byte[]> listFrame = new LinkedList<byte[]>();
		ReentrantLock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		int[] cmdId;

		public FrameFilter(int[] cmdId) {
			// TODO Auto-generated constructor stub
			this.cmdId = cmdId;
		}

		public FrameFilter() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public byte[] getFrame(int timeout) {
			// TODO Auto-generated method stub
			byte[] frame = null;
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

		protected void putFrame(byte[] frame, int length) {

			// 先判断是否匹配
			int cmd = frame[HeaderTag.length + 5] & 0x7f;

			if (cmdId != null) // 必须匹配命令
			{
				boolean match = false;
				for (int i = 0; i < cmdId.length; i++) {
					if (cmdId[i] == cmd) {
						match = true;
						break;
					}
				}
				if (match == false)
					return;
			}
			byte[] data = new byte[length];
			System.arraycopy(frame, 0, data, 0, length);

			lock.lock();
			try {
				if (listFrame.size() > 100) {
					listFrame.removeFirst();
				}
				listFrame.addLast(data);
				condition.signal();

			} finally {
				lock.unlock();
			}
		}

	}

	class SerialRead implements Runnable {

		byte[] rcvBuffer = new byte[MaxFrameLength]; // 接收缓存
		byte[] frameBuffer = new byte[MaxFrameLength]; // 数据帧

		int frameLength = 0;
		int dataLength = 0;

		MatchState matchState = MatchState.MatchNull;

		SerialPort serialPort;

		public SerialRead(SerialPort serialPort) {
			// TODO Auto-generated constructor stub
			this.serialPort = serialPort;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				InputStream inputStream = serialPort.getInputStream();

				while (true) {
					int length = inputStream.read(rcvBuffer);

					ParseFrame(rcvBuffer, length);
				}

			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		private byte[] CutFrameHeader() {
			byte[] newBuffer = new byte[frameLength - HeaderTag.length];
			System.arraycopy(frameBuffer, HeaderTag.length, newBuffer, 0,
					newBuffer.length);

			frameLength = 0;
			matchState = MatchState.MatchNull;
			return newBuffer;
		}

		private void ParseFrame(byte[] rdBuffer, int length) {
			for (int i = 0; i < length; i++) {
				frameBuffer[frameLength] = rdBuffer[i];
				frameLength++;
				int uByte = rdBuffer[i] & 0xff;
				switch (matchState) {
				case MatchNull: // 未遇见帧头
					if (uByte == HeaderTag[frameLength - 1]) {
						if (frameLength == HeaderTag.length) {
							matchState = MatchState.MatchHeader;
						}
					} else {
						frameLength = 0;
					}
					break;
				case MatchHeader:
					if (uByte == ProtocolVersion) // 版本一致
					{
						matchState = MatchState.MatchVersion;
					} else {
						byte[] newBuffer = CutFrameHeader();
						ParseFrame(newBuffer, newBuffer.length);
					}
					break;
				case MatchVersion:
					if (frameLength == (HeaderTag.length + 3)) {
						dataLength = (frameBuffer[frameLength - 2] & 0xff)
								+ ((frameBuffer[frameLength - 1] & 0xff) << 8);
						if (dataLength <= MaxFrameLength - 10) {
							matchState = MatchState.MatchLength;
						} else {
							byte[] newBuffer = CutFrameHeader();
							ParseFrame(newBuffer, newBuffer.length);
						}
					}
					break;
				case MatchLength:
					if (frameLength == (HeaderTag.length + 5)) {
						int b1 = frameBuffer[frameLength - 1] & 0xff;
						int b2 = frameBuffer[frameLength - 3] & 0xff;
						int b3 = frameBuffer[frameLength - 2] & 0xff;
						int b4 = frameBuffer[frameLength - 4] & 0xff;
						if (((b1 ^ b2) == 0xff) && ((b3 ^ b4) == 0xff)) {
							matchState = MatchState.MatchLengthReversed;
						} else {
							byte[] newBuffer = CutFrameHeader();
							ParseFrame(newBuffer, newBuffer.length);
						}
					}
					break;
				case MatchLengthReversed: // 正式接收数据
					dataLength--;
					if (dataLength == 0) // 接收数据完成
					{
						matchState = MatchState.MatchReceived;
					}
					break;
				case MatchReceived:
					dataLength = (frameBuffer[HeaderTag.length + 1] & 0xff)
							+ ((frameBuffer[HeaderTag.length + 2] & 0xff) << 8);
					int sumCheck = 0;
					for (int j = 0; j < dataLength; j++) {
						sumCheck += (frameBuffer[HeaderTag.length + 5 + j] & 0xff);
					}
					if ((sumCheck & 0xff) == (rdBuffer[i] & 0xff)) {

						matchState = MatchState.MatchSumCheck;
						{
							matchState = MatchState.MatchNull;

							for (FrameFilter filter : listFilter) {
								filter.putFrame(frameBuffer, frameLength);
							}
							frameLength = 0;
						}
					} else {
						byte[] newBuffer = CutFrameHeader();
						ParseFrame(newBuffer, newBuffer.length);
					}
					break;
				default:
					break;

				}
				if (frameLength >= frameBuffer.length) // 数据溢出
				{
					matchState = MatchState.MatchNull;
					frameLength = 0;
				}
			}
		}

	}

}
