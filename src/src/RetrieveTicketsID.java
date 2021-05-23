package src;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTicketsID {
  
   public List<JiraTicket> retrieveTicketsID(String projName) throws JSONException, IOException, ParseException {
		   
	   Integer j = 0;
	   Integer i = 0;
	   Integer total = 1;
	   JSONArray issues;
	   List<JiraTicket> tickets = new ArrayList<>();
	   //Get JSON API for closed bugs w/ AV in the project
	   do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;
         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR%22status%22=%22resolved%22)"
                + "AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,fixVersions,versions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();
         JSONObject json = Utilities.readJsonFromUrl(url);
         issues = json.getJSONArray("issues");
         total = json.getInt("total");
         for (; i < total && i < j; i++) {
            //Iterate through each bug
        	 
        	 String key = issues.getJSONObject(i%1000).get("key").toString();
        	 Object fields = issues.getJSONObject(i%1000).get("fields");
        	 String fixVersionJson = ((JSONObject) fields).get("fixVersions").toString();
        	 
        	 String fixVersion = null;
        	 if (fixVersionJson.length() > 3) {
        		 fixVersion = fixVersionJson.split("\"name\":\"")[1].split("\",\"")[0];
        	 }
        	 
        	 String affectedVersionJson = ((JSONObject) fields).get("versions").toString();
        	 String affectedVersion = null;
        	 List<String> affectedVersionList = new ArrayList<>();
        	 String[] splittedString;
        	 if (affectedVersionJson.length() > 3) {
        		 splittedString = affectedVersionJson.split("\"name\":\"");
        		 for (int len = 1; len < splittedString.length; len++) { 			 
        			 affectedVersion = splittedString[len].split("\",\"")[0];
        			 affectedVersionList.add(affectedVersion);
        		 }
        	 }
        	 String openingDate = ((JSONObject) fields).get("created").toString().substring(0, 10);
        	 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
        	 Date date = formatter.parse(openingDate);
        	 
        	 JiraTicket ticket = new JiraTicket(key, fixVersion, affectedVersionList, date);
        	 tickets.add(ticket);
         }  
      } while (i < total);
	   return tickets;   
   }
}
