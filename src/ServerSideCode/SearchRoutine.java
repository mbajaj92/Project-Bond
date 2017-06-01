package ServerSideCode;

import java.io.IOException;
import java.util.ArrayList;

import ServerSideCode.Utils.QueueObj;
import TestCode.Message;

public class SearchRoutine {

	private Object mutex;
	private static SearchRoutine mSR = null;
	private ArrayList<QueueObj> mQueue;
	private SearchRoutineThread mSRThread = null;

	public static SearchRoutine getRoutine() {
		if (mSR == null)
			mSR = new SearchRoutine();
		return mSR;
	}

	private SearchRoutine() {
		mQueue = new ArrayList<QueueObj>();
		mutex = new Object();
		mSRThread = new SearchRoutineThread();
		mSRThread.start();
	}

	public void addSearchRequest(String userId, String query) {
		QueueObj obj = new QueueObj();
		obj.userID = userId;
		obj.text = query;
		synchronized (mutex) {
			mQueue.add(obj);
			mutex.notifyAll();
		}

		System.out.println("Adding Search Req");
		if (mSRThread == null || !mSRThread.isAlive()) {
			mSRThread = new SearchRoutineThread();
			mSRThread.start();
		}
	}

	private class SearchRoutineThread extends Thread {

		@Override
		public void run() {
			while (!mQueue.isEmpty()) {
				QueueObj obj = null;
				synchronized (mutex) {
					obj = mQueue.remove(0);
					mutex.notifyAll();
				}

				if (obj != null) {
					Message msg = new Message();
					try {
						msg.links = Utils.returnResults(obj.text);
						msg.msgType = Message.MSG_TYPE.SEARCH;
						msg.userId = obj.userID;
						PushNotification.getRoutine().sendMessage(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}