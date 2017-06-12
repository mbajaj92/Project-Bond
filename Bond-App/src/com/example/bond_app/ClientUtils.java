package com.example.bond_app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONObject;

public class ClientUtils {
	public static final String GROUP_NAME = "group_name";
	public static final String GROUP_LINK = "group_link";
	public static final String GROUP_DESC = "group_desc";
	public static final String ADD_GROUP = "add_group";
	public static final String SEARCH_QUERY = "search_query";
	public static final String WELCOME = "welcome";
	public static final String AUTHENTICATION = "authentication";
	public static final String PASSWORD = "password";
	public static final String USER_ID = "user_id";
	public static final String USERS_LIST = "users_list";
	public static final String TOKENS = "tokens";
	public static final String LOGIN = "login";
	public static final String REGISTER_TOKEN = "register_token";
	public static final String SEARCH = "search";
	public static final String LINKS = "links";
	public static final String LOGOFF = "log_off";
	public static final String NOTIFICATION = "notification";
	public static final String PACKET_TYPE = "Packet_Type";
	public static final int SERVER_PORT_NUMBER = 2457;
	public static final int CLIENT_PORT_NUMBER = 5724;

	public static Socket test_socket;
	public static DataOutputStream out;
	public static DataInputStream in;
	public static final String SERVER_NAME = "192.168.0.30";

	public static void sendmessage(JSONObject message) {
		try{
			test_socket = new Socket(SERVER_NAME, SERVER_PORT_NUMBER);
			out = new DataOutputStream(test_socket.getOutputStream());
			in = new DataInputStream(test_socket.getInputStream());
			out.writeUTF(message.toString());
			out.flush();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
