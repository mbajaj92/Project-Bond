package TestCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {

	public static void main(String args[]) {
		(new ServerImpl(2457)).start();
	}

	private static class ServerImpl extends Thread {
		ServerSocket server = null;
		Socket client = null;

		ServerImpl(int port) {
			try {
				server = new ServerSocket(port);
			} catch (Exception E) {
				System.out.println(E);
			}
		}

		@Override
		public void run() {
			System.out.println("Server is UP");
			while (true) {
				try {
					client = server.accept();
					Thread.sleep(2000);
					System.out.println("SERVER: We have accepted a client");
					ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(client.getInputStream());
					System.out.println("SERVER: We have input Stream");
					Message msg = (Message) in.readObject();
					System.out.println("SERVER: We have a Message");
					System.out.println("SERVER: We have recieved the message " + msg.tokens);
				} catch (IOException e) {
					System.out.println("SERVER 1: "+e);
				} catch (ClassNotFoundException e) {
					System.out.println("SERVER 2: "+e);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}