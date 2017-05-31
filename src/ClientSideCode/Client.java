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
	private String userId = null;
	private Socket test_socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public static void main(String args[]) {
		new Client();
	}

	Client() {
		try {
			String servername = "localhost";
			int port = Utils.SERVER_PORT_NUMBER;
			System.out.println("Enter the user id");
			scanner = new Scanner(System.in);
			userId = scanner.nextLine();

			System.out.println("Enter the Password");
			String password = scanner.nextLine();

			Message msg = new Message();
			msg.msgType = Message.MSG_TYPE.LOGIN;
			msg.userId = userId;
			msg.password = password;
			test_socket = new Socket(servername, port);
			out = new ObjectOutputStream(test_socket.getOutputStream());
			in = new ObjectInputStream(test_socket.getInputStream());
			out.writeObject(msg);
			out.flush();
			msg = (Message) in.readObject();
			if (msg.tokens.equals("WELCOME"))
				startLoop(servername, port);
			System.out.println("Time for Client to Die");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void startLoop(String servername, int port) throws IOException, ClassNotFoundException {
		System.out.println("Enter a choice \n1.Do a Search\n2.Register Keyword For Spying\n3.Log Out");
		int choice = scanner.nextInt();
		scanner.nextLine();
		Message msg = null;
		while (choice > 0 && choice <= 3) {
			msg = new Message();
			msg.userId = userId;
			switch (choice) {
			case 1:
				msg.msgType = Message.MSG_TYPE.SEARCH;

				System.out.println("Enter the search query");
				msg.tokens = scanner.nextLine();
				test_socket = new Socket(servername, port);
				out = new ObjectOutputStream(test_socket.getOutputStream());
				in = new ObjectInputStream(test_socket.getInputStream());
				out.writeObject(msg);
				out.flush();
				break;
			case 2:
				msg.msgType = Message.MSG_TYPE.REGISTER_TOKEN;

				System.out.println("Enter the tokens, seperated by commas");
				msg.tokens = scanner.nextLine();
				test_socket = new Socket(servername, port);
				out = new ObjectOutputStream(test_socket.getOutputStream());
				in = new ObjectInputStream(test_socket.getInputStream());
				out.writeObject(msg);
				out.flush();
				break;
			case 3:
				msg.msgType = Message.MSG_TYPE.LOGOFF;
				test_socket = new Socket(servername, port);
				out = new ObjectOutputStream(test_socket.getOutputStream());
				in = new ObjectInputStream(test_socket.getInputStream());
				out.writeObject(msg);
				out.flush();
				msg = (Message) in.readObject();
				return;
			}
			System.out.println("Enter a choice \n1.Do a Search\n2.Register Keyword For Spying\n3.Log Out");
			choice = scanner.nextInt();
			scanner.nextLine();
		}
	}
}