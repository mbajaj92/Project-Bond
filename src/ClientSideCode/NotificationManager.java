package ClientSideCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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
		System.out.println("Notification Manager is Online");
		try {
			mNotification = new ServerSocket(Utils.CLIENT_PORT_NUMBER);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (isAlive) {
			try {
				Socket test_socket = mNotification.accept();
				outStream = new ObjectOutputStream(test_socket.getOutputStream());
				inStream = new ObjectInputStream(test_socket.getInputStream());
				Message fromServer = (Message) inStream.readObject();
				if (fromServer.msgType == Message.MSG_TYPE.NOTIFICATION) {
					System.out.println("We got a Spy Ping !!");
					System.out.println("token - " + fromServer.tokens + " users " + fromServer.users);
				} else if (fromServer.msgType == Message.MSG_TYPE.SEARCH) {
					System.out.println("We got a Search Reply!!");
					String links[] = fromServer.links.split("$");
					for (int i = 0; i < links.length; i++)
						System.out.println(links[i]);

				} else
					System.out.println("Unkonwn Message Received");
			} catch (SocketException se) {
				System.out.println("Asked to Kill Notification Manager !");
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				outStream = null;
				inStream = null;
			}
		}
	}
}