package src;

import java.util.ArrayList;
import java.util.List;

public class ProportionCalculator {
	
	private float p;
	private int denominator;
	private List<String> versions;
	
	public ProportionCalculator(List<String> versions) {
		this.versions = versions;
	}
	
	public void incrementComputing(String fixVersion, String iv, String openingVersion) {
		if (versions.indexOf(fixVersion) >= 0 && versions.indexOf(openingVersion) >= 0 && (versions.indexOf(fixVersion)-versions.indexOf(openingVersion) > 0)) {
			p = ((p*denominator)+(((float)versions.indexOf(fixVersion)-versions.indexOf(iv))/(versions.indexOf(fixVersion)-versions.indexOf(openingVersion))))/(denominator+1);
			this.denominator++;
		}
	}
	
	public List<String> getAffectedVersions(String fixVersion, String openingVersion) {
		List<String> affectedVersions = new ArrayList<>();
		if (versions.indexOf(fixVersion) >= 0 && versions.indexOf(openingVersion) >= 0) {
			int ivIndex = Math.round(versions.indexOf(fixVersion) - (versions.indexOf(fixVersion) - versions.indexOf(openingVersion))*this.p);
			if (ivIndex >= 0) {
				for (int i = ivIndex; i < versions.indexOf(fixVersion); i++) {
					affectedVersions.add(versions.get(i));
				}
			}
		}
		return affectedVersions;
	}

}
