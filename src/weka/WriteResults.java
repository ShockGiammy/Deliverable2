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
	
	private String[] featureSelection = {"No Feature Selection", "Best first"};
	private String[] sampling = {"No sampling", "oversampling", "undersampling", "SMOTE"};
	private String[] sensitive = {"No cost sensitive", "Sensitive Threshold", "Sensitive Learning"};
	
	public WriteResults(String projName, List<Evaluation> randomForestRuns, List<Evaluation> naiveBayesRuns, List<Evaluation> ibkRuns, int originalRows, int releases, List<Integer> trainingRows) {
		
		this.randomForestRuns = randomForestRuns;
		this.naiveBayesRuns = naiveBayesRuns;
		this.ibkRuns = ibkRuns;
		this.projName = projName;
		this.originalRows = originalRows;
		this.releases = releases;
		this.trainingRows = trainingRows;
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
			for (var i = 0; i < (releases-1)*2; i+=2) {
				trainingRelease++;
				for (var j = 0; j < featureSelection.length; j++) {
					writeValues(writer, trainingRelease, i, featureSelection[j], sampling[0], sensitive[0]);
				}
			}
			writer.flush();
		} catch (IOException e) {
			Utilities.logError(e);
		}
	}
	
	
	private void writeValues(BufferedWriter writer, int trainingRelease, int i, String featureSelection, String sampling, String sensitive) throws IOException {
		writer.write(projName + delimiter + 
				trainingRelease + delimiter +
				Utilities.roundDouble((Double.valueOf(trainingRows.get(trainingRelease-1))/originalRows)*100, 2) + "%" + delimiter +
				"Defective in training" + delimiter +
				Utilities.roundDouble(randomForestRuns.get(i).errorRate(), 2) + "%" + delimiter +
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
				Utilities.roundDouble(randomForestRuns.get(i).areaUnderPRC(0), 3) + delimiter +
				Utilities.roundDouble(randomForestRuns.get(i).kappa(), 3) + delimiter + "\n");
		writer.write(projName + delimiter +
				trainingRelease + delimiter +
				Utilities.roundDouble((Double.valueOf(trainingRows.get(trainingRelease-1))/originalRows)*100, 2) + "%" + delimiter +
				"Defective in training" + delimiter +
				Utilities.roundDouble(naiveBayesRuns.get(i).errorRate(), 2) + "%" + delimiter +
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
				Utilities.roundDouble(naiveBayesRuns.get(i).areaUnderPRC(0), 3) + delimiter +
				Utilities.roundDouble(naiveBayesRuns.get(i).kappa(), 3) + delimiter + "\n");
		writer.write(projName + delimiter +
				trainingRelease + delimiter +
				Utilities.roundDouble((Double.valueOf(trainingRows.get(trainingRelease-1))/originalRows)*100, 2) + "%" + delimiter +
				"Defective in training" + delimiter +
				Utilities.roundDouble(ibkRuns.get(i).errorRate(), 2) + "%"  + delimiter +
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
				Utilities.roundDouble(ibkRuns.get(i).areaUnderPRC(0), 3) + delimiter +
				Utilities.roundDouble(ibkRuns.get(i).kappa(), 3) + delimiter + "\n");
		writer.flush();
	}
}
