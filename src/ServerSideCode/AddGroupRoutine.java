package ServerSideCode;

import java.io.IOException;
import java.util.ArrayList;

import ServerSideCode.Utils.QueueObj;

public class AddGroupRoutine {

	private Object mutex;
	private static AddGroupRoutine mAGR = null;
	private ArrayList<QueueObj> mQueue;
	private AddGroupRoutineThread mAGRThread = null;

	public static AddGroupRoutine getRoutine() {
		if (mAGR == null)
			mAGR = new AddGroupRoutine();
		return mAGR;
	}

	private AddGroupRoutine() {
		mQueue = new ArrayList<QueueObj>();
		mutex = new Object();
		mAGRThread = new AddGroupRoutineThread();
		mAGRThread.start();
	}

	public void addRequest(String groupLink, String groupName, String groupDesc) {
		QueueObj obj = new QueueObj();
		obj.text = groupLink;
		obj.text2 = groupName;
		obj.text3 = groupDesc;

		synchronized (mutex) {
			mQueue.add(obj);
			mutex.notifyAll();
		}

		System.out.println("Adding Group Add Req");
		if (mAGRThread == null || !mAGRThread.isAlive()) {
			mAGRThread = new AddGroupRoutineThread();
			mAGRThread.start();
		}
	}

	private class AddGroupRoutineThread extends Thread {

		@Override
		public void run() {
			while (!mQueue.isEmpty()) {
				QueueObj obj = null;
				String[] link = null, name = null, desc = null;
				synchronized (mutex) {
					link = new String[mQueue.size()];
					name = new String[mQueue.size()];
					desc = new String[mQueue.size()];
					int i = 0;
					while (!mQueue.isEmpty()) {
						obj = mQueue.remove(0);
						link[i] = obj.text;
						name[i] = obj.text2;
						desc[i] = obj.text3;
						i++;
					}
					mutex.notifyAll();
				}

				if (name != null && link != null && desc != null) {
					boolean done = false;
					while (!done) {
						try {
							Utils.addGroup(link, name, desc);
							System.out.println("Done");
							done = true;
						} catch (IOException e) {
							try {
								System.out.println("Going to Sleep !!");
								Thread.sleep(2500);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
}