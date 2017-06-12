package ServerSideCode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Server {

	public static void main(String args[]) {
		new Server(Utils.SERVER_PORT_NUMBER);
	}

	Server(int port) {
		try {
			ServerSocket server = new ServerSocket(port);
			while (true) {
				Socket client = server.accept();
				System.out.println("Accepted connection request from " + client.getInetAddress());

				DataOutputStream out = new DataOutputStream(client.getOutputStream());
				DataInputStream in = new DataInputStream(client.getInputStream());
				JSONObject packet = null;
				JSONObject reply = new JSONObject();
				packet = new JSONObject(in.readUTF());

				switch (packet.getString(Utils.PACKET_TYPE)) {
				case Utils.ADD_GROUP:
					String groupLink = packet.getString(Utils.GROUP_LINK);
					String groupName = packet.getString(Utils.GROUP_NAME);
					String groupDesc = packet.getString(Utils.GROUP_DESC);
					AddGroupRoutine.getRoutine().addRequest(groupLink, groupName, groupDesc);
					break;
				case Utils.LOGIN:
					/* Actually Authenticate */
					System.out.println("Log In Request for " + packet.getString(Utils.USER_ID) + " "
							+ packet.getString(Utils.PASSWORD));
					Utils.logUserIn(packet.getString(Utils.USER_ID), client.getInetAddress());
					reply.put(Utils.AUTHENTICATION, Utils.WELCOME);
					out.writeUTF(reply.toString());
					out.flush();
					break;
				case Utils.LOGOFF:
					System.out.println("Log Off Request for " + packet.getString(Utils.USER_ID));
					Utils.logUserOff(packet.getString(Utils.USER_ID));
					reply.put(Utils.AUTHENTICATION, "BYE");
					out.writeUTF(reply.toString());
					out.flush();
					break;
				case Utils.SEARCH:
					/* First check for registrations and then Call Abhidnya's
					 * Function */
					System.out.println("Received "+packet.toString());
					SearchRoutine.getRoutine().addSearchRequest(packet.getString(Utils.USER_ID), packet.getString(Utils.SEARCH_QUERY));
					RegisterRoutine.getRoutine().notifySpies(packet.getString(Utils.USER_ID), packet.getString(Utils.SEARCH_QUERY));
					System.out.println("Search Request from " + packet.getString(Utils.USER_ID));
					System.out.println("Search for " + packet.getString(Utils.SEARCH_QUERY));
					break;
				case Utils.REGISTER_TOKEN:
					/*
					 * Implement a DB, and then create a Probe for the user to
					 * keep checking for checks
					 */
					RegisterRoutine.getRoutine().register(packet.getString(Utils.USER_ID), packet.getString(Utils.TOKENS));
					System.out.println("Spy Request from " + packet.getString(Utils.USER_ID));
					System.out.println("Spy Tokens " + packet.getString(Utils.TOKENS));
					break;
				}
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
