package weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import src.Utilities;
import weka.classifiers.Evaluation;

public class WriteResults {
	
	private  String delimiter = ",";
	private List<Evaluation> randomForestRuns;
	private List<Evaluation> naiveBayesRuns;
	private List<Evaluation> ibkRuns;
	private String projName;
	private int originalRows;
	private int releases;
	private List<Integer> trainingRows;
	private List<Integer> defectsTrain;
	
	private String[] balancing = {"No sampling", "oversampling", "undersampling", "SMOTE"};
	private String[] featureSelection = {"No Feature Selection", "Best first"};
	private String[] sensitive = {"No cost sensitive", "Sensitive Threshold", "Sensitive Learning"};
	
	
	public WriteResults(String projName) {
		
		this.projName = projName;
	}
	
	public void setParametres(List<Evaluation> randomForestRuns, List<Evaluation> naiveBayesRuns, List<Evaluation> ibkRuns, int originalRows, int releases, List<Integer> trainingRows, List<Integer> defectsTrain) {
		
		this.randomForestRuns = randomForestRuns;
		this.naiveBayesRuns = naiveBayesRuns;
		this.ibkRuns = ibkRuns;
		
		this.originalRows = originalRows;
		this.releases = releases;
		this.trainingRows = trainingRows;
		this.defectsTrain = defectsTrain;
	}
	

	public void writeStatisticsOnFile() {
		
		var user = "Gian Marco/";
		String path = "C:/Users/" +  user + "Desktop/Falessi Deliverables/" + projName+ "_results.csv";
		var file = new File(path);
		Utilities.createFile(file, path);
		
		try (
				var writer = new BufferedWriter(new FileWriter(file));
				) {
				writer.write("Dataset" + delimiter
					+ "#TrainingRelease" + delimiter
					+ "%training" + delimiter					//to do
					+ "%Defective in training" + delimiter
					+ "%Defective in testing" + delimiter
					+ "balancing" + delimiter
					+ "Feature Selection" + delimiter
					+ "Sensitivity" + delimiter		//to do
					+ "TP" + delimiter
					+ "FP" + delimiter
					+ "TN" + delimiter
					+ "FN" + delimiter
					+ "Classifier" + delimiter 
					+ "Precision" + delimiter
					+ "Recall" + delimiter
					+ "AUC" + delimiter
					+ "Kappa" + "\n");
				
			var trainingRelease = 0;
			var j = 0;
			var k = 0;
			var l = 0;
			var runsForRelease = (balancing.length)*(featureSelection.length)*(sensitive.length);
			for (var i = 0; i < (releases-1)*runsForRelease; i++) {
				if ((i%runsForRelease) == 0) {
					trainingRelease++;
				}
				writeValues(writer, trainingRelease, i, balancing[j], featureSelection[k], sensitive[l]);
				l = (l+1)%sensitive.length;
				if (l == 0) {
					k = (k+1)%featureSelection.length;
					if (k == 0) {
						j = (j+1)%balancing.length;
					}
				}
			}
			writeAverages(writer, runsForRelease);
			writer.flush();
		} catch (IOException e) {
			Utilities.logError(e);
		}
	}
	
	
	private void writeAverages(BufferedWriter writer, int runsForRelease) throws IOException {
		
		var walk = "Walk";
		var forward = "Forward";
		var defectiveTrain = 0.0;
		for (var i = 0; i < releases-1; i++) {
			defectiveTrain += (Double.valueOf(defectsTrain.get(i))/trainingRows.get(i))*100;
		}
		
		var j = 0;
		var k = 0;
		var l = 0;
		for (var i = 0; i < runsForRelease; i++) {
			var defectiveTestRF = 0.0;
			var defectiveTestNB = 0.0;
			var defectiveTestIBK = 0.0;
			var tpRF = 0.0;
			var fpRF = 0.0;
			var tnRF = 0.0;
			var fnRF = 0.0;
			var tpNB = 0.0;
			var fpNB = 0.0;
			var tnNB = 0.0;
			var fnNB = 0.0;
			var tpIBK = 0.0;
			var fpIBK = 0.0;
			var tnIBK = 0.0;
			var fnIBK = 0.0;
			var precisionRF = 0.0;
			var recallRF = 0.0;
			var aucRF = 0.0;
			var kappaRF = 0.0;
			var precisionNB = 0.0;
			var recallNB = 0.0;
			var aucNB = 0.0;
			var kappaNB = 0.0;
			var precisionIBK = 0.0;
			var recallIBK = 0.0;
			var aucIBK = 0.0;
			var kappaIBK = 0.0;
			
			for (var r = i; r < (releases-1)*runsForRelease; r += runsForRelease) {
				
				defectiveTestRF += ((randomForestRuns.get(r).numTruePositives(0)+randomForestRuns.get(r).numFalseNegatives(0))/
						(randomForestRuns.get(r).numTruePositives(0)+randomForestRuns.get(r).numFalsePositives(0)+randomForestRuns.get(r).numTrueNegatives(0)+randomForestRuns.get(r).numFalseNegatives(0)))*100;
				
				tpRF += randomForestRuns.get(r).numTruePositives(0);
				fpRF += randomForestRuns.get(r).numFalsePositives(0);
				tnRF += randomForestRuns.get(r).numTrueNegatives(0);
				fnRF += randomForestRuns.get(r).numFalseNegatives(0);
				
				precisionRF += calculateaverageRF(r, 0);
				recallRF += calculateaverageRF(r, 1);
				aucRF += calculateaverageRF(r, 2);
				kappaRF += calculateaverageRF(r, 3);			
				
				defectiveTestNB += ((naiveBayesRuns.get(r).numTruePositives(0)+naiveBayesRuns.get(r).numFalseNegatives(0))/
						(naiveBayesRuns.get(r).numTruePositives(0)+naiveBayesRuns.get(r).numFalsePositives(0)+naiveBayesRuns.get(r).numTrueNegatives(0)+naiveBayesRuns.get(r).numFalseNegatives(0)))*100;
				
				tpNB += naiveBayesRuns.get(r).numTruePositives(0);
				fpNB += naiveBayesRuns.get(r).numFalsePositives(0);
				tnNB += naiveBayesRuns.get(r).numTrueNegatives(0);
				fnNB += naiveBayesRuns.get(r).numFalseNegatives(0);
				
				precisionNB += calculateaverageNB(r, 0);
				recallNB += calculateaverageNB(r, 1);
				aucNB += calculateaverageNB(r, 2);
				kappaNB += calculateaverageNB(r, 3);
				
				
				defectiveTestIBK += ((ibkRuns.get(r).numTruePositives(0)+ibkRuns.get(r).numFalseNegatives(0))/
						(ibkRuns.get(r).numTruePositives(0)+ibkRuns.get(r).numFalsePositives(0)+ibkRuns.get(r).numTrueNegatives(0)+ibkRuns.get(r).numFalseNegatives(0)))*100;
				
				tpIBK += ibkRuns.get(r).numTruePositives(0);
				fpIBK += ibkRuns.get(r).numFalsePositives(0);
				tnIBK += ibkRuns.get(r).numTrueNegatives(0);
				fnIBK += ibkRuns.get(r).numFalseNegatives(0);
				
				precisionIBK += calculateaverageIBk(r, 0);
				recallIBK += calculateaverageIBk(r, 1);
				aucIBK += calculateaverageIBk(r, 2);
				kappaIBK += calculateaverageIBk(r, 3);
				
			}
			writer.write(projName + delimiter + 
				walk + delimiter +
				forward + delimiter +
				Utilities.roundDouble(defectiveTrain/releases, 2) + "%" + delimiter +
				Utilities.roundDouble(defectiveTestRF/releases, 2) + "%" + delimiter +
				balancing[j] + delimiter +
				featureSelection[k] + delimiter +
				sensitive[l] + delimiter +
				Utilities.roundDouble(tpRF/(releases-1), 2) + delimiter +
				Utilities.roundDouble(fpRF/(releases-1), 2) + delimiter +
				Utilities.roundDouble(tnRF/(releases-1), 2) + delimiter +
				Utilities.roundDouble(fnRF/(releases-1), 2) + delimiter +
				"RandomForest" + delimiter +
				Utilities.roundDouble(precisionRF/(releases-1), 3) + delimiter +
				Utilities.roundDouble(recallRF/(releases-1), 3) + delimiter +
				Utilities.roundDouble(aucRF/(releases-1), 3) + delimiter +
				Utilities.roundDouble(kappaRF/(releases-1), 3) + delimiter + "\n");
		
			writer.write(projName + delimiter + 
				walk + delimiter +
				forward + delimiter +
				Utilities.roundDouble(defectiveTrain/releases, 2) + "%" + delimiter +
				Utilities.roundDouble(defectiveTestNB/releases, 2) + "%" + delimiter +
				balancing[j] + delimiter +
				featureSelection[k] + delimiter +
				sensitive[l] + delimiter +
				Utilities.roundDouble(tpNB/(releases-1), 2) + delimiter +
				Utilities.roundDouble(fpNB/(releases-1), 2) + delimiter +
				Utilities.roundDouble(tnNB/(releases-1), 2) + delimiter +
				Utilities.roundDouble(fnNB/(releases-1), 2) + delimiter +
				"NaiveBayes" + delimiter +
				Utilities.roundDouble(precisionNB/(releases-1), 3) + delimiter +
				Utilities.roundDouble(recallNB/(releases-1), 3) + delimiter +
				Utilities.roundDouble(aucNB/(releases-1), 3) + delimiter +
				Utilities.roundDouble(kappaNB/(releases-1), 3) + delimiter + "\n");
		
			writer.write(projName + delimiter + 
				walk + delimiter +
				forward + delimiter +
				Utilities.roundDouble(defectiveTrain/releases, 2) + "%" + delimiter +
				Utilities.roundDouble(defectiveTestIBK/releases, 2) + "%" + delimiter +
				balancing[j] + delimiter +
				featureSelection[k] + delimiter +
				sensitive[l] + delimiter +
				Utilities.roundDouble(tpIBK/(releases-1), 2) + delimiter +
				Utilities.roundDouble(fpIBK/(releases-1), 2) + delimiter +
				Utilities.roundDouble(tnIBK/(releases-1), 2) + delimiter +
				Utilities.roundDouble(fnIBK/(releases-1), 2) + delimiter +
				"IBk" + delimiter +
				Utilities.roundDouble(precisionIBK/(releases-1), 3) + delimiter +
				Utilities.roundDouble(recallIBK/(releases-1), 3) + delimiter +
				Utilities.roundDouble(aucIBK/(releases-1), 3) + delimiter +
				Utilities.roundDouble(kappaIBK/(releases-1), 3) + delimiter + "\n");
		
			writer.flush();
			
			l = (l+1)%sensitive.length;
			if (l == 0) {
				k = (k+1)%featureSelection.length;
				if (k == 0) {
					j = (j+1)%balancing.length;
				}
			}
		}
	}
	
	private double calculateaverageRF(int r, int i) {
		if (i == 0 && (!Double.isNaN(randomForestRuns.get(r).precision(0)))) {
			return randomForestRuns.get(r).precision(0);
		}
		else if (i == 1 && (!Double.isNaN(randomForestRuns.get(r).recall(0)))) {
			return randomForestRuns.get(r).recall(0);
		}
		else if (i == 2 && (!Double.isNaN(randomForestRuns.get(r).areaUnderROC(0)))) { 
			return randomForestRuns.get(r).areaUnderROC(0);
		}
		else if (i == 3 && (!Double.isNaN(randomForestRuns.get(r).kappa()))) {
			return randomForestRuns.get(r).kappa();
		}
		else {
			return 0;
		}
	}
	
	private double calculateaverageNB(int r, int i) {
		if (i == 0 && (!Double.isNaN(naiveBayesRuns.get(r).precision(0)))) {
			return naiveBayesRuns.get(r).precision(0);
		}
		else if (i == 1 && (!Double.isNaN(naiveBayesRuns.get(r).recall(0)))) {
			return naiveBayesRuns.get(r).recall(0);
		}
		else if (i == 2 && (!Double.isNaN(naiveBayesRuns.get(r).areaUnderROC(0)))) {
			return naiveBayesRuns.get(r).areaUnderROC(0);
		}
		else if (i == 3 && (!Double.isNaN(naiveBayesRuns.get(r).kappa()))) {
			return naiveBayesRuns.get(r).kappa();
		}
		else {
			return 0;
		}
	}
	
	private double calculateaverageIBk(int r, int i) {
		if (i == 0 && (!Double.isNaN(ibkRuns.get(r).precision(0)))) {
			return ibkRuns.get(r).precision(0);
		}
		else if (i == 1 && (!Double.isNaN(ibkRuns.get(r).recall(0)))) {
			return ibkRuns.get(r).recall(0);
		}
		else if (i == 2 && (!Double.isNaN(ibkRuns.get(r).areaUnderROC(0)))) {
			return ibkRuns.get(r).areaUnderROC(0);
		}
		else if (i == 3 && (!Double.isNaN(ibkRuns.get(r).kappa()))) {
			return ibkRuns.get(r).kappa();
		}
		else {
			return 0;
		}
	}
	
	private void writeValues(BufferedWriter writer, int trainingRelease, int i, String featureSelection, String sampling, String sensitive) throws IOException {
		writer.write(projName + delimiter + 
				trainingRelease + delimiter +
				Utilities.roundDouble((Double.valueOf(trainingRows.get(trainingRelease-1))/originalRows)*100, 2) + "%" + delimiter +
				Utilities.roundDouble((Double.valueOf(defectsTrain.get(trainingRelease-1))/trainingRows.get(trainingRelease-1))*100, 2) + "%" + delimiter +
				Utilities.roundDouble(((randomForestRuns.get(i).numTruePositives(0)+randomForestRuns.get(i).numFalseNegatives(0))/
						(randomForestRuns.get(i).numTruePositives(0)+randomForestRuns.get(i).numFalsePositives(0)+randomForestRuns.get(i).numTrueNegatives(0)+randomForestRuns.get(i).numFalseNegatives(0)))*100, 2) 
				+ "%" + delimiter +
				sampling + delimiter +
				featureSelection + delimiter +
				sensitive + delimiter +
				randomForestRuns.get(i).numTruePositives(0) + delimiter +
				randomForestRuns.get(i).numFalsePositives(0) + delimiter +
				randomForestRuns.get(i).numTrueNegatives(0) + delimiter +
				randomForestRuns.get(i).numFalseNegatives(0) + delimiter +
				"RandomForest" + delimiter +
				Utilities.roundDouble(randomForestRuns.get(i).precision(0), 3) + delimiter +
				Utilities.roundDouble(randomForestRuns.get(i).recall(0), 3) + delimiter +
				Utilities.roundDouble(randomForestRuns.get(i).areaUnderROC(0), 3) + delimiter +
				Utilities.roundDouble(randomForestRuns.get(i).kappa(), 3) + delimiter + "\n");
		
		writer.write(projName + delimiter +
				trainingRelease + delimiter +
				Utilities.roundDouble((Double.valueOf(trainingRows.get(trainingRelease-1))/originalRows)*100, 2) + "%" + delimiter +
				Utilities.roundDouble((Double.valueOf(defectsTrain.get(trainingRelease-1))/trainingRows.get(trainingRelease-1))*100, 2) + "%" + delimiter +
				Utilities.roundDouble(((naiveBayesRuns.get(i).numTruePositives(0)+naiveBayesRuns.get(i).numFalseNegatives(0))/
						(naiveBayesRuns.get(i).numTruePositives(0)+naiveBayesRuns.get(i).numFalsePositives(0)+naiveBayesRuns.get(i).numTrueNegatives(0)+naiveBayesRuns.get(i).numFalseNegatives(0)))*100, 2)
				+ "%" + delimiter +
				sampling + delimiter +
				featureSelection + delimiter +
				sensitive + delimiter +
				naiveBayesRuns.get(i).numTruePositives(0) + delimiter +
				naiveBayesRuns.get(i).numFalsePositives(0) + delimiter +
				naiveBayesRuns.get(i).numTrueNegatives(0) + delimiter +
				naiveBayesRuns.get(i).numFalseNegatives(0) + delimiter +
				"NaiveBayes" + delimiter +
				Utilities.roundDouble(naiveBayesRuns.get(i).precision(0), 3) + delimiter +
				Utilities.roundDouble(naiveBayesRuns.get(i).recall(0), 3) + delimiter +
				Utilities.roundDouble(naiveBayesRuns.get(i).areaUnderROC(0), 3) + delimiter +
				Utilities.roundDouble(naiveBayesRuns.get(i).kappa(), 3) + delimiter + "\n");
		
		writer.write(projName + delimiter +
				trainingRelease + delimiter +
				Utilities.roundDouble((Double.valueOf(trainingRows.get(trainingRelease-1))/originalRows)*100, 2) + "%" + delimiter +
				Utilities.roundDouble((Double.valueOf(defectsTrain.get(trainingRelease-1))/trainingRows.get(trainingRelease-1))*100, 2) + "%" + delimiter +
				Utilities.roundDouble(((ibkRuns.get(i).numTruePositives(0)+ibkRuns.get(i).numFalseNegatives(0))/
						(ibkRuns.get(i).numTruePositives(0)+ibkRuns.get(i).numFalsePositives(0)+ibkRuns.get(i).numTrueNegatives(0)+ibkRuns.get(i).numFalseNegatives(0)))*100, 2)
				+ "%" + delimiter +
				sampling + delimiter +
				featureSelection + delimiter +
				sensitive + delimiter +
				ibkRuns.get(i).numTruePositives(0) + delimiter +
				ibkRuns.get(i).numFalsePositives(0) + delimiter +
				ibkRuns.get(i).numTrueNegatives(0) + delimiter +
				ibkRuns.get(i).numFalseNegatives(0) + delimiter +
				"IBk" + delimiter +
				Utilities.roundDouble(ibkRuns.get(i).precision(0), 3) + delimiter +
				Utilities.roundDouble(ibkRuns.get(i).recall(0), 3) + delimiter +
				Utilities.roundDouble(ibkRuns.get(i).areaUnderROC(0), 3) + delimiter +
				Utilities.roundDouble(ibkRuns.get(i).kappa(), 3) + delimiter + "\n");
		
		writer.flush();
	}
}
