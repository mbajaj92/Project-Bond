package ServerSideCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

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

	public static boolean sendMessage(JSONObject message) {
		try {

			if (!isUserOnline(message.getString(Utils.USER_ID)))
				return false;

			Socket socket = new Socket(Utils.getIPForUser(message.getString(Utils.USER_ID)), Utils.CLIENT_PORT_NUMBER);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			out.writeUTF(message.toString());
			out.flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static String performSearch(String query) throws IOException {
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
				JSONObject reply = new JSONObject();
				try {
					reply.put(Utils.PACKET_TYPE, Utils.NOTIFICATION);
					reply.put(Utils.USER_ID, userID);
					reply.put(Utils.USERS_LIST, userId);
					reply.put(Utils.TOKENS, tokens.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				PushNotification.getRoutine().sendMessage(reply);
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