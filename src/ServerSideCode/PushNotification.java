package ServerSideCode;

import java.util.ArrayList;

import TestCode.Message;

public class PushNotification {
	private Object mutex;
	private static PushNotification mPN = null;
	private PushNotificationThread mPNThread = null;
	private ArrayList<Message> mQueue;

	public static PushNotification getRoutine() {
		if (mPN == null)
			mPN = new PushNotification();
		return mPN;
	}

	private PushNotification() {
		mQueue = new ArrayList<Message>();
		mutex = new Object();
		mPNThread = new PushNotificationThread();
		mPNThread.start();
	}

	public void sendMessage(Message obj) {
		synchronized (mutex) {
			mQueue.add(obj);
			mutex.notifyAll();
		}

		if (mPNThread == null || !mPNThread.isAlive()) {
			mPNThread = new PushNotificationThread();
			mPNThread.start();
		}
	}

	private class PushNotificationThread extends Thread {

		@Override
		public void run() {
			while (!mQueue.isEmpty()) {
				Message obj = null;
				synchronized (mutex) {
					obj = mQueue.get(0);
					mutex.notifyAll();
				}

				if (obj != null)
					Utils.sendMessage(obj);
			}
		}
	}
}