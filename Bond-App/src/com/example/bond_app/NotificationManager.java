package com.example.bond_app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

import ServerSideCode.Utils;
import android.os.Handler;
import android.os.Message;

public class NotificationManager extends Thread {
	private String userId;
	private DataOutputStream outStream;
	private DataInputStream inStream;
	private boolean isAlive;
	private ServerSocket mNotification = null;
	private Handler handler ;

	public NotificationManager(String uId, Handler handlerh) {
		handler = handlerh;
		userId = uId;
		isAlive = true;
	}

	public void stopListening() {
		try {
			mNotification.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		isAlive = false;
	}

	@Override
	public void run() {
		System.out.println("Notification Manager is Online");
		try {
			mNotification = new ServerSocket(Utils.CLIENT_PORT_NUMBER);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (isAlive) {
			try {
				Socket test_socket = mNotification.accept();
				outStream = new DataOutputStream(test_socket.getOutputStream());
				inStream = new DataInputStream(test_socket.getInputStream());
				JSONObject fromServ = new JSONObject(inStream.readUTF());

				if (fromServ.getString(Utils.PACKET_TYPE).equals(Utils.NOTIFICATION)) {
					System.out.println("We got a Spy Ping !!");
					String abc = "token - " + fromServ.getString(Utils.TOKENS) + " users "
							+ fromServ.getString(Utils.USERS_LIST);
					System.out.println(abc);
					Message msg = Message.obtain();
					msg.what = 2;
					msg.obj = abc;
					handler.sendMessage(msg);
				} else if (fromServ.getString(Utils.PACKET_TYPE).equals(Utils.SEARCH)) {
					System.out.println("We got a Search Reply!!");
					String xyz ="";
					String links[] = fromServ.getString(Utils.LINKS).split("\\$");
					for (int i = 0; i < links.length; i++){
						xyz += links[i] + "\n";
						System.out.println(links[i]);
					}
					Message msg = Message.obtain();
					msg.what = 1;
					msg.obj = xyz;
					handler.sendMessage(msg);
						
				} else
					System.out.println("Unkonwn Message Received");
			} catch (SocketException se) {
				System.out.println("Asked to Kill Notification Manager !");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				outStream = null;
				inStream = null;
			}
		}
	}
}