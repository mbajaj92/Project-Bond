package ClientSideCode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import ServerSideCode.Utils;

public class Client {

	private Scanner scanner;
	private String userId = null;
	private Socket test_socket;
	private DataOutputStream out;
	private DataInputStream in;

	public static void main(String args[]) {
		new Client();
	}

	Client() {
		try {
			String servername = "172.20.10.3";
			int port = Utils.SERVER_PORT_NUMBER;
			System.out.println("Enter the user id");
			scanner = new Scanner(System.in);
			userId = scanner.nextLine();

			System.out.println("Enter the Password");
			String password = scanner.nextLine();

			JSONObject packet = new JSONObject();
			packet.put(Utils.PACKET_TYPE, Utils.LOGIN);
			packet.put(Utils.USER_ID, userId);
			packet.put(Utils.PASSWORD, password);

			test_socket = new Socket(servername, port);
			out = new DataOutputStream(test_socket.getOutputStream());
			in = new DataInputStream(test_socket.getInputStream());
			out.writeUTF(packet.toString());
			out.flush();
			packet = new JSONObject(in.readUTF());
			if (packet.getString(Utils.AUTHENTICATION).equals(Utils.WELCOME))
				startLoop(servername, port);

			System.out.println("Time for Client to Die");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void startLoop(String servername, int port) throws IOException, ClassNotFoundException {
		boolean logout = false;
		NotificationManager nManager = new NotificationManager(userId);
		nManager.start();
		System.out.println("Enter a choice \n1.Do a Search\n2.Register Keyword For Spying\n3.Log Out");
		int choice = scanner.nextInt();
		scanner.nextLine();
		JSONObject json = null;
		// msg = null;
		try {
			while (choice > 0 && choice <= 3) {
				json = new JSONObject();
				json.put(Utils.USER_ID, userId);
				// msg.userId = userId;
				switch (choice) {
				case 1:
					json.put(Utils.PACKET_TYPE, Utils.SEARCH);

					System.out.println("Enter the search query");
					json.put(Utils.SEARCH_QUERY, scanner.nextLine());
					test_socket = new Socket(servername, port);
					out = new DataOutputStream(test_socket.getOutputStream());
					in = new DataInputStream(test_socket.getInputStream());
					out.writeUTF(json.toString());
					out.flush();
					break;
				case 2:
					json.put(Utils.PACKET_TYPE, Utils.REGISTER_TOKEN);

					System.out.println("Enter the tokens, seperated by commas");
					json.put(Utils.TOKENS, scanner.nextLine());
					test_socket = new Socket(servername, port);
					out = new DataOutputStream(test_socket.getOutputStream());
					in = new DataInputStream(test_socket.getInputStream());
					out.writeUTF(json.toString());
					out.flush();
					break;
				case 3:

					json.put(Utils.PACKET_TYPE, Utils.LOGOFF);

					test_socket = new Socket(servername, port);
					out = new DataOutputStream(test_socket.getOutputStream());
					in = new DataInputStream(test_socket.getInputStream());
					out.writeUTF(json.toString());
					out.flush();
					json = new JSONObject(in.readUTF());
					nManager.stopListening();
					logout = true;
					return;
				}
				System.out.println("Enter a choice \n1.Do a Search\n2.Register Keyword For Spying\n3.Log Out");
				choice = scanner.nextInt();
				scanner.nextLine();
			}
		} catch (ConnectException e) {
			if(!logout)
				e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}