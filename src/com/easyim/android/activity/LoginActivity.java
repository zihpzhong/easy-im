package com.easyim.android.activity;

import org.jivesoftware.smack.XMPPException;

import org.jivesoftware.smack.packet.Presence;


import com.easyim.android.util.DialogFactory;
import com.easyim.android.xmpp.R;
import com.easyim.android.xmppmanager.XmppConnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

@SuppressWarnings("all")
public class LoginActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */

	private Button mBtnRegister;
	private Button mBtnLogin;
	private EditText mAccounts, mPassword;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loginpage);
        initView();
    }
    
    public void initView() {
		mBtnRegister = (Button) findViewById(R.id.regist_btn);
		mBtnRegister.setOnClickListener(this);
		mBtnLogin = (Button) findViewById(R.id.login_btn);
		mBtnLogin.setOnClickListener(this);
		mAccounts = (EditText) findViewById(R.id.lgoin_accounts);
		mPassword = (EditText) findViewById(R.id.login_password);
		
	}
    
    /**
	 * 处理点击事件
	 */
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.regist_btn:
			register();
			break;
		case R.id.login_btn:
			submit();
			break;
		default:
			break;
		}
	}

	private void register() {
		Intent intent = new Intent();
		intent.setClass(LoginActivity.this, RegisterActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 提交账号密码信息到服务器
	 */
	private void submit() {
		String accounts = mAccounts.getText().toString();
		String password = mPassword.getText().toString();
		if (accounts.length() == 0 || password.length() == 0) {
			DialogFactory.ToastDialog(this, "登录提示", "亲！帐号或密码不能为空哦");
		} else {
			try {
				//连接服务器
				XmppConnection.getConnection().login(accounts, password);
				//连接服务器成功，更改在线状态
				Presence presence = new Presence(Presence.Type.available);
				XmppConnection.getConnection().sendPacket(presence);
				//弹出登录成功提示
				//DialogFactory.ToastDialog(this, "登录提示", "亲，恭喜你，登录成功了！");
				//跳转到好友列表
				Intent intent = new Intent();
				intent.putExtra("USERID", accounts);
				intent.setClass(LoginActivity.this, FriendListActivity.class);
				startActivity(intent);
			} catch (XMPPException e) {
				XmppConnection.closeConnection();
				handler.sendEmptyMessage(2);
			}			
		}
	}
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg)
		{
			if(msg.what==2)
			{
				DialogFactory.ToastDialog(LoginActivity.this, "登录提示", "亲，登录失败，请重新登录！");
			}
		};
	};
}