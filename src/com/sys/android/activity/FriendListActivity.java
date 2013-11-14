package com.sys.android.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.sys.android.entity.FriendInfo;
import com.sys.android.entity.GroupInfo;
import com.sys.android.util.Utils;
import com.sys.android.xmpp.R;
import com.sys.android.xmppmanager.XmppConnection;
import com.sys.android.xmppmanager.XmppService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * �����б�
 */
@SuppressWarnings("all")
public class FriendListActivity extends Activity implements OnGroupClickListener, OnChildClickListener {
	private String pUSERID;//��ǰ�û�
	private String pGROUPNAME;//��ǰ��
	private LayoutInflater mChildInflater;
	private ExpandableListView listView;
	private List<GroupInfo> groupList;
	private List<FriendInfo> friendList;
	public static MyAdapter adapter;
	public static FriendListActivity friendListActivity;
	FriendInfo friendInfo;
	GroupInfo groupInfo;
	Roster roster = XmppConnection.getConnection().getRoster();
	public final static int NOTIF_UI = 1000;
	public static final int ADD_FRIEND = 1003;
    private NotificationManager mNotificationManager;
    XMPPConnection connection = XmppConnection.getConnection();
	public static final String CHECK = null;
    private String fromUserJid = null;//����������û���userJid
    private String toUserJid = null;//�յ�������û���userJid
	private TextView myStatusText = null;
    private String myMood = null;
    private String friendMood = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		friendListActivity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.friend_list);
		mNotificationManager = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);		
		this.pUSERID = getIntent().getStringExtra("USERID");
		this.pGROUPNAME = getIntent().getStringExtra("GROUPNAME");
		this.fromUserJid = getIntent().getStringExtra("fromUserJid");
		TextView friend_list_myName = (TextView)findViewById(R.id.friend_list_myName);
		friend_list_myName.setText(pUSERID);
		listView = (ExpandableListView) findViewById(R.id.contact_list_view);
		registerForContextMenu(listView);
		try {		
			loadFriend();
		} catch (Exception e) {		
			e.printStackTrace();
			Intent intent = new Intent(this,LoginActivity.class);
			startActivity(intent);
			finish();
			Toast.makeText(this, "������",0).show();
			return;
		}
		adapter = new MyAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnGroupClickListener(this);
		listView.setOnChildClickListener(this);
		listView.setOnLongClickListener(new OnLongClickListener() {		
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	
		listView.setOnLongClickListener(new OnLongClickListener() {		
			@Override
			public boolean onLongClick(View v) {	
				return false;
			}
		});
		//����ѷ����Լ���״̬
		myStatusText = (TextView)findViewById(R.id.myStatusText);
		String status = myStatusText.getText().toString();
		XmppService.changeStateMessage(connection, status);	
		
		roster.addRosterListener(  
             new RosterListener() {   
                    @Override  
                    //��������������Ϣ
                    public void entriesAdded(Collection<String> invites) {  
                        // TODO Auto-generated method stub  
                    	System.out.println("�����������������Ϣ�ǣ�"+invites); 
                    	for (Iterator iter = invites.iterator(); iter.hasNext();) {
                    		  String fromUserJids = (String)iter.next();
                    		  System.out.println("fromUserJids�ǣ�"+fromUserJids);
                    		  fromUserJid = fromUserJids;
                    	}               	 
                    	 if(fromUserJid!=null){
                     	   Intent intent = new Intent();
                     	   intent.putExtra("USERID", pUSERID);
                     	   intent.putExtra("fromUserJid", fromUserJid);
                  		   intent.setClass(FriendListActivity.this, FriendListActivity.class);
                  		   startActivity(intent); 
                 	   }     
                    }    
                    @Override  
                    //��������ͬ�������Ϣ
                    public void entriesUpdated(Collection<String> invites) {  
                            // TODO Auto-generated method stub  
                	       System.out.println("����������ͬ�����Ϣ�ǣ�"+invites); 	
                    	   for (Iterator iter = invites.iterator(); iter.hasNext();) {
	                  		  String fromUserJids = (String)iter.next();
	                  		  System.out.println("ͬ����ӵĺ����ǣ�"+fromUserJids);
	                  	      toUserJid = fromUserJids;
	                    	} 
                    	    if(toUserJid!=null){
    	                        XmppService.addUserToGroup(toUserJid, pGROUPNAME, connection);
    	                        loadFriend();
                    	    }               	    
                    } 
                    @Override  
                    //��������ɾ����Ϣ
                    public void entriesDeleted(Collection<String> delFriends) {  
                        // TODO Auto-generated method stub  
                    	System.out.println("������ɾ�����ѵ���Ϣ�ǣ�"+delFriends);  
                    	if(delFriends.size()>0){
                    		loadFriend();
                	    }  
                    } 
                   @Override  
                   //��������״̬�ı���Ϣ
                    public void presenceChanged(Presence presence) {  
                        // TODO Auto-generated method stub  
                	    friendMood = presence.getStatus();
                    	System.out.println("presence.getStatus()�ǣ�"+presence.getStatus());
            }     
	                      
	    });  
		
		ChatManager cm = XmppConnection.getConnection().getChatManager();		
		//��ȡ���������������κ���Ϣ
		cm.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean able) {
				chat.addMessageListener(new MessageListener() {
					@Override
					public void processMessage(Chat chat2, Message message) {
							android.os.Message msg=handler.obtainMessage(); 
							System.out.println("��������������Ϣ�ǣ�"+message.getBody());
							//setNotiType(R.drawable.log, message.getBody());
		                    msg.obj=message.getBody();  
		                    msg.sendToTarget();  
					}
				});
			}
		});
		System.out.println("fromUserJid�ǣ�"+fromUserJid);
        if(fromUserJid!=null){
    		AlertDialog.Builder dialog=new AlertDialog.Builder(FriendListActivity.this);
			dialog.setTitle("��������")
			      .setIcon(R.drawable.log)
			      .setMessage("��"+fromUserJid+"�����㷢���������룬�Ƿ���ӶԷ�Ϊ����?")
			      .setPositiveButton("���", new DialogInterface.OnClickListener() {
		                     @Override
		                     public void onClick(DialogInterface dialog, int which) {		 
		                         // TODO Auto-generated method stub	
	                    	     dialog.cancel();//ȡ��������
	                    	     //������Ӻ�����ظ���Ϣ����������Ӧ��Ҳ����һ����������
	                    	     Presence subscription = new Presence(Presence.Type.subscribe);
	                             subscription.setTo(fromUserJid);
	                             XmppConnection.getConnection().sendPacket(subscription);
	                             System.out.println("pGROUPNAME�ǣ�"+pGROUPNAME);
	                             if(pGROUPNAME == null){
	                            	 pGROUPNAME = "�ҵĺ���";
	                             }
	                             XmppService.addUserToGroup(fromUserJid, pGROUPNAME, connection);
	                             Intent intent = new Intent();
	                    	     intent.putExtra("USERID", pUSERID);
	                    	     intent.putExtra("fromUserJid", CHECK);
	                   			 intent.setClass(FriendListActivity.this, FriendListActivity.class);
	                   			 startActivity(intent); 
		                     }
		                   })
			       .setNegativeButton("�ܾ�", new DialogInterface.OnClickListener() {
			                 public void onClick(DialogInterface dialog, int which) {			 
			                     // TODO Auto-generated method stub
			                	 XmppService.removeUser(roster, fromUserJid);
			                     dialog.cancel();//ȡ��������
			                 }
			               }).create().show();
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
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				String[] args = (String[]) msg.obj;
				break;			
			default:
				break;
			}
		};
	};
	
	public void loadFriend() {
		try {
			XMPPConnection conn = XmppConnection.getConnection();
			Roster roster = conn.getRoster();
			Collection<RosterGroup> groups = roster.getGroups();
			groupList = new ArrayList<GroupInfo>();
			for (RosterGroup group : groups) {
				groupInfo = new GroupInfo();
				friendList = new ArrayList<FriendInfo>();
				groupInfo.setGroupName(group.getName());
				Collection<RosterEntry> entries = group.getEntries();
				for (RosterEntry entry : entries) {
					if("both".equals(entry.getType().name())){//ֻ���˫�ߺ��� 
						friendInfo = new FriendInfo();
						friendInfo.setUsername(Utils.getJidToUsername(entry.getUser()));
						System.out.println("�ҵĺ��������ǣ�"+entry.getStatus().fromString(entry.getUser()));
						if(friendMood == null){
							friendMood ="Q�Ұɣ�����������ţ�";
						}
						friendInfo.setMood(friendMood);
						friendList.add(friendInfo);
						friendInfo = null;			
					}
				}
				groupInfo.setFriendInfoList(friendList);
				groupList.add(groupInfo);
				groupInfo = null;
			}
		if(groupList.isEmpty()){
			groupInfo = new GroupInfo();
			groupInfo.setGroupName("�ҵĺ���");
			groupInfo.setFriendInfoList(new ArrayList<FriendInfo>());
			groupList.add(groupInfo);
			groupInfo = null;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		adapter.notifyDataSetChanged();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		XmppConnection.closeConnection();
		Intent intent = new Intent(this, XmppService.class);
		stopService(intent);
		friendListActivity = null;
		super.onDestroy();
	}
	public class MyAdapter extends BaseExpandableListAdapter {
		Context context;
		public MyAdapter(Context context){
			mChildInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		class FriendHolder{
			TextView name;
			TextView mood;
			ImageView iv;
		}
		
		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return groupList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return groupList.get(groupPosition).getFriendInfoList().size();
		}

		@Override
		public GroupInfo getGroup(int groupPosition) {
			
			return groupList.get(groupPosition);
		}
		public GroupInfo getGroup(String groupName) {
			GroupInfo groupInfo = null;
			if(getGroupCount() > 0){
				for(int i = 0,j = getGroupCount();i< j;i++){
					GroupInfo holder = (GroupInfo) getGroup(i);
					if(TextUtils.isEmpty(holder.getGroupName())){
						groupList.remove(holder);
					}else{
						if(holder.getGroupName().equals(groupInfo)){
							groupInfo = holder;
						}
					}
				}
			}
			return groupInfo;
		}

		@Override
		public FriendInfo getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return groupList.get(groupPosition).getFriendInfoList().get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
				FriendHolder holder;
				if(convertView == null){
					holder = new FriendHolder();					
					convertView = mChildInflater.inflate(R.layout.friend_group_item,null);
					holder.name =  (TextView) convertView.findViewById(R.id.friend_group_list_name);
					holder.iv = (ImageView) convertView.findViewById(R.id.friend_group_list_icon);
					convertView.setTag(holder);
				}else{
					holder = (FriendHolder) convertView.getTag();
				}
				String groupname = groupList.get(groupPosition).getGroupName();
				holder.name.setText(groupname);
				if(isExpanded){
					holder.iv.setBackgroundResource(R.drawable.sc_group_expand);
				}else{
					holder.iv.setBackgroundResource(R.drawable.sc_group_unexpand);		
				}		
				return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			    FriendHolder holder;
				if(convertView == null){
					holder = new FriendHolder();
					convertView = mChildInflater.inflate(R.layout.friend_child_item,null);
					holder.name =  (TextView) convertView.findViewById(R.id.friend_nickname);
					holder.mood =  (TextView) convertView.findViewById(R.id.friend_mood);
					convertView.setTag(holder);
				}else{
					holder = (FriendHolder) convertView.getTag();
				}
				FriendInfo groupname = groupList.get(groupPosition).getFriendInfoList().get(childPosition);
				holder.name.setText(groupname.getUsername());
				holder.mood.setText(groupname.getMood());
				if(isLastChild){
					listView.setItemChecked(groupPosition, true);
				}
				return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View view,
			int groupPosition, long id) {
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		FriendInfo info = groupList.get(groupPosition).getFriendInfoList().get(childPosition);
		Intent intent = new Intent(this,ChatActivity.class);
		String pFRIENDID = info.getJid();
		intent.putExtra("FRIENDID", pFRIENDID);
		intent.putExtra("user", pFRIENDID);
		intent.putExtra("USERID", pUSERID);
		startActivity(intent);
		return false;
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Menu.FIRST + 1, 1,"ˢ���б�").setIcon(R.drawable.menu_refresh);
		menu.add(Menu.NONE, Menu.FIRST + 2, 1,"��������").setIcon(R.drawable.menu_setting);
		menu.add(Menu.NONE, Menu.FIRST + 3, 1,"��Ӻ���").setIcon(R.drawable.addfriends_icon_icon);
		menu.add(Menu.NONE, Menu.FIRST + 4, 1,"�˳���¼").setIcon(R.drawable.menu_exit);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			friendMood = "ȥ���ɣ�";
			loadFriend();
//			Intent intent1 = new Intent();
//			intent1.putExtra("USERID", pUSERID);
//			intent1.putExtra("MOOD", moods);
//			intent1.setClass(FriendListActivity.this, FriendListActivity.class);
//			startActivity(intent1);
			break;
		case Menu.FIRST + 2:
			LayoutInflater layoutInflater= LayoutInflater.from(this);
            final View myMoodView = layoutInflater.inflate(R.layout.dialog_mood, null);           
            Dialog dialog =new AlertDialog.Builder(this)
            .setView(myMoodView)
            .setPositiveButton("����", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                	myMood = ((EditText)myMoodView.findViewById(R.id.myMood)).getText().toString().trim();
                    System.out.println("�Ҹ��ĵ������ǣ�"+myMood);
                    XmppService.changeStateMessage(connection, myMood);
                    myStatusText.setText(myMood);
                }
            })
            .setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                	dialog.cancel();
                 }
            })
            .create();
            dialog.show();
			break;
		case Menu.FIRST + 3:
			Intent intent11 = new Intent();
			intent11.putExtra("USERID", pUSERID);
			intent11.setClass(FriendListActivity.this, FriendAddActivity.class);
			startActivity(intent11);
			break;
		case Menu.FIRST + 4:
			XmppService.deleteAccount(connection);
            Intent exits = new Intent(Intent.ACTION_MAIN);		
            exits.addCategory(Intent.CATEGORY_HOME);
            exits.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exits);
            System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * �����¼�ɾ������
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo){
			
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
			
			int type = ExpandableListView.getPackedPositionType(info.packedPosition);
			
			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				
				int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
				int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
				final FriendInfo dInfo = groupList.get(groupPos).getFriendInfoList().get(childPos);
				final GroupInfo  gInfo = groupList.get(groupPos);
				LayoutInflater layoutInflater= LayoutInflater.from(this);
	            View delFriendView = layoutInflater.inflate(R.layout.dialog_del_friend, null);
	            TextView delname = (TextView)delFriendView.findViewById(R.id.delname);
	            delname.setText(dInfo.getJid());
	            final CheckBox delCheckBox = (CheckBox)delFriendView.findViewById(R.id.delCheckBox);
	            Dialog dialog =new AlertDialog.Builder(this)
	            .setIcon(R.drawable.default_head)
	            .setTitle("ɾ������")
	            .setView(delFriendView)
	            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
	                @Override
	                public void onClick(DialogInterface dialog, int which){
						 XmppService.removeUserFromGroup(dInfo.getJid(), gInfo.getGroupName(), connection);
						 if(delCheckBox.isChecked()){
							 XmppService.removeUser(roster, dInfo.getJid());
						 }
						 Intent intent = new Intent();
               	         intent.putExtra("USERID", pUSERID);
                	     intent.putExtra("fromUserJid", CHECK);
              			 intent.setClass(FriendListActivity.this, FriendListActivity.class);
              			 startActivity(intent); 
	                }
	            })
	            .setNegativeButton("ȡ��", new DialogInterface.OnClickListener(){
	                @Override
	                public void onClick(DialogInterface dialog, int which){
		                	dialog.cancel();
		                }
	            })
	            .create();
	            dialog.show();
			}
		}
	}
}
