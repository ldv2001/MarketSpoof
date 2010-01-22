package nu.vegeby.android.MarketSpoof;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.os.SystemClock;

public class MarketSpoofProvider extends AppWidgetProvider {
	private final String APP = "MSW";
	private TelephonyManager wtm = null;
	private Boolean wBeenRun = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		int simState;
		Log.i(APP, "onReceive() -> Boottime: " + bootTimeMillis());
		super.onReceive(context, intent);
		wtm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		simState = wtm.getSimState();
		if (intent.getAction().equals("nu.vegeby.android.MarketSpoof.CLICK")) {
			if (!wBeenRun && (simState != TelephonyManager.SIM_STATE_ABSENT) && (simState != TelephonyManager.SIM_STATE_READY)) {
				Toast.makeText(context, "Wait for phone to register on network", Toast.LENGTH_SHORT).show();
			} else {
				checkPrefs(context);
				Boolean neu;
				if (getState(context)) {
					neu = false;
				} else {
					neu = true;
				}
				AppWidgetManager mgr = AppWidgetManager.getInstance(context);
				int[] appWidgetIds = mgr.getAppWidgetIds(new ComponentName(context.getPackageName(), MarketSpoofProvider.class.getName()));
				final int N = appWidgetIds.length;
				for (int i = 0; i < N; i++) {
					int[] appWidgetId = appWidgetIds; 
					RemoteViews views = getRV(context, neu);
					mgr.updateAppWidget(appWidgetId, views);
				}
				setState(context, neu);
				// Write here your button click code
				//Toast.makeText(context, "It works!!" + neu, Toast.LENGTH_SHORT).show();
				
			}
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
		Log.i(APP, "onUpdate() -> Boottime: " + bootTimeMillis());
		
		RemoteViews views = getRV(context, getState(context));
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int[] appWidgetId = appWidgetIds; 
			mgr.updateAppWidget(appWidgetId, views);
		}
		/*
		 * for (int i = 0; i < N; i++) { int[] appWidgetId = appWidgetIds;
		 * RemoteViews views = new RemoteViews(context.getPackageName(),
		 * R.layout.widget); Intent clickintent = new
		 * Intent("nu.vegeby.android.MarketSpoof.CLICK"); PendingIntent
		 * pendingIntentClick = PendingIntent.getBroadcast(context, 0,
		 * clickintent, 0); views.setOnClickPendingIntent(R.id.widget_button,
		 * pendingIntentClick); mgr.updateAppWidget(appWidgetId, views); }
		 */

	}
	
	public RemoteViews getRV(Context context, Boolean active) {
		int image;
//		String text = new String();
		if (active) {
//			text = "ON";
			image = R.drawable.msbtnon;
			run("setprop gsm.sim.operator.numeric " + getFakeNetNum(context)+ "\nkill $(ps | grep vending | tr -s ' ' | cut -d ' ' -f2)\ncd /data/data/com.android.vending/shared_prefs/\ncp vending_preferences.xml vending_preferences.xml.orig\ncat vending_preferences.xml.orig  | sed 's/paid_apps_enabled\\\" value=\\\"false\\\"/paid_apps_enabled\\\" value=\\\"true\\\"/' > vending_preferences.xml");
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (!tm.getSimOperator().equals(getFakeNetNum(context))) {
				active = false;
				image = R.drawable.msbtnoff;
			}
		} else {
//			text = "OFF";
			image = R.drawable.msbtnoff;
			run("setprop gsm.sim.operator.numeric " + getNetNum(context) + "\nkill $(ps | grep vending | tr -s ' ' | cut -d ' ' -f2)"); 
		}
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		Intent clickintent = new Intent("nu.vegeby.android.MarketSpoof.CLICK");
		PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, clickintent, 0);
		views.setOnClickPendingIntent(R.id.widget_button, pendingIntentClick);
//		views.setTextViewText(R.id.widget_button, text);
		views.setImageViewResource(R.id.widget_button, image);
		return views;
	}
	
	@Override
	public void onEnabled(Context context) {
		Log.i(APP, "onEnabled() -> Boottime: " + bootTimeMillis());
		super.onEnabled(context);
	}
	
	@Override
	public void onDisabled(Context context) {
		Log.i(APP, "onDisabled() -> Boottime: " + bootTimeMillis());
		super.onDisabled(context);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(APP, "onDeleted() -> Boottime: " + bootTimeMillis());
		super.onDeleted(context, appWidgetIds);
	}
	
	public long bootTimeMillis() {
		Log.i(APP, "bootTimeMillis()");
		return System.currentTimeMillis() - SystemClock.elapsedRealtime();
	}
	
	private Boolean getState(Context context) {
		Log.i(APP, "getState() -> Boottime: " + bootTimeMillis());
		SharedPreferences sp = context.getSharedPreferences("settings", 0);
		return sp.getBoolean("state", false);
	}
	
	private String getNetNum(Context context) {
		Log.i(APP, "getNetNum() -> Boottime: " + bootTimeMillis());
		SharedPreferences sp = context.getSharedPreferences("settings", 0);
		return sp.getString("netnum", "");
	}
	
	private String getFakeNetNum(Context context) {
		Log.i(APP, "getFakeNetNum() -> Boottime: " + bootTimeMillis());
		SharedPreferences sp = context.getSharedPreferences("settings", 0);
		return sp.getString("fakenetnum", "20416");
	}
	
	private long getBootTime(Context context) {
		Log.i(APP, "getBootTime() -> Boottime: " + bootTimeMillis());
		SharedPreferences sp = context.getSharedPreferences("settings", 0);
		return sp.getLong("boottime", 0);
	}
	
	private void setState(Context context, Boolean state) {
		Log.i(APP, "setState() -> Boottime: " + bootTimeMillis());
		SharedPreferences.Editor spe = context.getSharedPreferences("settings", 0).edit();
		spe.putBoolean("state", state);
		spe.commit();
	}
	
	private void setNetNum(Context context, String netnum) {
		Log.i(APP, "setNetNum() -> Boottime: " + bootTimeMillis());
		SharedPreferences.Editor spe = context.getSharedPreferences("settings", 0).edit();
		spe.putString("netnum", netnum);
		spe.commit();
	}
	
	private void setFakeNetNum(Context context, String fakenetnum) {
		Log.i(APP, "setFakeNetNum() -> Boottime: " + bootTimeMillis());
		SharedPreferences.Editor spe = context.getSharedPreferences("settings", 0).edit();
		spe.putString("fakenetnum", fakenetnum);
		spe.commit();
	}
	
	private void setBootTime(Context context, long boottime) {
		Log.i(APP, "setBootTime() -> Boottime: " + bootTimeMillis());
		SharedPreferences.Editor spe = context.getSharedPreferences("settings", 0).edit();
		spe.putLong("boottime", boottime);
		spe.commit();
	}
	
	private void checkPrefs(Context context) {
		Log.i(APP, "checkPrefs() -> " + (bootTimeMillis() - getBootTime(context)));
		long penis = (getBootTime(context) - bootTimeMillis());
		if ((penis < -50) || (penis > 50)) {
			Log.i(APP, "checkPrefs() -> Fixing Prefs -> Boottime: " + bootTimeMillis());
			setBootTime(context, bootTimeMillis());
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			setNetNum(context, tm.getSimOperator());
		}
	}
	public void run(String command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			e.printStackTrace();
		}
		DataOutputStream os = new DataOutputStream(process.getOutputStream());

		try { os.writeBytes(command + "\n"); } catch (IOException e) { e.printStackTrace(); }
   		try { os.flush(); } catch (IOException e) { e.printStackTrace(); }

		try { os.writeBytes("exit\n"); } catch (IOException e) { e.printStackTrace(); }
		try { os.flush(); } catch (IOException e) { e.printStackTrace(); }
		try { process.waitFor(); } catch (InterruptedException e) { e.printStackTrace(); }		
	}

}
