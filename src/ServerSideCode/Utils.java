package ServerSideCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import TestCode.Message;

public class Utils {

	public static int SERVER_PORT_NUMBER = 2457;
	public static int CLIENT_PORT_NUMBER = 5724;

	private static HashMap<String, InetAddress> onlineUsers;

	private static void nullCheck() {
		if (onlineUsers == null)
			onlineUsers = new HashMap<String, InetAddress>();
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
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.execfile("src/PythonCode/Retriever.py");
		interpreter.set("myquery", query);
		PyObject linkswithseparatorsnotformatted = interpreter.eval("repr(finddocs(myquery))");
		String links = linkswithseparatorsnotformatted.toString();
		links = links.replaceAll("u'|\'$", "");
		return links;
	}

public static ArrayList<String> getStemmed(String token) {
	PythonInterpreter interpreter = new PythonInterpreter();
	interpreter.execfile("src/PythonCode/Retriever.py");
	interpreter.set("mytoken", token);
	PyObject stemmednotformatted = interpreter.eval("repr(stem(myquery))");
	String reply = stemmednotformatted.toString();
	return (ArrayList<String>) Arrays.asList(reply.split(" ||| "));
}
}