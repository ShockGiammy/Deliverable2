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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {
	
	private Utilities() {
	    throw new IllegalStateException("Utility class");
	}

	
	private static Logger logger = Logger.getLogger(Utilities.class.getName());

	private static String readAll(Reader rd) throws IOException {
		var sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	    	sb.append((char) cp);
	    }
	  	return sb.toString();
	}

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (
				var rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
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
				var rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			) {
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}
	
	public static void writeFile(String projName, List<VersionInfo> versionInfo) {
		var delimiter = ",";
		var user = "Gian Marco/";
		String path = "C:/Users/" +  user + "Desktop/Falessi Deliverables/dataset/" + projName+ "_dataset.csv";
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
				var writer = new BufferedWriter(new FileWriter(file));
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
					
			/*writer.write("@RELATION iris\n\n" +
			   "@ATTRIBUTE Version name  STRING\n" +
			   "@ATTRIBUTE File name   STRING\n" +
			   "@ATTRIBUTE Size   NUMERIC\n" +
			   "@ATTRIBUTE NR   NUMERIC\n" +
			   "@ATTRIBUTE NAuth   NUMERIC\n" +
			   "@ATTRIBUTE LOCTouched   NUMERIC\n" +
			   "@ATTRIBUTE LOCAdded   NUMERIC\n" +
			   "@ATTRIBUTE MAX_LOC_Added   NUMERIC\n" +
			   "@ATTRIBUTE AVG_LOC_Added   NUMERIC\n" +
			   "@ATTRIBUTE Churn   NUMERIC\n" +
			   "@ATTRIBUTE MAX_Churn   NUMERIC\n" +
			   "@ATTRIBUTE AVG_Churn   NUMERIC\n" +
			   "@ATTRIBUTE bugginess     {YES,NO}"); */
			
			for (var i = 0; i < versionInfo.size(); i++) {
				List<String> classes = versionInfo.get(i).getJavaClasses();
				for (var j = 0; j < classes.size(); j++) {
				
					var avgLoc = 0;
					var avgChurn = 0;
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
	
	static void deleteDir(File directoryToBeDeleted) throws IOException {
		var rootPath = Paths.get(directoryToBeDeleted.getAbsolutePath());
		try (Stream<Path> walk = Files.walk(rootPath)) {
		    walk.sorted(Comparator.reverseOrder())
		        .map(Path::toFile)
		        .forEach(File::delete);
		}
	}
	
	
	public static void runCommand(String command, Process p) throws IOException {
		var input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        var text = new StringBuilder();
        text.append(command + "\n");
        while ((line = input.readLine()) != null) {
            text.append(line + "\n");
        }
        logger.log(Level.INFO, "Line: {0}", text);
	}
}
