package com.easyim.android.util;

import com.easyim.android.xmppmanager.XmppConnection;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * ������
 */
public class Utils {
	/**
	 * ����jid��ȡ�û���
	 */
	public static String getJidToUsername(String jid){
		return jid.split("@")[0];
	}
	
	public static String getUserNameToJid(String username){
		return username + "@" + XmppConnection.SERVER_NAME;
	}
	
	public static int getScreenWidth(Context context) {
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}

	public static int getScreenHeight(Context context) {
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}

	public static float getScreenDensity(Context context) {
		try {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager manager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			manager.getDefaultDisplay().getMetrics(dm);
			return dm.density;
		} catch (Exception ex) {

		}
		return 1.0f;
	}
}
