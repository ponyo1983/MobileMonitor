package com.lon.mobilemonitor.gui;

import com.lon.mobilemonitor.R;
import com.lon.mobilemonitor.core.SignalModuleManager;
import com.lon.mobilemonitor.core.WorkMode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WorkModeAdapter extends BaseAdapter {

	Context mContext;
	SignalModuleManager moduleManager;

	public WorkModeAdapter(Context context) {
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
			imgView.setImageResource(R.drawable.gear_wheel);
			TextView textView = (TextView) convertView
					.findViewById(R.id.textView1);

			
			textView.setText(getModeString(position));
			ImageView imgView2 = (ImageView) convertView
					.findViewById(R.id.imageView2);
			imgView2.setImageResource(R.drawable.mode_list);

			convertView.setTag(new ViewHolder(imgView, textView, imgView2));
		} else {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			holder.txtView.setText(getModeString(position));

		}
		return convertView;
	}

	private String getModeString(int position) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("通道" + (position + 1)+":   ");
		
		WorkMode wkMode=moduleManager.getChannel(position).getCurrentMode();
		if(wkMode==null)
		{
			sb.append("未设置");
		}
		else {
			sb.append("采样率["+wkMode.getSampleRate()+"]");
			sb.append(" 上限:["+wkMode.getUpper()+"]");
			sb.append(" 下限:["+wkMode.getLower()+"]");
			sb.append(" 单位:["+wkMode.getUnit()+"]");
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
