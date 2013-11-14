package com.sys.android.activity;

import java.util.ArrayList;
import java.util.List;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.sys.android.util.TimeRender;
import com.sys.android.xmpp.R;
import com.sys.android.xmppmanager.XmppConnection;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {

	private String userChat="";//��ǰ����
	private MyAdapter adapter;
	private List<Msg> listMsg = new ArrayList<Msg>();
	private String pUSERID;
	private String pFRIENDID;
	private EditText msgText;
	private TextView chat_name;
    private NotificationManager mNotificationManager;

	public class Msg {
		String userid;
		String msg;
		String date;
		String from;

		public Msg(String userid, String msg, String date, String from) {
			this.userid = userid;
			this.msg = msg;
			this.date = date;
			this.from = from;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chat_client);
		mNotificationManager = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);		
		//��ȡIntent���������û���
		this.pUSERID = getIntent().getStringExtra("USERID");
		this.userChat=getIntent().getStringExtra("user");
		this.pFRIENDID = getIntent().getStringExtra("FRIENDID");	
		System.out.println("������Ϣ���û�pFRIENDID�ǣ�"+userChat);
		System.out.println("������Ϣ���û�pUSERID�ǣ�"+pUSERID);
		chat_name = (TextView)findViewById(R.id.chat_name);
		chat_name.setText(pFRIENDID);
		ListView listview = (ListView) findViewById(R.id.formclient_listview);
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		this.adapter = new MyAdapter(this);
		listview.setAdapter(adapter);		
		//��ȡ�ı���Ϣ
		this.msgText = (EditText) findViewById(R.id.formclient_text);
		//��Ϣ����
		ChatManager cm = XmppConnection.getConnection().getChatManager();		
		//������Ϣ��pc�������ĺ��ѣ���ȡ�Լ��ķ��������ͺ��ѣ�
		final Chat newchat = cm.createChat(pFRIENDID, null);
		cm.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean able) {
				chat.addMessageListener(new MessageListener() {
					@Override
					public void processMessage(Chat chat, Message message) {
						//�յ�����pc����������Ϣ����ȡ�Լ����ѷ�������Ϣ��
						if(message.getFrom().contains(userChat))
						{
							//��ȡ�û�����Ϣ��ʱ�䡢IN
							String[] args = new String[] {userChat, message.getBody(), TimeRender.getDate(), "IN" };
							//��handler��ȡ������ʾ��Ϣ
							android.os.Message msg = handler.obtainMessage();
							msg.what = 1;
							msg.obj = args;
							msg.sendToTarget();
						}
					}
				});
			}
		});
		//���ذ�ť
		Button mBtnBack = (Button) findViewById(R.id.chat_back);
		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				  finish();			
			}			
		});
		//������Ϣ
		Button btsend = (Button) findViewById(R.id.formclient_btsend);
		btsend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//��ȡtext�ı�
				String msg = msgText.getText().toString();			
				if(msg.length() > 0){
					//������Ϣ
					listMsg.add(new Msg(pUSERID, msg, TimeRender.getDate(), "OUT"));
					//ˢ��������
					adapter.notifyDataSetChanged();				
					try {
						//������Ϣ
						newchat.sendMessage(msg);					
					}catch (XMPPException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					Toast.makeText(ChatActivity.this, "������Ϣ����Ϊ��", Toast.LENGTH_SHORT).show();
				}
				//���text
				msgText.setText("");
			}
		});
	}
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				String[] args = (String[]) msg.obj;
				listMsg.add(new Msg(args[0], args[1], args[2], args[3]));
				adapter.notifyDataSetChanged();
				break;			
			default:
				break;
			}
		};
	};
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		XmppConnection.closeConnection();
		System.exit(0);
	}
	class MyAdapter extends BaseAdapter {
		private Context cxt;
		private LayoutInflater inflater;
		public MyAdapter(ChatActivity formClient) {
			this.cxt = formClient;
		}
		@Override
		public int getCount() {
			return listMsg.size();
		}
		@Override
		public Object getItem(int position) {
			return listMsg.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			this.inflater = (LayoutInflater) this.cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(listMsg.get(position).from.equals("IN")){
				convertView = this.inflater.inflate(R.layout.formclient_chat_in, null);
			}else{
				convertView = this.inflater.inflate(R.layout.formclient_chat_out, null);
			}
			TextView useridView = (TextView) convertView.findViewById(R.id.formclient_row_userid);
			TextView dateView = (TextView) convertView.findViewById(R.id.formclient_row_date);
			TextView msgView = (TextView) convertView.findViewById(R.id.formclient_row_msg);
			useridView.setText(listMsg.get(position).userid);
			dateView.setText(listMsg.get(position).date);
			msgView.setText(listMsg.get(position).msg);
			return convertView;
		}
	}	
	protected void setNotiType(int iconId, String s) {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent appIntent = PendingIntent.getActivity(this, 0, intent, 0);
		Notification myNoti = new Notification();
		myNoti.icon = iconId;
		myNoti.tickerText = s;
		myNoti.defaults = Notification.DEFAULT_SOUND;
		myNoti.flags |= Notification.FLAG_AUTO_CANCEL;
		myNoti.setLatestEventInfo(this, "QQ��Ϣ", s, appIntent);
		mNotificationManager.notify(0, myNoti);
	}
}