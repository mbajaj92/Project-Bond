package ClientSideCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ServerSideCode.Utils;
import TestCode.Message;

public class NotificationManager extends Thread {
	private String userId;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
	private boolean isAlive;
	private ServerSocket mNotification = null;

	public NotificationManager(String uId) {
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
		System.out.println("Client Receive is ONLINE WAITING");
		try {
			mNotification = new ServerSocket(Utils.CLIENT_PORT_NUMBER);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (isAlive) {
			try {
				Socket test_socket = mNotification.accept();
				System.out.println("Client Receiver got request from Server Yayy ~!!");
				outStream = new ObjectOutputStream(test_socket.getOutputStream());
				inStream = new ObjectInputStream(test_socket.getInputStream());
				Message fromServer = (Message) inStream.readObject();
				if (fromServer.msgType == Message.MSG_TYPE.NOTIFICATION) {
					System.out.println("We got a Spy Ping !!");
					System.out.println("token - " + fromServer.tokens + " users " + fromServer.users);
				} else if (fromServer.msgType == Message.MSG_TYPE.SEARCH) {
					System.out.println("We got a Search Reply!!");
					System.out.println(fromServer.links.split(" ||| "));
				} else
					System.out.println("Unkonwn Message Received");
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				outStream = null;
				inStream = null;
			}
		}
	}
}