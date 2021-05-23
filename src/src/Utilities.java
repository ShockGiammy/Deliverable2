package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {
	
	private Utilities() {
	    throw new IllegalStateException("Utility class");
	  }

	
	private static Logger logger = Logger.getLogger(Utilities.class.getName());

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	    	sb.append((char) cp);
	    }
	  	return sb.toString();
	}

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				) {
			String jsonText = readAll(rd);
			return new JSONArray(jsonText);
		} finally {
			is.close();
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			) {
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}
	
	public static void writeFile(String projName, List<VersionInfo> versionInfo) {
		String delimiter = ";";
		String user = "Gian Marco/";
		String path = "C:/Users/" +  user + "Desktop/" + projName+ "_dataset.csv";
		File file;
		file = new File(path);
		if (file.exists())
			logger.log(Level.INFO, "Il file {0} esiste", path);
		else
			try {
				if (file.createNewFile())
					logger.log(Level.INFO, "Il file {0} è stato creato", path);
				else
					logger.log(Level.INFO, "Il file {0} non può essere creato", path);
			} catch (IOException e) {
				e.printStackTrace();
		}
		try (
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				) {
			writer.write("Version name" + delimiter + "File name" + delimiter
					+ "Size" + delimiter 
					+ "NR" + delimiter
					+ "NAuth" + delimiter
					+ "LOCTouched" + delimiter
					+ "LOCAdded" + delimiter
					+ "MAX_LOC_Added" + delimiter
					+ "AVG_LOC_Added" + delimiter 
					+ "Churn" + delimiter 
					+ "MAX_Churn" + delimiter 
					+ "AVG_Churn" + delimiter 
					+ "bugginess" + "\n");
			
			for (int i = 0; i < versionInfo.size(); i++) {
				List<String> classes = versionInfo.get(i).getJavaClasses();
				for (int j = 0; j < classes.size(); j++) {
				
					int avgLoc = 0;
					int avgChurn = 0;
					if (versionInfo.get(i).getRevision(classes.get(j)) != 0 ) {
						avgLoc = Math.round(versionInfo.get(i).getlocAdded(classes.get(j))/(float)versionInfo.get(i).getRevision(classes.get(j)));
						avgChurn = Math.round(versionInfo.get(i).getChurn(classes.get(j))/(float)versionInfo.get(i).getRevision(classes.get(j)));
					}
					
					writer.write(versionInfo.get(i).getVersionName() + delimiter + 
							classes.get(j) + delimiter
							+ versionInfo.get(i).getSize(classes.get(j)) + delimiter
							+ versionInfo.get(i).getRevision(classes.get(j)) + delimiter
							+ versionInfo.get(i).getAuthors(classes.get(j)) + delimiter
							+ versionInfo.get(i).getlocTouched(classes.get(j)) + delimiter
							+ versionInfo.get(i).getlocAdded(classes.get(j)) + delimiter 
							+ versionInfo.get(i).getMAXlocAdded(classes.get(j)) + delimiter 
							+ avgLoc + delimiter 
							+ versionInfo.get(i).getChurn(classes.get(j)) + delimiter 
							+ versionInfo.get(i).getMAXChurn(classes.get(j)) + delimiter 
							+ avgChurn + delimiter 
							+ versionInfo.get(i).getBuggyClass(classes.get(j)) + 
							"\n");
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static boolean deleteDir(File file) {
		if (file.isDirectory()) {
			String[] contenuto = file.list();
			for (int i=0; i<contenuto.length; i++) {
				boolean success = deleteDir(new File(file, contenuto[i]));
				if (!success) { 
					return false; 
				}
			}
	    }
	    return file.delete();
	}
	
	public static void runCommand(String command, Process p) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder text = new StringBuilder();
        text.append(command + "\n");
        while ((line = input.readLine()) != null) {
            text.append(line + "\n");
        }
        logger.log(Level.INFO, "Line: {0}", text);
	}
}
