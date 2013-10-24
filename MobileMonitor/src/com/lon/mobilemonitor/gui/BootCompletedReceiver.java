package com.lon.mobilemonitor.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent newIntent = new Intent(context, MainActivity.class);
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // ע�⣬������������ǣ�����������ʧ��
			context.startActivity(newIntent);
		}
	}

}
