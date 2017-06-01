package ServerSideCode;

import java.util.ArrayList;

import ServerSideCode.Utils.QueueObj;
import TestCode.Message;

public class Undeliverables {

	private Object mUTmutex;
	private static Undeliverables mUR = null;
	private ArrayList<Message> mUTQueue;
	private UndeliverablesThread mUThread = null;

	public static Undeliverables getRoutine() {
		if (mUR == null)
			mUR = new Undeliverables();
		return mUR;
	}

	private Undeliverables() {
		mUTQueue = new ArrayList<Message>();
		mUTmutex = new Object();
		mUThread = new UndeliverablesThread();
		mUThread.start();
	}

	public void queueMessage(Message msg) {
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
				boolean flag = false;
				synchronized (mUTmutex) {
					for (int i = 0; i < mUTQueue.size();) {
						Message msg = mUTQueue.get(i);
						if (Utils.isUserOnline(msg.userId)) {
							mUTQueue.remove(i);
							System.out.println("Moving to Push Notification");
							PushNotification.getRoutine().sendMessage(msg);
							flag = true;
						} else
							i++;
					}
					mUTmutex.notifyAll();
				}

				try {
					if (!flag)
						Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}