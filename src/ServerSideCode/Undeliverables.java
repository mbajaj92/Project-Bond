package ServerSideCode;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Undeliverables {

	private Object mUTmutex;
	private static Undeliverables mUR = null;
	private ArrayList<JSONObject> mUTQueue;
	private UndeliverablesThread mUThread = null;

	public static Undeliverables getRoutine() {
		if (mUR == null)
			mUR = new Undeliverables();
		return mUR;
	}

	private Undeliverables() {
		mUTQueue = new ArrayList<JSONObject>();
		mUTmutex = new Object();
		mUThread = new UndeliverablesThread();
		mUThread.start();
	}

	public void queueMessage(JSONObject msg) {
		synchronized (mUTmutex) {
			mUTQueue.add(msg);
			mUTmutex.notifyAll();
		}

		if (mUThread == null || !mUThread.isAlive()) {
			mUThread = new UndeliverablesThread();
			mUThread.start();
		}
	}

	private class UndeliverablesThread extends Thread {

		@Override
		public void run() {
			while (!mUTQueue.isEmpty()) {
				synchronized (mUTmutex) {
					boolean flag = false;
					for (int i = 0; i < mUTQueue.size();) {
						try {
							if (!flag)
								Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						flag = false;
						JSONObject msg = mUTQueue.get(i);
						try {
							if (Utils.isUserOnline(msg.getString(Utils.USER_ID))) {
								mUTQueue.remove(i);
								System.out.println("Moving to Push Notification");
								PushNotification.getRoutine().sendMessage(msg);
								flag = true;
							} else
								i++;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					mUTmutex.notifyAll();
				}
			}
		}
	}
}