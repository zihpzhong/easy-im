package com.easyim.android.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import com.easyim.android.util.TimeRender;
import com.easyim.android.xmpp.R;
import com.easyim.android.xmppmanager.XmppConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    //������
    private ProgressBar pb;
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
		
		//��ȡ�ı���Ϣ
		this.msgText = (EditText) findViewById(R.id.formclient_text);
		this.pb = (ProgressBar) findViewById(R.id.formclient_pb);
		
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
		
		//�����ļ�
		FileTransferManager fileTransferManager = new FileTransferManager(XmppConnection.getConnection());
		fileTransferManager.addFileTransferListener(new RecFileTransferListener());
		//���͸���
		Button btattach = (Button) findViewById(R.id.formclient_btattach);
		btattach.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) 
			{
				Intent intent = new Intent(ChatActivity.this, FilesActivity.class);
				intent.putExtra("userChat", userChat);
				startActivityForResult(intent, 2);				
			}			
		});
	}
	private FileTransferRequest request;
	private File file;
	class RecFileTransferListener implements FileTransferListener 
	{
		@Override
		public void fileTransferRequest(FileTransferRequest prequest)
		{
			//���ܸ���
//			System.out.println("The file received from: " + prequest.getRequestor());
			
			file = new File("mnt/sdcard/" + prequest.getFileName());
			request = prequest;
			handler.sendEmptyMessage(5);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//���͸���
		if(requestCode==2 && resultCode==2 && data!=null){
			
			String filepath = data.getStringExtra("filepath");
			if(filepath.length() > 0)
			{
				sendFile(filepath);
			}
		}
	}
	
	
	private void sendFile(String filepath) {
		// ServiceDiscoveryManager sdm = new ServiceDiscoveryManager(connection);
		
		final FileTransferManager fileTransferManager = new FileTransferManager(XmppConnection.getConnection());
		//���͸�yinghan-pc��������xiaowang����ȡ�Լ��ķ��������ͺ��ѣ�
		//zss@microsof-482a97/Spark 2.6.3
		System.out.println("�ļ����Ͷ���:"+userChat);
		final OutgoingFileTransfer fileTransfer = fileTransferManager.createOutgoingFileTransfer(userChat+"/Spark 2.6.3");				
		
		final File file = new File(filepath);
		
		try 
		{
			fileTransfer.sendFile(file, "Sending");
		} 
		catch (Exception e) 
		{
			Toast.makeText(ChatActivity.this,"����ʧ��!",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() 
			{
				try{					
					while (true) 
					{
						Looper.prepare(); 
						Thread.sleep(500L);
						Status status = fileTransfer.getStatus();		
						Looper.prepare();
						if ((status == FileTransfer.Status.error)
								|| (status == FileTransfer.Status.complete)
								|| (status == FileTransfer.Status.cancelled)
								|| (status == FileTransfer.Status.refused))
						{
							handler.sendEmptyMessage(4);
							break;
						}
						else if(status == FileTransfer.Status.negotiating_transfer)
						{
							//..
						}
						else if(status == FileTransfer.Status.negotiated)
						{							
							//..
						}
						else if(status == FileTransfer.Status.initial)
						{
							//..
						}
						else if(status == FileTransfer.Status.negotiating_stream)
						{							
							//..
						}
						else if(status == FileTransfer.Status.in_progress)
						{
							//��������ʾ
							handler.sendEmptyMessage(2);
							
							long p = fileTransfer.getBytesSent() * 100L / fileTransfer.getFileSize();	
							
							android.os.Message message = handler.obtainMessage();
							message.arg1 = Math.round((float) p);
							message.what = 3;
							message.sendToTarget();
							Toast.makeText(ChatActivity.this,"���ͳɹ�!",Toast.LENGTH_SHORT).show();
						}
						
						
						 Looper.loop();
					}
				} 
				catch (Exception e) 
				{
					Toast.makeText(ChatActivity.this,"����ʧ��!",Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}).start();
	}
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				String[] args = (String[]) msg.obj;
				listMsg.add(new Msg(args[0], args[1], args[2], args[3]));
				adapter.notifyDataSetChanged();
				break;			
			case 2:
				//����������
				if(pb.getVisibility()==View.GONE){
					pb.setMax(100);
					pb.setProgress(1);
					pb.setVisibility(View.VISIBLE);
				}
				break;
			case 3:
				pb.setProgress(msg.arg1);
				break;
			case 4:
				pb.setVisibility(View.GONE);
				break;
			case 5:
				final IncomingFileTransfer infiletransfer = request.accept();
				
				//��ʾ��
				AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
				
				builder.setTitle("������")
						.setCancelable(false)
						.setMessage("�Ƿ�����ļ���"+file.getName()+"?")
						.setPositiveButton("����",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										try 
										{
											infiletransfer.recieveFile(file);
										} 
										catch (XMPPException e)
										{
											Toast.makeText(ChatActivity.this,"����ʧ��!",Toast.LENGTH_SHORT).show();
											e.printStackTrace();
										}
										
										handler.sendEmptyMessage(2);
										
										Timer timer = new Timer();
										TimerTask updateProgessBar = new TimerTask() {
											public void run() {
												//  ���ӵĻ��ᱨ  Can��t create handler inside thread that has not called Looper.prepare()���� 
												//  �ο�  http://vaero.blog.51cto.com/4350852/782595
												 Looper.prepare(); 
												if ((infiletransfer.getAmountWritten() >= request.getFileSize())
														|| (infiletransfer.getStatus() == FileTransfer.Status.error)
														|| (infiletransfer.getStatus() == FileTransfer.Status.refused)
														|| (infiletransfer.getStatus() == FileTransfer.Status.cancelled)
														|| (infiletransfer.getStatus() == FileTransfer.Status.complete)) 
												{
													cancel();
													handler.sendEmptyMessage(4);
												} 
												else
												{
													long p = infiletransfer.getAmountWritten() * 100L / infiletransfer.getFileSize();													
													
													android.os.Message message = handler.obtainMessage();
													message.arg1 = Math.round((float) p);
													message.what = 3;
													message.sendToTarget();
													Toast.makeText(ChatActivity.this,"�������!",Toast.LENGTH_SHORT).show();
												}
												 Looper.loop();
											}
										};
										timer.scheduleAtFixedRate(updateProgessBar, 10L, 10L);
										dialog.dismiss();
									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id)
									{
										request.reject();
										dialog.cancel();
									}
								}).show();
				
			
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
		myNoti.setLatestEventInfo(this, "easyIm��Ϣ", s, appIntent);
		mNotificationManager.notify(0, myNoti);
	}
}