package TestCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestClient {
	public static void main(String args[]) {
		String servername = "172.20.10.3";
		int port = 2457;
		Message msg = new Message();
		msg.msgType = Message.MSG_TYPE.REGISTER_TOKEN;
		msg.token = "Hello World!";
		Socket test_socket;
		try {
			test_socket = new Socket(servername, port);
			System.out.println("Client: Connection Established");
			ObjectOutputStream outputStream = new ObjectOutputStream(test_socket.getOutputStream());
			System.out.println("Client: We have output Stream");
			ObjectInputStream inputStream = new ObjectInputStream(test_socket.getInputStream());
			System.out.println("Client: We have input Stream");
			outputStream.writeObject(msg);
			outputStream.flush();
			System.out.println("Client: We have written a message");
			msg = (Message) inputStream.readObject();
		} catch (IOException e) {
			System.out.println("CLIENT 1: " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}