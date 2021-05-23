package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class GetReleaseInfo {
	
	List<VersionInfo> versionInfo = new ArrayList<>();
	Map<LocalDateTime, String> releaseNames;
	Map<LocalDateTime, String> releaseID;
	List<LocalDateTime> releases;
	

	public List<VersionInfo> getReleaseInfo(String projName, ProportionCalculator proportion, List<String> versionList) throws JSONException, IOException {
		
		//Fills the arraylist with releases dates and orders them
		//Ignores releases with missing dates
		releases = new ArrayList<>();
		Integer i;
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		JSONObject json = Utilities.readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");
		releaseNames = new HashMap<>();
		releaseID = new HashMap<> ();
		for (i = 0; i < versions.length(); i++ ) {
			String name = "";
			String id = "";
			if(versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name,id);
			}
		}
		// order releases by date
		Collections.sort(releases, (LocalDateTime o1, LocalDateTime o2) -> o1.compareTo(o2));
		
		VersionInfo firstVersion = new VersionInfo(releaseID.get(releases.get(0)), releaseNames.get(releases.get(0)), releases.get(0), null, proportion);	
		versionInfo.add(firstVersion);
		versionList.add(releaseNames.get(releases.get(0)));
		for ( i = 1; i < releases.size(); i++) {
			VersionInfo version = new VersionInfo(releaseID.get(releases.get(i)), releaseNames.get(releases.get(i)), releases.get(i), versionInfo.get(i-1), proportion);	
			versionInfo.add(version);
			versionList.add(releaseNames.get(releases.get(i)));
		}
		return versionInfo;
	}
 
	
	public void addRelease(String strDate, String name, String id) {
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();
		if (!releases.contains(dateTime))
			releases.add(dateTime);
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
	}
}