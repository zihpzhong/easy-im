package com.easyim.android.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.easyim.android.xmpp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class FilesActivity extends Activity{
private List<String> items = null;
private List<String> pathlist = null;
private ListView listview;
private final String rootpath = "/";
private String originalpath;

@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.frmfiles);

	//���ظ���
	listview = (ListView) findViewById(R.id.frmfiles_listview);
	
	getFileDir(rootpath);
	listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			//ȡ��·��
			File file = new File(pathlist.get(position));
			//isDirectory()��һ���ļ�·������true
			if (file.isDirectory())
			{
				try 
				{
					getFileDir(file.getPath());
				}
				catch (Exception e)
				{
					Toast.makeText(FilesActivity.this, "��·����û�и���", Toast.LENGTH_SHORT).show();
					getFileDir(file.getParent());
				}
			} 
			else
			{
				originalpath = file.getPath().toLowerCase();
				onExit();
			}
		}
	});
}
//�˳�����
private void onExit() 
{
	Intent intent = new Intent();
	intent.putExtra("filepath", originalpath);
	setResult(2, intent);
	finish();
}
//��ȡ������ʾ
private void getFileDir(String filepath)
{
	items = new ArrayList<String>();
	pathlist = new ArrayList<String>();
	File sfile = new File(filepath);

	if (!rootpath.equals(filepath))
	{
		items.add("back");
		pathlist.add(sfile.getParent());
	}

	File[] files = sfile.listFiles();
	for (File file : files) 
	{
		items.add(file.getName());
		pathlist.add(file.getPath());
	}
	
	listview.setAdapter(new FileAdapter(this, items, pathlist));
}
}
