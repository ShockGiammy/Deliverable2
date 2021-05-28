package weka;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import src.Utilities;
import src.VersionInfo;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;


public class TestWeka{
	
	List<Evaluation> randomForestRuns = new ArrayList<>();
	List<Evaluation> naiveBayesRuns = new ArrayList<>();
	List<Evaluation> ibkRuns = new ArrayList<>();
	private int removeLastHalf;
	
	public TestWeka(String projName, List<VersionInfo> versionInfo, int removeLastHalf) throws Exception {
		
		this.removeLastHalf = removeLastHalf;
		var delimiter = ",";
		var user = "Gian Marco/";
		String basePath = "C:/Users/" +  user + "Desktop/Falessi Deliverables/dataset/" + projName;
		
		var path = basePath + "_dataset.csv";
		var originalDataset = new File(path);
		
		var trainingPath = basePath + "_training.csv";
		var trainingFile = new File(trainingPath);
		
		var testingPath = basePath + "_testing.csv";
		var testingFile = new File(testingPath);
		
		String header = getHeaderFile(originalDataset);
		
		String line = null;
		for (var i = 1; i < removeLastHalf; i++) {		
			try (	
				var reader = new BufferedReader(new FileReader(originalDataset));
				) {
				try (	
					var trainingWriter =  new PrintWriter(new FileWriter(trainingFile));
					) {
					for (var j = 0; j < i; j++) {
						while ((line = reader.readLine()) != null && (line.split(delimiter)[0].contains(versionInfo.get(j).getVersionName()) ||
								line.split(delimiter)[0].contains("Version name"))) {
							trainingWriter.println(line);
						}
						if (j != i-1) {
							trainingWriter.println(line);
						}
					}
					try (	
							var testingWriter=  new PrintWriter(new FileWriter(testingFile));
							) {
						testingWriter.println(header);
						testingWriter.println(line);
						while ((line = reader.readLine()) != null && line.split(delimiter)[0].contains(versionInfo.get(i).getVersionName())) {
							testingWriter.println(line);
						}
					}
				}
			}
			
			var trainingArff = basePath + "_training.arff";
			var trainingFileArff = new File(trainingArff);
			CSV2Arff.convertCsv2Arff(trainingFile, trainingFileArff);
			
			var testingArff = basePath + "_testing.arff";
			var testingFileArff = new File(testingArff);
			CSV2Arff.convertCsv2Arff(testingFile, testingFileArff);
			
			
			calculateStatistics(trainingArff, testingArff);
        }
		
		writeStatisticsOnFile(projName);
	}
	
	private String getHeaderFile(File originalDataset) throws IOException {
		var rows = 0;
		String header = null;
		try (			
				var reader = new BufferedReader(new FileReader(originalDataset));
				) {
			String line;
			while ((line = reader.readLine()) != null) {
				rows++;
				if (rows == 1) {
					header = line;
				}
			}
		}
		return header;
	}

	private void writeStatisticsOnFile(String projName) {
		
		
		var delimiter = ",";
		var user = "Gian Marco/";
		String path = "C:/Users/" +  user + "Desktop/Falessi Deliverables/" + projName+ "_results.csv";
		var file = new File(path);
		Utilities.createFile(file, path);
		
		try (
				var writer = new BufferedWriter(new FileWriter(file));
				) {
				writer.write("Dataset" + delimiter + "#TrainingRelease" + delimiter
					+ "Classifier" + delimiter 
					+ "Precision" + delimiter
					+ "Recall" + delimiter
					+ "AUC" + delimiter
					+ "Kappa" + "\n");
					
			for (var i = 0; i < removeLastHalf-1; i++) {
					writer.write(projName + delimiter + (i+1) + delimiter +
						"RandomForest" + delimiter +
						Utilities.roundDouble(randomForestRuns.get(i).precision(0), 3) + delimiter +
						Utilities.roundDouble(randomForestRuns.get(i).recall(0), 3) + delimiter +
						Utilities.roundDouble(randomForestRuns.get(i).areaUnderPRC(0), 3) + delimiter +
						Utilities.roundDouble(randomForestRuns.get(i).kappa(), 3) + delimiter + "\n");
					writer.write(projName + delimiter + (i+1) + delimiter +
						"NaiveBayes" + delimiter +
						Utilities.roundDouble(naiveBayesRuns.get(i).precision(0), 3) + delimiter +
						Utilities.roundDouble(naiveBayesRuns.get(i).recall(0), 3) + delimiter +
						Utilities.roundDouble(naiveBayesRuns.get(i).areaUnderPRC(0), 3) + delimiter +
						Utilities.roundDouble(naiveBayesRuns.get(i).kappa(), 3) + delimiter + "\n");
					writer.write(projName + delimiter + (i+1) + delimiter +
						"IBk" + delimiter +
						Utilities.roundDouble(ibkRuns.get(i).precision(0), 3) + delimiter +
						Utilities.roundDouble(ibkRuns.get(i).recall(0), 3) + delimiter +
						Utilities.roundDouble(ibkRuns.get(i).areaUnderPRC(0), 3) + delimiter +
						Utilities.roundDouble(ibkRuns.get(i).kappa(), 3) + delimiter + "\n");
			
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void calculateStatistics(String trainingPath, String testingPath) throws Exception {
		
		var training = new Instances(new BufferedReader(new FileReader(trainingPath)));
		
		var testing = new Instances(new BufferedReader(new FileReader(testingPath)));

		var removeFilter = new Remove();
		int[] indices = {0, 1};
		removeFilter.setAttributeIndicesArray(indices);
		removeFilter.setInputFormat(training);
		training = Filter.useFilter(training, removeFilter);
		removeFilter.setInputFormat(testing);
		testing = Filter.useFilter(testing, removeFilter);
		
		
		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);
		
		
		var randomForest = new RandomForest();
		var naiveBayes = new NaiveBayes();
		var ibk = new IBk();
		
		randomForest.buildClassifier(training);
		var evalRandomForest = new Evaluation(testing);	
		evalRandomForest.evaluateModel(randomForest, testing);
		randomForestRuns.add(evalRandomForest);
		
		
		naiveBayes.buildClassifier(training);
		var evalNaiveBayes = new Evaluation(testing);	
		evalNaiveBayes.evaluateModel(naiveBayes, testing); 
		naiveBayesRuns.add(evalNaiveBayes);
		
		ibk.buildClassifier(training);
		var evalIBk = new Evaluation(testing);	
		evalIBk.evaluateModel(ibk, testing);
		ibkRuns.add(evalIBk);
		
		var zeroR = new ZeroR();
		zeroR.buildClassifier(training);
		var evalZeroR = new Evaluation(testing);	
		evalZeroR.evaluateModel(zeroR, testing);
	}
}
