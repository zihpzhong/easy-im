package com.sys.android.activity;

import com.sys.android.xmpp.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Window;
/**
 * ��ӭ����
 * @author yuanqihesheng
 * @date 2013-04-27
 */
public class WelcomeActivity extends Activity {
	private Handler mHandler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		initView();
	}

	public void initView() {
		// ������״�����
			mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {
				public void run() {
					goLoginActivity();
				}
			}, 1000);
	}

	/**
	 * �����½����
	 */
	public void goLoginActivity() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}
	/**
	 * ���������ݷ�ʽ
	 */
	public void createShut() {
		// ������ӿ�ݷ�ʽ��Intent
		Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		String title = getResources().getString(R.string.app_name);
		// ���ؿ�ݷ�ʽ��ͼ��
		Parcelable icon = Intent.ShortcutIconResource.fromContext(WelcomeActivity.this, R.drawable.icon);
		// ���������ݷ�ʽ�����Intent,�ô�����������Ŀ�ݷ�ʽ���ٴ������ó���
		Intent myIntent = new Intent(WelcomeActivity.this,WelcomeActivity.class);
		// ���ÿ�ݷ�ʽ�ı���
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		// ���ÿ�ݷ�ʽ��ͼ��
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// ���ÿ�ݷ�ʽ��Ӧ��Intent
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);
		// ���͹㲥��ӿ�ݷ�ʽ
		sendBroadcast(addIntent);
	}
}