package com.test.facerecognitionbyusbcamera.filemanager;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.test.facerecognitionbyusbcamera.R;

import java.io.File;
import java.util.List;

public class FileAdapter extends BaseAdapter {
  private LayoutInflater mInflater;
  private Bitmap mIcon1;
  private Bitmap mIcon2;
  private Bitmap mIcon3;
  private Bitmap mIcon4;
  private List<String> items;
  private List<String> paths;
  public FileAdapter(Context context, List<String> it, List<String> pa) {
    mInflater = LayoutInflater.from(context);
    items = it;
    paths = pa;
    mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.back01);
    mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.back02);
    mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
    mIcon4 = BitmapFactory.decodeResource(context.getResources(),R.drawable.doc);
  }
  
  public int getCount()
  {
    return items.size();
  }

  public Object getItem(int position)
  {
    return items.get(position);
  }
  
  public long getItemId(int position)
  {
    return position;
  }
  
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.file_row, null);
      holder = new ViewHolder();
      holder.text = (TextView) convertView.findViewById(R.id.text);
      holder.icon = (ImageView) convertView.findViewById(R.id.icon);
      
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    File f = new File(paths.get(position).toString());
    if (items.get(position).toString().equals("b1")) {
      holder.text.setText(R.string.file_go_root);
      holder.icon.setImageBitmap(mIcon1);
    } else if(items.get(position).toString().equals("b2")) {
      holder.text.setText(R.string.file_go_up);
      holder.icon.setImageBitmap(mIcon2);
    } else {
      holder.text.setText(f.getName());
      if(f.isDirectory()) {
        holder.icon.setImageBitmap(mIcon3);
      } else {
        holder.icon.setImageBitmap(mIcon4);
      }
    }
    return convertView;
  }

  private class ViewHolder {
    TextView text;
    ImageView icon;
  }
}
