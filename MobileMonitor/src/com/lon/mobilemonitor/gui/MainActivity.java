package com.lon.mobilemonitor.gui;

import java.io.File;

import com.lon.mobilemonitor.R;
import com.lon.mobilemonitor.core.CalManager;
import com.lon.mobilemonitor.core.FileManager;
import com.lon.mobilemonitor.core.SignalModule;
import com.lon.mobilemonitor.core.SignalModuleManager;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

public class MainActivity extends Activity {

	private GridView gridView;
	// ͼƬ�����ֱ���
	private String[] titles = new String[] { "1:����", "2:����", "3:����", };
	// ͼƬID����
	private int[] images = new int[] { R.drawable.pic1, R.drawable.pic3,
			R.drawable.pic2, };
	SignalModule module;

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		
//		AlertDialog.Builder builder=new AlertDialog.Builder(this);
//		
//		builder.setTitle("�ƶ����");
//		
//		builder.setMessage("�Ƿ�ȷ���˳�����?");
//		
//		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		
//		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				
//				SignalModuleManager.getInstance().stop();
//				finish();
//				
//			}
//		});
//		
//		
//		
//		builder.show();
//		
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTitle("�ƶ����");
		gridView = (GridView) findViewById(R.id.gridview);
		PictureAdapter adapter = new PictureAdapter(titles, images, this);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				// TODO Auto-generated method stub

				switch (position) {
				case 0:
					startActivity(new Intent(MainActivity.this,
							SignalListActivity.class));
					break;
				case 1:
					startActivity(new Intent(MainActivity.this,
							WorkModeActivity.class));
					break;
				case 2: {

					AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("�ƶ����");
					builder.setMessage("�ƶ����汾��:1.0.0");
					
					builder.setPositiveButton("ȷ��", null);
					
					builder.show();
				}
					break;
				}

			}
		});
		CalManager.getInstance().ReadCalData(new File("/cal.txt"));
		FileManager.getInstance().start();
		SignalModuleManager.getInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
