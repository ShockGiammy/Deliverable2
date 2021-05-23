package src;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JiraTicket {
	
	private String ticketName;
	private String fixVersion;
	private Date openingDate;
	private String openingVersion;
	private List<String> affectedVersions;
	private List<String> buggyClasses;
	
	
	public String getTicketName() {
		return ticketName;
	}

	public String getFixVersion() {
		return fixVersion;
	}
	
	public Date getOpeningDate() {
		return openingDate;
	}
	
	public String getOpeningVersion() {
		return openingVersion;
	}

	public void setOpeningVersion(String openingVersion) {
		this.openingVersion = openingVersion;
	}

	public List<String> getAffectedVersions() {
		return affectedVersions;
	}

	public JiraTicket(String ticketName, String fv, List<String> av, Date date)  {
		this.ticketName = ticketName;
		this.fixVersion = fv;
		this.affectedVersions = av;
		this.buggyClasses = new ArrayList<>();
		this.openingDate = date;
	}

	public List<String> getBuggyClasses() {
		return buggyClasses;
	}

	public void addBuggyClasses(String buggyClass) {
		if (!buggyClasses.contains(buggyClass)) {
			this.buggyClasses.add(buggyClass);
		}
	}
}
