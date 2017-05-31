package ServerSideCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import TestCode.Message;

public class Server {

	public static void main(String args[]) {
		new Server(Utils.SERVER_PORT_NUMBER);
	}

	Server(int port) {
		try {
			ServerSocket server = new ServerSocket(port);
			while (true) {
				Socket client = server.accept();
				System.out.println("Accepted connection request from " + client.getInetAddress()); // Prints

				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				Message msg = null;
				msg = (Message) in.readObject();

				switch (msg.msgType) {
				case LOGIN:
					/* Actually Authenticate */
					System.out.println("Log In Request for " + msg.userId + " " + msg.password);
					Utils.logUserIn(msg.userId, client.getInetAddress());
					msg.tokens = "WELCOME";
					out.writeObject(msg);
					out.flush();
					break;
				case LOGOFF:
					System.out.println("Log Off Request for " + msg.userId);
					Utils.logUserOff(msg.userId);
					msg.tokens = "BYE!!";
					out.writeObject(msg);
					out.flush();
					break;
				case SEARCH:
					/*
					 * First check for registrations and then Call Abhidnya's
					 * Function
					 */
					SearchRoutine.getRoutine().addSearchRequest(msg.userId, msg.tokens);
					RegisterRoutine.getRoutine().notifySpies(msg.userId, msg.tokens);
					System.out.println("Search Request from " + msg.userId);
					System.out.println("Search for " + msg.tokens);
					break;
				case REGISTER_TOKEN:
					/*
					 * Implement a DB, and then create a Probe for the user to
					 * keep checking for checks
					 */
					RegisterRoutine.getRoutine().register(msg.userId, msg.tokens);
					System.out.println("Spy Request from " + msg.userId);
					System.out.println("Spy Tokens " + msg.tokens);
					break;
				}
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
