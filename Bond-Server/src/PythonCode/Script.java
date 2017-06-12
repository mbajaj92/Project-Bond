package PythonCode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;

public class Script {
	public static void main(String args[]) {
		String csvFile = "src/PythonCode/corpus/groupfinder.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			int fileCount = 0;
			JSONObject json = new JSONObject();
			while ((line = br.readLine()) != null) {
				if (fileCount != 0) {
					String fileContent = "";
					String[] content = line.split(cvsSplitBy);
					fileContent = content[0] + "  " + content[2];
					String groupLink = content[1];
					PrintWriter writer = new PrintWriter("src/PythonCode/corpus/0/" + fileCount, "UTF-8");
					writer.println(fileContent);
					writer.close();
					json.put("0/" + fileCount, groupLink);
				}
				fileCount++;
			}

			PrintWriter writer = new PrintWriter("src/PythonCode/corpus/book_keeping.json", "UTF-8");
		    writer.println(json.toString());
		    writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			System.out.println("I am here 4");
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("I am here 6");
	}
}
