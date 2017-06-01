package ServerSideCode;

import java.util.ArrayList;
import java.util.Arrays;

import ServerSideCode.Utils.QueueObj;

public class RegisterRoutine {

	private Object mRRTmutex, mSRTmutex;
	private static RegisterRoutine mSR = null;
	private ArrayList<QueueObj> mRRTQueue;
	private ArrayList<QueueObj> mSRTQueue;
	private RegisterRoutineThread mRRThread = null;
	private SpyResponseThread mSRThread = null;

	public static RegisterRoutine getRoutine() {
		if (mSR == null)
			mSR = new RegisterRoutine();
		return mSR;
	}

	private RegisterRoutine() {
		mRRTQueue = new ArrayList<QueueObj>();
		mSRTQueue = new ArrayList<QueueObj>();
		mRRTmutex = new Object();
		mSRTmutex = new Object();
		mRRThread = new RegisterRoutineThread();
		mRRThread.start();
	}

	public void register(String userId, String token) {
		QueueObj obj = new QueueObj();
		obj.userID = userId;
		obj.text = token;
		synchronized (mRRTmutex) {
			mRRTQueue.add(obj);
			mRRTmutex.notifyAll();
		}

		if (mRRThread == null || !mRRThread.isAlive()) {
			mRRThread = new RegisterRoutineThread();
			mRRThread.start();
		}
	}

	public void notifySpies(String userId, String tokens) {
		QueueObj obj = new QueueObj();
		obj.userID = userId;
		obj.text = tokens;
		synchronized (mSRTmutex) {
			mSRTQueue.add(obj);
			mSRTmutex.notifyAll();
		}

		if (mSRThread == null || !mSRThread.isAlive()) {
			mSRThread = new SpyResponseThread();
			mSRThread.start();
		}
	}

	private class SpyResponseThread extends Thread {
		@Override
		public void run() {
			while (!mSRTQueue.isEmpty()) {
				QueueObj obj = null;
				synchronized (mSRTmutex) {
					obj = mSRTQueue.remove(0);
					mSRTmutex.notifyAll();
				}

				if (obj != null) {
					ArrayList<String> stemmedTokens = new ArrayList<String>(Utils.getStemmed(obj.text));
					Utils.notifySpies(obj.userID, stemmedTokens);
				}
			}
		}
	}

	private class RegisterRoutineThread extends Thread {

		@Override
		public void run() {
			while (!mRRTQueue.isEmpty()) {
				QueueObj obj = null;
				synchronized (mRRTmutex) {
					obj = mRRTQueue.get(0);
					mRRTmutex.notifyAll();
				}

				if (obj != null) {
					ArrayList<String> stemmedTokens = new ArrayList<String>(Utils.getStemmed(obj.text));
					Utils.register(obj.userID, stemmedTokens);
				}
			}
		}
	}
}