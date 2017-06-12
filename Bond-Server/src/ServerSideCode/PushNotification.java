package ServerSideCode;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class PushNotification {
	private Object mutex;
	private static PushNotification mPN = null;
	private PushNotificationThread mPNThread = null;
	private ArrayList<JSONObject> mQueue;

	public static PushNotification getRoutine() {
		if (mPN == null)
			mPN = new PushNotification();
		return mPN;
	}

	private PushNotification() {
		mQueue = new ArrayList<JSONObject>();
		mutex = new Object();
		mPNThread = new PushNotificationThread();
		mPNThread.start();
	}

	public void sendMessage(JSONObject obj) {
		synchronized (mutex) {
			mQueue.add(obj);
			mutex.notifyAll();
		}

		System.out.println("Adding Notification Req");
		if (mPNThread == null || !mPNThread.isAlive()) {
			mPNThread = new PushNotificationThread();
			mPNThread.start();
		}
	}

	private class PushNotificationThread extends Thread {

		@Override
		public void run() {
			while (!mQueue.isEmpty()) {
				JSONObject obj = null;
				synchronized (mutex) {
					obj = mQueue.remove(0);
					mutex.notifyAll();
				}

				boolean sent = true;
				if (obj != null) {
					System.out.println("Notification Sent");
					sent = Utils.sendMessage(obj);
				}

				if (!sent) {
					System.out.println("Moving to Undeliverable");
					try {
						Utils.logUserOff(obj.getString(Utils.USER_ID));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Undeliverables.getRoutine().queueMessage(obj);
				}
			}
		}
	}
}