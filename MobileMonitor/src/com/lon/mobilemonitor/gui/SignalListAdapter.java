package com.lon.mobilemonitor.gui;

import com.lon.mobilemonitor.R;
import com.lon.mobilemonitor.core.ISignal;
import com.lon.mobilemonitor.core.SignalModuleManager;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SignalListAdapter extends BaseAdapter {

	Context mContext;
	SignalModuleManager moduleManager;

	public SignalListAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.moduleManager = SignalModuleManager.getInstance();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return moduleManager.getModuleNum() * 3;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return moduleManager.getChannel(position);
	}

	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.mode_setting, null);

			ImageView imgView = (ImageView) convertView
					.findViewById(R.id.imageView1);
			imgView.setImageResource(R.drawable.monitorr);
			TextView textView = (TextView) convertView
					.findViewById(R.id.textView1);

			
			textView.setText(getSignalInfo(position));
			ImageView imgView2 = (ImageView) convertView
					.findViewById(R.id.imageView2);
			imgView2.setImageResource(R.drawable.arrow_right);

			convertView.setTag(new ViewHolder(imgView, textView, imgView2));
		} else {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			holder.txtView.setText(getSignalInfo(position));

		}
		return convertView;
	}

	private String getSignalInfo(int position) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("通道" + (position + 1)+":   ");
		
		ISignal signal=moduleManager.getChannel(position).getCurrentSignal();
		if(signal==null)
		{
			sb.append("无信号输入");
		}
		else {
			sb.append(signal.getSignalInfo());
		}
		
		return sb.toString();
	}

	class ViewHolder {
		ImageView img1;
		TextView txtView;
		ImageView img2;

		public ViewHolder(ImageView img1, TextView txtView, ImageView img2) {
			// TODO Auto-generated constructor stub
			this.img1 = img1;
			this.txtView = txtView;
			this.img2 = img2;
		}

	}

}
