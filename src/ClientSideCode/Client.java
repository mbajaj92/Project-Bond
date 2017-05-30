package ClientSideCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import ServerSideCode.Utils;
import TestCode.Message;

public class Client {

	private Scanner scanner;
	private Socket test_socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public static void main(String args[]) {
		new Client();
	}

	Client() {
		try {
			String servername = "localhost";
			int port = Utils.SERVER_PORT_NUMBER;
			System.out.println("Enter the user id");
			scanner = new Scanner(System.in);
			String user_id = scanner.nextLine();

			System.out.println("Enter the Password");
			String password = scanner.nextLine();

			Message msg = new Message();
			msg.msgType = Message.MSG_TYPE.LOGIN;
			msg.userId = user_id;
			msg.password = password;
			test_socket = new Socket(servername, port);
			outputStream = new ObjectOutputStream(test_socket.getOutputStream());
			inputStream = new ObjectInputStream(test_socket.getInputStream());
			outputStream.writeObject(msg);
			outputStream.flush();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}