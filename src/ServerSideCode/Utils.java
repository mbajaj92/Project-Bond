package ServerSideCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import TestCode.Message;

public class Utils {

	public static int SERVER_PORT_NUMBER = 2457;
	public static int CLIENT_PORT_NUMBER = 5724;

	private static HashMap<String, InetAddress> onlineUsers;
	private static HashMap<String, ArrayList<String>> registration;

	public static class QueueObj {
		String userID;
		String text;
	}

	private static void nullCheck() {
		if (onlineUsers == null)
			onlineUsers = new HashMap<String, InetAddress>();

		if (registration == null)
			registration = new HashMap<String, ArrayList<String>>();
	}

	public static boolean isUserOnline(String id) {
		return (onlineUsers != null && onlineUsers.containsKey(id));
	}

	public static void logUserOff(String id) {
		nullCheck();
		onlineUsers.remove(id);
	}

	public static void logUserIn(String id, InetAddress ip) {
		nullCheck();
		onlineUsers.put(id, ip);
	}

	public static InetAddress getIPForUser(String id) {
		nullCheck();
		return onlineUsers.get(id);
	}

	public static boolean sendMessage(Message message) {
		try {

			if (!isUserOnline(message.userId))
				return false;

			Socket socket = new Socket(Utils.getIPForUser(message.userId), Utils.CLIENT_PORT_NUMBER);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(message);
			out.flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static String returnResults(String query) throws IOException {
		String url = "http://127.0.0.1:5000/Project-Bond/scavenge?query=" + query;
		String links = "";
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			links = rd.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		return links;
	}

	public static List<String> getStemmed(String token) throws IOException {
		String url = "http://127.0.0.1:5000/Project-Bond/stemmed?query=" + token;
		String links = "";
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			links = rd.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		return Arrays.asList(links.split("$"));
	}

	public static void notifySpies(String userId, ArrayList<String> tokens) {
		if (registration == null)
			return;

		for (String userID : registration.keySet()) {
			ArrayList<String> regList = registration.get(userID);

			synchronized (regList) {
				tokens.retainAll(regList);
				regList.notifyAll();
			}

			if (!tokens.isEmpty()) {
				Message msg = new Message();
				msg.msgType = Message.MSG_TYPE.NOTIFICATION;
				msg.userId = userID;
				msg.users = userId;
				msg.tokens = tokens.toString();
				PushNotification.getRoutine().sendMessage(msg);
			}
		}
	}

	public static void register(String userId, ArrayList<String> tokens) {
		nullCheck();
		ArrayList<String> registeredTokens = registration.get(userId);
		if (registeredTokens == null) {
			registration.put(userId, tokens);
			return;
		}

		synchronized (registeredTokens) {
			registeredTokens.addAll(tokens);
			registeredTokens.notifyAll();
		}
	}
}