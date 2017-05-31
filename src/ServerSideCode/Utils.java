package ServerSideCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.modules.synchronize;
import org.python.util.PythonInterpreter;

import ClientSideCode.NotificationManager;
import TestCode.Message;

public class Utils {

	public static int SERVER_PORT_NUMBER = 2457;
	public static int CLIENT_PORT_NUMBER = 5724;

	private static HashMap<String, InetAddress> onlineUsers;
	private static HashMap<String, ArrayList<String>> registration;

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

	public static void sendMessage(Message message) {
		try {
			Socket socket = new Socket(Utils.getIPForUser(message.userId), Utils.CLIENT_PORT_NUMBER);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(message);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String returnResults(String query) {
		PythonInterpreter interpreter = new PythonInterpreter(null, new PySystemState());
		PySystemState sys = interpreter.getSystemState();
		interpreter.execfile("src/PythonCode/retriever.py");
		interpreter.set("myquery", query);
		PyObject linkswithseparatorsnotformatted = interpreter.eval("repr(finddocs(myquery))");
		String links = linkswithseparatorsnotformatted.toString();
		links = links.replaceAll("u'|\'$", "");
		return links;
	}

	public static List<String> getStemmed(String token) {
		PythonInterpreter interpreter = new PythonInterpreter(null, new PySystemState());
		PySystemState sys = interpreter.getSystemState();
		//sys.path.append(new PyString("C:\\Python27\\Lib\\site-packages;"));
		interpreter.execfile("src/PythonCode/retriever.py");
		interpreter.set("mytoken", token);
		PyObject stemmednotformatted = interpreter.eval("repr(stem(myquery))");
		String reply = stemmednotformatted.toString();
		return Arrays.asList(reply.split(" ||| "));
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
			registration.notifyAll();
			return;
		}

		synchronized (registeredTokens) {
			registeredTokens.addAll(tokens);
			registeredTokens.notifyAll();
		}
	}
}