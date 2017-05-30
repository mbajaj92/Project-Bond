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
				System.out.println(msg.userId + " " + msg.password);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
