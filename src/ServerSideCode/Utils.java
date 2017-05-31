package ServerSideCode;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class Utils {
	public static int SERVER_PORT_NUMBER = 2457; 
	public String returnresults(String query) {
		
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.execfile("src/PythonCode/retriever.py");
		interpreter.set("myquery", query);
		PyObject linkswithseparatorsnotformatted = interpreter.eval("repr(finddocs(myquery))");
		String links = linkswithseparatorsnotformatted.toString();
		links = links.replaceAll("u'|\'$", "");
		return links;
	}
}