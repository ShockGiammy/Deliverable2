package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONException;

import weka.TestWeka;

public class GetAllFiles {

	static List<String> buggyClasses = new ArrayList<>();
	static String shell = "cmd.exe";

	public static void main(String[] args) throws Exception {
		
		var projName ="BOOKKEEPER";  //BOOKKEEPER  ZOOKEEPER
		
		List<VersionInfo> versionInfo = null;
		List<JiraTicket> tickets = null;
		List<String> versionsList = new ArrayList<>();
		
		try {
			cloneProject(projName);
		} catch (IOException e) {
			Utilities.logError(e);
		}
		
		var proportion = new ProportionCalculator(versionsList);
		
		Utilities.logMsg("Getting releases informations...");
		try {
			versionInfo = new GetReleaseInfo().getReleaseInfo(projName, proportion, versionsList);
		} catch (JSONException | IOException e) {
			Utilities.logError(e);
		}
		
		Utilities.logMsg("Calculating statistics on classes...");
		try {
			locAnalysis(projName, versionInfo);
		} catch (IOException | ParseException e) {
			Utilities.logError(e);
		}
		
		Utilities.logMsg("Getting tickets informations...");
		try {
			tickets = new RetrieveTicketsID().retrieveTicketsID(projName);
		} catch (JSONException | IOException | ParseException e) {
			Utilities.logError(e);
		}
		
		if (tickets != null) {
			associateOpeningVersion(projName, versionInfo, tickets);
		}
		
		Utilities.logMsg("Updating classes' size...");
		var remainingReleases = 0;
		if (versionInfo != null) {
			remainingReleases = (versionInfo.size()+1)/2;
			for (var i = 1; i < versionInfo.size(); i++) {
				versionInfo.get(i).updateSize();
			}
		}
	
		if (versionInfo != null && tickets != null) {
			for (var i = 0; i < versionInfo.size(); i++) {
				for (var j = 0; j < tickets.size(); j ++) {
					versionInfo.get(i).setBuggyClasses(tickets.get(j));
				}
			}
		}

		
		Utilities.writeFile(projName, versionInfo, remainingReleases);
		Utilities.deleteDir(new File(projName));
		
		Utilities.logMsg("Starting weka analysis...");
		new TestWeka(projName, versionInfo, remainingReleases);
	}
	
	public static void associateOpeningVersion(String projName, List<VersionInfo> versionInfo, List<JiraTicket> tickets) {
		for (var i = 0; i < tickets.size(); i++) {
			try {
				retrieveCommitsID(projName, tickets.get(i));
			} catch (IOException e) {
				Utilities.logError(e);
			}
			if (versionInfo != null) {
				for (var j = 0; j < versionInfo.size()-1; j++) {
					if (versionInfo.get(j).getVersionDate().before(tickets.get(i).getOpeningDate()) && 
						versionInfo.get(j+1).getVersionDate().after(tickets.get(i).getOpeningDate())) {
						tickets.get(i).setOpeningVersion(versionInfo.get(j).getVersionName());
					}
					else if (versionInfo.get(versionInfo.size()-1).getVersionDate().before(tickets.get(i).getOpeningDate())) {
						tickets.get(i).setOpeningVersion(versionInfo.get(versionInfo.size()-1).getVersionName());
					}
				}
			}
		}
	}
	
	
	public static void cloneProject(String projName) throws IOException {
		String url = "https://github.com/apache/" + projName;
		String command = "git clone " + url;
		var builder = new ProcessBuilder(shell, "/c", command);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		Utilities.runCommand(command, p);
	}
	
	public static void retrieveCommitsID(String dirName, JiraTicket jiraTicket) throws IOException {

		String ticket = jiraTicket.getTicketName();
		String command = "cd " + dirName + " && git log --grep=" + ticket + " --pretty=\"short\" --name-only";
		var builder = new ProcessBuilder(shell, "/c", command);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		
		var input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        var text = new StringBuilder();
        text.append(command + "\n");
        while ((line = input.readLine()) != null) {
            text.append(line + "\n");
            if (line.contains(".java")) {
            	String affectedClass = line;
            	jiraTicket.addBuggyClasses(affectedClass);
            }
        }
	}
	
	public static void locAnalysis(String dirName, List<VersionInfo> versions) throws IOException, ParseException {

		String command = "cd " + dirName + " && git log --stat=260 --reverse";
		var builder = new ProcessBuilder(shell, "/c", command);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		var formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",Locale.US); 

		var input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        var text = new StringBuilder();
        text.append(command + "\n");
		Date date = null;
		String author = null;
        while ((line = input.readLine()) != null) {
            text.append(line + "\n");
            if (line.contains("Date:"))  {
           		String dateToBeParse = line.replace("Date: ", "").replace("  ", "");
           		date = formatter.parse(dateToBeParse);
            }
            if (line.contains("Author:"))  {
           		author = line.replace("Author: ", "").split(Pattern.quote(" <"))[0];
            }
           	else if (line.contains(".java") && line.contains(" | ")) {
           		String javaClass = line.split(Pattern.quote(" | "))[0].replace(" ", "");
           		var locTouched = Integer.parseInt(line.split(Pattern.quote(" | "))[1].split(Pattern.quote("+"))[0].replace(" ", "").replace("-", ""));
           		
           		
           		String toBeCount = line.split(Pattern.quote(" | "))[1];
           		String withoutPlus = toBeCount.replace("+", "");
           		int added = toBeCount.length() - withoutPlus.length();
           		String withoutMinus = toBeCount.replace("-", "");
           		int removed = toBeCount.length() - withoutMinus.length();
           		int locAdded = (int) Math.ceil(added/(double)(added+removed)* locTouched);
           		int locRemoved = (int) Math.ceil(removed/(double)(added+removed)* locTouched);
           		int sizeAdded = locAdded - locRemoved;
           		
           		fillVersionsInfo(versions, date, javaClass, locTouched, locAdded, sizeAdded, author);
            }
        }
	}

	public static void fillVersionsInfo(List<VersionInfo> versions, Date date, String javaClass, int locTouched, int locAdded, int sizeAdded, String author) {
		if (versions.get(0).getVersionDate().after(date)) {
		
			versions.get(0).addClass(javaClass);
			versions.get(0).addLocTouched(javaClass, locTouched);
			versions.get(0).addLocAdded(javaClass, locAdded);
			versions.get(0).addSize(javaClass, sizeAdded);
			versions.get(0).addAuthor(javaClass, author);
		}
		else {
		
	
			for (var i = 0; i < versions.size()-1; i++) {
		
				if (versions.get(i).getVersionDate().before(date) && versions.get(i+1).getVersionDate().after(date)) {
				
					versions.get(i).addClass(javaClass);
					versions.get(i).addLocTouched(javaClass, locTouched);
					versions.get(i).addLocAdded(javaClass, locAdded);
					versions.get(i).addSize(javaClass, sizeAdded);
					versions.get(i).addAuthor(javaClass, author);
				}
			}
			if (versions.get(versions.size()-1).getVersionDate().before(date)) {
				
				versions.get(versions.size()-1).addClass(javaClass);
				versions.get(versions.size()-1).addLocTouched(javaClass, locTouched);
				versions.get(versions.size()-1).addLocAdded(javaClass, locAdded);
				versions.get(versions.size()-1).addSize(javaClass, sizeAdded);
				versions.get(versions.size()-1).addAuthor(javaClass, author);
			}
		}
	}
}