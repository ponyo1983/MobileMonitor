package com.lon.mobilemonitor.gui;

import java.util.Timer;
import java.util.TimerTask;

import com.lon.mobilemonitor.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SignalListActivity extends Activity {

	SignalListAdapter signalListAdapter;
	Timer refreshTimer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTitle("测试信号列表");
		setContentView(R.layout.activity_signal_list);
		
		
		ListView listView=(ListView)findViewById(R.id.listView1);
		signalListAdapter=new SignalListAdapter(this);
		listView.setAdapter(signalListAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				Intent intent=new Intent(SignalListActivity.this, SignalDetailActivity.class);
				intent.putExtra("channel", position); //通道的序号
				startActivity(intent);
				
				
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
				signalListAdapter.notifyDataSetChanged();
				break;
			}
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signal_list, menu);
		return true;
	}

}
