package com.test.facerecognitionbyusbcamera.filemanager;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.test.facerecognitionbyusbcamera.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerActivity extends ListActivity {
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/sdcard";
	private String curPath = "/sdcard";
	private TextView mPath;

	public final static int RESULT_CODE_CHOOSED_PATH = 0;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.file_select);
		mPath = findViewById(R.id.mPath);
		Button buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent data = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("path", curPath);
				data.putExtras(bundle);
				setResult(RESULT_CODE_CHOOSED_PATH, data);
				finish();
			}
		});
		Button buttonCancle = findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
		getFileDir(rootPath);
	}

	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();

		if (!filePath.equals(rootPath)) {
			items.add("b1");
			paths.add(rootPath);
			items.add("b2");
			paths.add(f.getParent());
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			items.add(file.getName());
			paths.add(file.getPath());
		}

		setListAdapter(new FileAdapter(this, items, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			File file = new File(paths.get(position));
			if (file.isDirectory()) {
				String path = paths.get(position);
				getFileDir(path);
				curPath = path;
			} else {
				openFile(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), R.string.read_file_fail, Toast.LENGTH_LONG).show();
		}
	}

	private void openFile(File f) {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("path", f.getAbsolutePath());
        data.putExtras(bundle);
        setResult(RESULT_CODE_CHOOSED_PATH, data);
        finish();
	}
}