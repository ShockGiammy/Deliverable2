package src;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class VersionInfo {
	
	private String versionID;
	private String versionName;
	private Date versionDate;
	
	List<String> javaClasses = new ArrayList<>();
	Map<String, Integer> classesSize = new HashMap<>();
	Map<String, Integer> churn = new HashMap<>();
	Map<String, Integer> maxChurn = new HashMap<>();
	Map<String, Integer> locAdded = new HashMap<>();
	Map<String, Integer> locTouched = new HashMap<>();
	Map<String, Integer> maxLocAdded = new HashMap<>();
	Map<String, Integer> numberRevision = new HashMap<>();
	Map<String, List<String>> authors = new HashMap<>();
	Map <String, String> buggyClasses = new HashMap<>();
	
	VersionInfo prevVersion;
	ProportionCalculator proportion;
	
	
	public String getVersionID() {
		return versionID;
	}

	public String getVersionName() {
		return versionName;
	}

	public Date getVersionDate() {
		return versionDate;
	}
	public VersionInfo(String versionID, String versionName, LocalDateTime versionDate, VersionInfo prevVersion, ProportionCalculator proportionIns) {
		this.versionID = versionID;
		this.versionName = versionName;
		this.versionDate = Date.from(versionDate.atZone(ZoneId.systemDefault()).toInstant());
		this.prevVersion = prevVersion;
		this.proportion = proportionIns;
	}
	
	public void removeOldClass(String newClass, String oldClass) {
		if (prevVersion != null) {
			prevVersion.removeOldClass(newClass, oldClass);
		}
		if (javaClasses.contains(oldClass)) {
			updateAll(newClass, oldClass);
			javaClasses.remove(oldClass);
			javaClasses.add(newClass);
		}
	}

	public boolean addClass(String javaClass) {
		if (javaClass.contains("{")) {
			String oldClass = oldClass(javaClass);
			javaClass = clearInput(javaClass);
			updateAll(javaClass, oldClass);
			javaClasses.remove(oldClass);
			if (prevVersion != null) {
				prevVersion.removeOldClass(javaClass, oldClass);
			}
		}
		if (!javaClasses.contains(javaClass)) {
			javaClasses.add(javaClass);
			return true;
		}
		return false;
	}
	
	public void updateAll(String newClass, String oldClass) {
		if (classesSize.containsKey(oldClass)) {
			int value = classesSize.remove(oldClass);
			classesSize.put(newClass, value);
		}
		
		if (locAdded.containsKey(oldClass)) {
			int value = locAdded.remove(oldClass);
			locAdded.put(newClass, value);
		}
		
		if (maxLocAdded.containsKey(oldClass)) {
			int value = maxLocAdded.remove(oldClass);
			maxLocAdded.put(newClass, value);
		}
		
		if (locTouched.containsKey(oldClass)) {
			int value = locTouched.remove(oldClass);
			locTouched.put(newClass, value);
		}
		
		if (numberRevision.containsKey(oldClass)) {
			int value = numberRevision.remove(oldClass);
			numberRevision.put(newClass, value);
		}
		
		if (churn.containsKey(oldClass)) {
			int value = churn.remove(oldClass);
			churn.put(newClass, value);
		}
		
		if (maxChurn.containsKey(oldClass)) {
			int value = maxChurn.remove(oldClass);
			maxChurn.put(newClass, value);
		}
		
		if (authors.containsKey(oldClass)) {
			List<String> list = authors.remove(oldClass);
			authors.put(newClass, list);
		}
	}
	
	
	private String oldClass (String javaClass) {
		
		String firstPart = javaClass.split(Pattern.quote("{"))[0];
		
		String toBeModified = javaClass.split(Pattern.quote("{"))[1].split(Pattern.quote("}"))[0];
		
		String[] splittedName = javaClass.split(Pattern.quote("{"))[1].split(Pattern.quote("}"));	
		var lastPart = "";
		if (splittedName.length > 1)  {
			lastPart = javaClass.split(Pattern.quote("{"))[1].split(Pattern.quote("}"))[1];
		}
		
		String mediumPart = toBeModified.split(Pattern.quote("=>"))[0];
		
		if (firstPart.length() > 0 && mediumPart.length() == 0) {
			firstPart = firstPart.substring(0, firstPart.length()-1);
		}
		
		return firstPart + mediumPart + lastPart;
	}
	
	private String clearInput(String javaClass) {
		
		String firstPart = javaClass.split(Pattern.quote("{"))[0];
		
		String toBeModified = javaClass.split(Pattern.quote("{"))[1].split(Pattern.quote("}"))[0];
		
		String[] splittedName = javaClass.split(Pattern.quote("{"))[1].split(Pattern.quote("}"));	
		var lastPart = "";
		if (splittedName.length > 1)  {
			lastPart = javaClass.split(Pattern.quote("{"))[1].split(Pattern.quote("}"))[1];
		}
		
		String[] mediumPartRaw = toBeModified.split(Pattern.quote("=>"));
		var mediumPart = "";
		if (mediumPartRaw.length > 1)  {
			mediumPart = mediumPartRaw[1];
		}
		else {
			lastPart = lastPart.substring(1, lastPart.length());
		}
		
		return firstPart + mediumPart + lastPart;
	}
	
	public List<String> getJavaClasses() {
		for (var i = 0; i < javaClasses.size(); i ++) {
			if (this.getSize(javaClasses.get(i)) <= 5 && (this.getRevision(javaClasses.get(i)) <= 0 || this.getlocTouched(javaClasses.get(i)) <= 0)) {
				javaClasses.remove(javaClasses.get(i));
			}
		}
		return this.javaClasses;
	}
		
	public void addSize(String javaClass, int size) {
		if (javaClass.contains("{")) {
			javaClass = clearInput(javaClass);
		}
		if (classesSize.containsKey(javaClass)) {
			int previousSize = classesSize.get(javaClass);
			classesSize.replace(javaClass, previousSize + size);
		}
		else {
			classesSize.put(javaClass, size);
		}
		if (churn.containsKey(javaClass)) {
			int previousSize = churn.get(javaClass);
			churn.replace(javaClass, previousSize + size);
		}
		else {
			churn.put(javaClass, size);
		}
		addMAXChurn(javaClass, size);
	}
	
	public void addMAXChurn(String javaClass, int loc) {
		if (maxChurn.containsKey(javaClass)) {
			int previousMax = maxChurn.get(javaClass);
			if (previousMax < loc) {
				maxChurn.replace(javaClass, loc);
			}
		}
		else {
			maxChurn.put(javaClass, loc);
		}
	}
	
	public void addLocAdded(String javaClass, int loc) {
		if (javaClass.contains("{")) {
			javaClass = clearInput(javaClass);
		}
		if (locAdded.containsKey(javaClass)) {
			int previousLoc = locAdded.get(javaClass);
			locAdded.replace(javaClass, previousLoc + loc);
		}
		else {
			locAdded.put(javaClass, loc);
		}
		addMAXLocAdded(javaClass, loc);
	}
	
	public void addMAXLocAdded(String javaClass, int loc) {

		if (maxLocAdded.containsKey(javaClass)) {
			int previousMax = maxLocAdded.get(javaClass);
			if (previousMax < loc) {
				maxLocAdded.replace(javaClass, loc);
			}
		}
		else {
			maxLocAdded.put(javaClass, loc);
		}
	}
	
	public Integer getlocAdded(String javaClass) {
		if (locAdded.containsKey(javaClass)) {
			return this.locAdded.get(javaClass);
		}
		else {
			return 0;
		}
	}
	
	public Integer getMAXlocAdded(String javaClass) {
		if (maxLocAdded.containsKey(javaClass)) {
			return this.maxLocAdded.get(javaClass);
		}
		else {
			return 0;
		}
	}

	public void addLocTouched(String javaClass, int loc) {
		if (javaClass.contains("{")) {
			javaClass = clearInput(javaClass);
		}
		if (locTouched.containsKey(javaClass)) {
			int previousLoc = locTouched.get(javaClass);
			locTouched.replace(javaClass, previousLoc + loc);
		}
		else {
			locTouched.put(javaClass, loc);
		}
		if (numberRevision.containsKey(javaClass)) {
			int previousLoc = numberRevision.get(javaClass);
			numberRevision.replace(javaClass, previousLoc+1);
		}
		else {
			numberRevision.put(javaClass, 1);
		}
	}
	
	public Integer getlocTouched(String javaClass) {
		if (locTouched.containsKey(javaClass)) {
			return this.locTouched.get(javaClass);
		}
		else {
			return 0;
		}
	}
	
	public Map<String, Integer> getListOfSize() {
		Map<String, Integer> sizeToSend = new HashMap<>();
		for (Entry<String, Integer> entry : classesSize.entrySet()) {
			if (entry.getValue() >= 5) {
				sizeToSend.put(entry.getKey(), entry.getValue());
			}
		}
		return sizeToSend;
	}
	
	public Integer getSize(String javaClass) {
		if (classesSize.containsKey(javaClass)) {
			if (this.classesSize.get(javaClass)> 0) {
				return this.classesSize.get(javaClass);
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	
	public Integer getChurn(String javaClass) {
		if (churn.containsKey(javaClass)) {
			return this.churn.get(javaClass);
		}
		else {
			return 0;
		}
	}
	
	public Integer getMAXChurn(String javaClass) {
		if (maxChurn.containsKey(javaClass)) {
			return this.maxChurn.get(javaClass);
		}
		else {
			return 0;
		}
	}
	
	public void updateSize() {
		Map<String, Integer> prevSize = prevVersion.getListOfSize();
		Set<String> keys = prevSize.keySet();
		for (String key : keys) {
			int prevValue = prevSize.get(key);
			if (!javaClasses.contains(key)) {
				addClass(key);
			}
			if (classesSize.containsKey(key)) {
				int actualValue = classesSize.get(key);
				classesSize.replace(key, (actualValue + prevValue));
			}
			else {
				classesSize.put(key, prevValue);
			}
		}
	}
	
	public Integer getRevision(String javaClass) {
		if (numberRevision.containsKey(javaClass)) {
			return this.numberRevision.get(javaClass);
		}
		else {
			return 0;
		}
	}
	
	public void addAuthor(String javaClass, String author) {
		if (javaClass.contains("{")) {
			javaClass = clearInput(javaClass);
		}
		if (authors.containsKey(javaClass)) {
			if (!authors.get(javaClass).contains(author)) {
				authors.get(javaClass).add(author);
			}
		}
		else {
			List<String> list = new ArrayList<>();
			list.add(author);
			authors.put(javaClass, list);
		}
	}
	
	public Integer getAuthors(String javaClass) {
		if (authors.containsKey(javaClass)) {
			return this.authors.get(javaClass).size();
		}
		else {
			return 0;
		}
	}
	
	public String getBuggyClass(String javaClass) {
		if (buggyClasses.containsKey(javaClass)) {
			return this.buggyClasses.get(javaClass);
		}
		else {
			return "NO";
		}
	}
	
	public void setBuggyClasses(JiraTicket ticket) {
			
		List<String> affectedVersion = ticket.getAffectedVersions();
		List<String> classesToControl = ticket.getBuggyClasses();
		String openinigVersion = ticket.getOpeningVersion();
		String fixVersion = ticket.getFixVersion();
		if (!affectedVersion.isEmpty()) {
			setBuggyAndUpdateProportion(affectedVersion, classesToControl, fixVersion, openinigVersion);
		}
		else {
			if (fixVersion != null && openinigVersion != null) {
				useProportion(classesToControl, fixVersion, openinigVersion);
			}
		}
	}
	
	private void incrementProportion(String fixVersion, String iv, String openinigVersion)  {
		if (fixVersion != null && openinigVersion != null) {
			proportion.incrementComputing(fixVersion, iv, openinigVersion);
		}
	}
	
	private void setBuggyAndUpdateProportion(List<String> affectedVersion, List<String> classesToControl, String fixVersion, String openinigVersion) {
		String iv = affectedVersion.get(0);
		for (String version : affectedVersion) {
			if (version.compareTo(iv) < 0) {
				iv = version;
			}
			if (this.versionName.equals(version)) {
				incrementProportion(fixVersion, iv, openinigVersion);
				if (!this.versionName.equals(fixVersion)) {
					for (String buggyClass : classesToControl) {
						setBuggy(buggyClass);
					}
				}
			}
		}
	}
	
	private void useProportion(List<String> classesToControl, String fixVersion, String openinigVersion) {
		List<String> proportionAV = proportion.getAffectedVersions(fixVersion, openinigVersion);
		for (String version : proportionAV) {
			if (this.versionName.equals(version)) {
				for (String buggyClass : classesToControl) {	
					if (javaClasses.contains(buggyClass)) {
						buggyClasses.put(buggyClass, "YES");
					}
				}
			}
		}
	}
	
	private void setBuggy(String buggyClass) {
		if (javaClasses.contains(buggyClass)) {
			buggyClasses.put(buggyClass, "YES");
		}
	}
}
