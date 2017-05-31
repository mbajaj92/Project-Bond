package ServerSideCode;

import java.util.ArrayList;

public class RegisterRoutine {

	private Object mutex;
	private static RegisterRoutine mSR = null;
	private ArrayList<QueueObj> mQueue;
	private RegisterRoutineThread mRRThread = null;

	private class QueueObj {
		String userID;
		String text;
	}

	public static RegisterRoutine getRoutine() {
		if (mSR == null)
			mSR = new RegisterRoutine();
		return mSR;
	}

	private RegisterRoutine() {
		mQueue = new ArrayList<QueueObj>();
		mutex = new Object();
		mRRThread = new RegisterRoutineThread();
		mRRThread.start();
	}

	public void register(String userId, String token) {
		QueueObj obj = new QueueObj();
		obj.userID = userId;
		obj.text = token;
		synchronized (mutex) {
			mQueue.add(obj);
			mutex.notifyAll();
		}

		if (mRRThread == null || !mRRThread.isAlive()) {
			mRRThread = new RegisterRoutineThread();
			mRRThread.start();
		}
	}

	private class RegisterRoutineThread extends Thread {

		@Override
		public void run() {
			while(!mQueue.isEmpty()) {
				QueueObj obj = null;
				synchronized (mutex) {
					obj = mQueue.get(0);
					mutex.notifyAll();
				}

				if(obj != null) {

				}
			}
		}
	}
}