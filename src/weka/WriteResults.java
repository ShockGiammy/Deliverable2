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
			var denPrecisionRF = releases;
			var denRecallRF = releases;
			var denAucRF = releases;
			var denKappaRF = releases;
			var denPrecisionNB = releases;
			var denRecallNB = releases;
			var denAucNB = releases;
			var denKappaNB = releases;
			var denPrecisionIBK = releases;
			var denRecallIBK = releases;
			var denAucIBK = releases;
			var denKappaIBK = releases;
			for (var r = i; r < (releases-1)*runsForRelease; r += runsForRelease) {
				
				defectiveTestRF += ((randomForestRuns.get(r).numTruePositives(0)+randomForestRuns.get(r).numFalseNegatives(0))/
						(randomForestRuns.get(r).numTruePositives(0)+randomForestRuns.get(r).numFalsePositives(0)+randomForestRuns.get(r).numTrueNegatives(0)+randomForestRuns.get(r).numFalseNegatives(0)))*100;
				
				tpRF += randomForestRuns.get(r).numTruePositives(0);
				fpRF += randomForestRuns.get(r).numFalsePositives(0);
				tnRF += randomForestRuns.get(r).numTrueNegatives(0);
				fnRF += randomForestRuns.get(r).numFalseNegatives(0);
				
				var values = calculateaverageRF(precisionRF, denPrecisionRF, r, 0);
				precisionRF += values[0];
				denPrecisionRF -= values[1];
				values = calculateaverageRF(recallRF, denRecallRF, r, 1);
				recallRF += values[0];
				denRecallRF -= values[1];
				values = calculateaverageRF(aucRF, denAucRF, r, 2);
				aucRF += values[0];
				denAucRF -= values[1];
				values = calculateaverageRF(kappaRF, denKappaRF, r, 3);
				kappaRF += values[0];
				denKappaRF -= values[1];
				
				
				
				defectiveTestNB += ((naiveBayesRuns.get(r).numTruePositives(0)+naiveBayesRuns.get(r).numFalseNegatives(0))/
						(naiveBayesRuns.get(r).numTruePositives(0)+naiveBayesRuns.get(r).numFalsePositives(0)+naiveBayesRuns.get(r).numTrueNegatives(0)+naiveBayesRuns.get(r).numFalseNegatives(0)))*100;
				
				tpNB += naiveBayesRuns.get(r).numTruePositives(0);
				fpNB += naiveBayesRuns.get(r).numFalsePositives(0);
				tnNB += naiveBayesRuns.get(r).numTrueNegatives(0);
				fnNB += naiveBayesRuns.get(r).numFalseNegatives(0);
				
				values = calculateaverageNB(precisionNB, denPrecisionNB, r, 0);
				precisionNB += values[0];
				denPrecisionNB -= values[1];
				values = calculateaverageNB(recallNB, denRecallNB, r, 1);
				recallNB += values[0];
				denRecallNB -= values[1];
				values = calculateaverageNB(aucNB, denAucNB, r, 2);
				aucNB += values[0];
				denAucNB -= values[1];
				values = calculateaverageNB(kappaNB, denKappaNB, r, 3);
				kappaNB += values[0];
				denKappaNB -= values[1];
				
				
				defectiveTestIBK += ((ibkRuns.get(r).numTruePositives(0)+ibkRuns.get(r).numFalseNegatives(0))/
						(ibkRuns.get(r).numTruePositives(0)+ibkRuns.get(r).numFalsePositives(0)+ibkRuns.get(r).numTrueNegatives(0)+ibkRuns.get(r).numFalseNegatives(0)))*100;
				
				tpIBK += ibkRuns.get(r).numTruePositives(0);
				fpIBK += ibkRuns.get(r).numFalsePositives(0);
				tnIBK += ibkRuns.get(r).numTrueNegatives(0);
				fnIBK += ibkRuns.get(r).numFalseNegatives(0);
				
				values = calculateaverageIBk(precisionIBK, denPrecisionIBK, r, 0);
				precisionIBK += values[0];
				denPrecisionIBK -= values[1];
				values = calculateaverageIBk(recallIBK, denRecallIBK, r, 1);
				recallIBK += values[0];
				denRecallIBK -= values[1];
				values = calculateaverageIBk(aucIBK, denAucIBK, r, 2);
				aucIBK += values[0];
				denAucIBK -= values[1];
				values = calculateaverageIBk(kappaIBK, denKappaIBK, r, 3);
				kappaIBK += values[0];
				denKappaIBK -= values[1];
				
			}
			writer.write(projName + delimiter + 
				walk + delimiter +
				forward + delimiter +
				Utilities.roundDouble(defectiveTrain/releases, 2) + "%" + delimiter +
				Utilities.roundDouble(defectiveTestRF/releases, 2) + "%" + delimiter +
				balancing[j] + delimiter +
				featureSelection[k] + delimiter +
				sensitive[l] + delimiter +
				Utilities.roundDouble(tpRF/releases, 2) + delimiter +
				Utilities.roundDouble(fpRF/releases, 2) + delimiter +
				Utilities.roundDouble(tnRF/releases, 2) + delimiter +
				Utilities.roundDouble(fnRF/releases, 2) + delimiter +
				"RandomForest" + delimiter +
				Utilities.roundDouble(precisionRF/denPrecisionRF, 3) + delimiter +
				Utilities.roundDouble(recallRF/denRecallRF, 3) + delimiter +
				Utilities.roundDouble(aucRF/denAucRF, 3) + delimiter +
				Utilities.roundDouble(kappaRF/denKappaRF, 3) + delimiter + "\n");
		
			writer.write(projName + delimiter + 
				walk + delimiter +
				forward + delimiter +
				Utilities.roundDouble(defectiveTrain/releases, 2) + "%" + delimiter +
				Utilities.roundDouble(defectiveTestNB/releases, 2) + "%" + delimiter +
				balancing[j] + delimiter +
				featureSelection[k] + delimiter +
				sensitive[l] + delimiter +
				Utilities.roundDouble(tpNB/releases, 2) + delimiter +
				Utilities.roundDouble(fpNB/releases, 2) + delimiter +
				Utilities.roundDouble(tnNB/releases, 2) + delimiter +
				Utilities.roundDouble(fnNB/releases, 2) + delimiter +
				"NaiveBayes" + delimiter +
				Utilities.roundDouble(precisionNB/denPrecisionNB, 3) + delimiter +
				Utilities.roundDouble(recallNB/denRecallNB, 3) + delimiter +
				Utilities.roundDouble(aucNB/denAucNB, 3) + delimiter +
				Utilities.roundDouble(kappaNB/denKappaNB, 3) + delimiter + "\n");
		
			writer.write(projName + delimiter + 
				walk + delimiter +
				forward + delimiter +
				Utilities.roundDouble(defectiveTrain/releases, 2) + "%" + delimiter +
				Utilities.roundDouble(defectiveTestIBK/releases, 2) + "%" + delimiter +
				balancing[j] + delimiter +
				featureSelection[k] + delimiter +
				sensitive[l] + delimiter +
				Utilities.roundDouble(tpIBK/releases, 2) + delimiter +
				Utilities.roundDouble(fpIBK/releases, 2) + delimiter +
				Utilities.roundDouble(tnIBK/releases, 2) + delimiter +
				Utilities.roundDouble(fnIBK/releases, 2) + delimiter +
				"IBk" + delimiter +
				Utilities.roundDouble(precisionIBK/denPrecisionIBK, 3) + delimiter +
				Utilities.roundDouble(recallIBK/denRecallIBK, 3) + delimiter +
				Utilities.roundDouble(aucIBK/denAucIBK, 3) + delimiter +
				Utilities.roundDouble(kappaIBK/denKappaIBK, 3) + delimiter + "\n");
		
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
	
	private double[] calculateaverageRF(double numerator, int denominator, int r, int i) {
		if (i == 0 && !Double.isNaN(randomForestRuns.get(r).precision(0))) {
			numerator += randomForestRuns.get(r).precision(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 1 && !Double.isNaN(randomForestRuns.get(r).recall(0))) {
			numerator += randomForestRuns.get(r).recall(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 2 && !Double.isNaN(randomForestRuns.get(r).areaUnderROC(0))) {
			numerator += randomForestRuns.get(r).areaUnderROC(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 3 && !Double.isNaN(randomForestRuns.get(r).kappa())) {
			numerator += randomForestRuns.get(r).kappa();
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else {
			return new double[] {0.0, 0};
		}
	}
	
	private double[] calculateaverageNB(double numerator, int denominator, int r, int i) {
		if (i == 0 && !Double.isNaN(naiveBayesRuns.get(r).precision(0))) {
			numerator += naiveBayesRuns.get(r).precision(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 1 && !Double.isNaN(naiveBayesRuns.get(r).recall(0))) {
			numerator += naiveBayesRuns.get(r).recall(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 2 && !Double.isNaN(naiveBayesRuns.get(r).areaUnderROC(0))) {
			numerator += naiveBayesRuns.get(r).areaUnderROC(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 3 && !Double.isNaN(naiveBayesRuns.get(r).kappa())) {
			numerator += naiveBayesRuns.get(r).kappa();
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else {
			return new double[] {0.0, 0};
		}
	}
	
	private double[] calculateaverageIBk(double numerator, int denominator, int r, int i) {
		if (i == 0 && !Double.isNaN(ibkRuns.get(r).precision(0))) {
			numerator += ibkRuns.get(r).precision(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 1 && !Double.isNaN(ibkRuns.get(r).recall(0))) {
			numerator += ibkRuns.get(r).recall(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 2 && !Double.isNaN(ibkRuns.get(r).areaUnderROC(0))) {
			numerator += ibkRuns.get(r).areaUnderROC(0);
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else if (i == 3 && !Double.isNaN(ibkRuns.get(r).kappa())) {
			numerator += ibkRuns.get(r).kappa();
			denominator = denominator - 1;
			return new double[] {numerator, denominator};
		}
		else {
			return new double[] {0.0, 0};
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
