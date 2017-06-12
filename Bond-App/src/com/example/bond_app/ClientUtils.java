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
	public static final String USER_ID_CLIENT = "USER_ID";
	public static final String ADD_GROUP = "add_group";
	public static Socket test_socket;
	public static DataOutputStream out;
	public static DataInputStream in;
	public static final String SERVER_NAME = "192.168.0.30";
	public static final int SERVER_PORT_NUMBER = 2457;
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
