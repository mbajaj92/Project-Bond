package ServerSideCode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

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

	private static HashMap<String, InetAddress> onlineUsers;
	private static HashMap<String, HashSet<String>> registration;

	public static class QueueObj {
		String userID;
		String text;
		String text2;
		String text3;
	}

	private static void nullCheck() {
		if (onlineUsers == null)
			onlineUsers = new HashMap<String, InetAddress>();

		if (registration == null)
			registration = new HashMap<String, HashSet<String>>();
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
		query = query.replaceAll(" ","%20");
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
		token = token.replaceAll(" ","%20");
		String url = "http://127.0.0.1:5000/Project-Bond/stemmed?query=" + token;
		System.out.println(url);
		String links = "";
		InputStream is = new URL(url).openStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		links = rd.readLine();
		is.close();
		return Arrays.asList(links.split("\\$"));
	}

	public static void notifySpies(String userId, ArrayList<String> tokens) {
		if (registration == null)
			return;

		for (String userID : registration.keySet()) {
			HashSet<String> regList = registration.get(userID);

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

	public static void addGroup(String link[], String name[], String desc[]) throws IOException {
		FileWriter fw = new FileWriter("src/PythonCode/corpus/groupfinder.csv", true);
		for (int i = 0; i < name.length; i++) {
			/*if (name[i] == null)
				continue;*/
			name[i] = name[i].replace(",", " ").trim();
			link[i] = link[i].replace(",", " ").trim();
			desc[i] = desc[i].replace(",", " ").trim();
			System.out.println("Adding "+ name[i] + " , " + link[i] + " , " + desc[i]);
			fw.write("\n" + name[i] + " , " + link[i] + " , " + desc[i]);
		}
		fw.close();
	}

	public static void register(String userId, ArrayList<String> tokens) {
		nullCheck();
		HashSet<String> registeredTokens = registration.get(userId);
		if (registeredTokens == null) {
			registration.put(userId, new HashSet<String>(tokens));
			return;
		}

		synchronized (registeredTokens) {
			registeredTokens.addAll(tokens);
			registeredTokens.notifyAll();
		}
	}
}