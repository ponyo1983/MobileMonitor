package com.lon.mobilemonitor.gui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.lon.mobilemonitor.R;
import com.lon.mobilemonitor.core.SignalChannel;
import com.lon.mobilemonitor.core.SignalModuleManager;
import com.lon.mobilemonitor.core.WorkMode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class WorkModeActivity extends Activity {

	WorkModeAdapter modeAdapter;
	Timer refreshTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTitle("通道模式设置");
		setContentView(R.layout.activity_mode_setting);

		ListView listView = (ListView) findViewById(R.id.listView1);

		modeAdapter = new WorkModeAdapter(this);

		listView.setAdapter(modeAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				SignalChannel channel = SignalModuleManager.getInstance()
						.getChannel(position);

				List<WorkMode> listModes = channel.getModeList();

				if (listModes == null || listModes.size() <= 0) {
					Toast.makeText(WorkModeActivity.this,
							"通道" + (position + 1) + "无模式列表", Toast.LENGTH_SHORT)
							.show();
				} else {
					setWorkMode(listModes, position, channel.getCurrentMode());
				}

			}

		});

		refreshTimer = new Timer();

		refreshTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				mHandler.sendEmptyMessage(0);

			}
		}, 1000, 1000);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				modeAdapter.notifyDataSetChanged();
				break;
			}
		}
	};

	private void setWorkMode(final List<WorkMode> listModes,
			final int channelNum, WorkMode currentMode) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("通道" + (channelNum + 1) + "模式设置");
		int cnt = listModes.size();
		String[] modeInfos = new String[cnt];
		int modeCode = -1;
		int selectIndex = 0;
		if (currentMode != null) {
			modeCode = currentMode.getMode();
		}
		for (int i = 0; i < cnt; i++) {
			WorkMode mode = listModes.get(i);
			StringBuilder sb = new StringBuilder();
			sb.append("采样率[" + mode.getSampleRate() + "]");
			sb.append(" 上限[" + mode.getUpper() + "]");
			sb.append(" 下限[" + mode.getLower() + "]");
			sb.append(" 单位[" + mode.getUnit() + "]");
			modeInfos[i] = sb.toString();
			if (mode.getMode() == modeCode) {
				selectIndex = i;
			}
		}

		builder.setSingleChoiceItems(modeInfos, selectIndex, null);

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				SignalChannel channel = SignalModuleManager.getInstance()
						.getChannel(channelNum);
				int selectedPosition = ((AlertDialog) dialog).getListView()
						.getCheckedItemPosition();
				channel.setWorkMode(listModes.get(selectedPosition).getMode());
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});

		builder.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mode_setting, menu);
		return true;
	}

}
