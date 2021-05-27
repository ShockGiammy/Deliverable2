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
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;


public class TestWeka{
	
	private static Logger logger = Logger.getLogger(TestWeka.class.getName());
	
	static List<Evaluation> randomForestRuns = new ArrayList<>();
	static List<Evaluation> naiveBayesRuns = new ArrayList<>();
	static List<Evaluation> ibkRuns = new ArrayList<>();
	static int numberParts = 10;
	
	public static void main(String[] args) throws Exception {
		
		var projName = "BOOKKEEPER";
		var user = "Gian Marco/";
		String basePath = "C:/Users/" +  user + "Desktop/Falessi Deliverables/dataset/" + projName;
		
		var path = basePath + "_dataset.csv";
		var originalDataset = new File(path);
		
		var trainingPath = basePath + "_training.csv";
		var trainingFile = new File(trainingPath);
		
		var testingPath = basePath + "_testing.csv";
		var testingFile = new File(testingPath);
		
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
			rows--;
		}
		
		int lenghtPart = (rows/numberParts);
		String line;
		for (var i = 1; i < numberParts; i++) {		
			
			rows = 0;
			try (	
				var reader = new BufferedReader(new FileReader(originalDataset));
				) {
				try (	
					var trainingWriter =  new PrintWriter(new FileWriter(trainingFile));
					) {
					while ((line = reader.readLine()) != null && rows < (lenghtPart*i)) {
						trainingWriter.println(line);
						rows++;
					}
				}
				try (	
					var testingWriter=  new PrintWriter(new FileWriter(testingFile));
					) {
					testingWriter.println(header);
					testingWriter.println(line);
					while ((line = reader.readLine()) != null && rows < (lenghtPart*(i+1))) {
						rows++;
						if (rows > (lenghtPart*(i))) {
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

	private static void writeStatisticsOnFile(String projName) {
		
		var user = "Gian Marco/";
		String path = "C:/Users/" +  user + "Desktop/Falessi Deliverables/" + projName+ "_results.csv";
		var file = new File(path);
		if (file.exists())
			logger.log(Level.INFO, "Il file {0} esiste", path);
		else
			try {
				if (file.createNewFile())
					logger.log(Level.INFO, "Il file {0} è stato creato", path);
				else
					logger.log(Level.INFO, "Il file {0} non può essere creato", path);
			} catch (IOException e) {
				e.printStackTrace();
		}
		
		var delimiter = ",";
		try (
				var writer = new BufferedWriter(new FileWriter(file));
				) {
				writer.write("Dataset" + delimiter + "#TrainingRelease" + delimiter
					+ "Classifier" + delimiter 
					+ "Precision" + delimiter
					+ "Recall" + delimiter
					+ "AUC" + delimiter
					+ "Kappa" + "\n");
					
			for (var i = 0; i < numberParts-1; i++) {
					writer.write(projName + delimiter + (i+1) + delimiter +
						"RandomForest" + delimiter +
						randomForestRuns.get(i).precision(0) + delimiter +
						randomForestRuns.get(i).recall(0) + delimiter +
						randomForestRuns.get(i).areaUnderPRC(0) + delimiter +
						randomForestRuns.get(i).kappa() + delimiter + "\n");
					writer.write(projName + delimiter + (i+1) + delimiter +
						"NaiveBayes" + delimiter +
						naiveBayesRuns.get(i).precision(0) + delimiter +
						naiveBayesRuns.get(i).recall(0) + delimiter +
						naiveBayesRuns.get(i).areaUnderPRC(0) + delimiter +
						naiveBayesRuns.get(i).kappa() + delimiter + "\n");
					writer.write(projName + delimiter + (i+1) + delimiter +
						"IBk" + delimiter +
						ibkRuns.get(i).precision(0) + delimiter +
						ibkRuns.get(i).recall(0) + delimiter +
						ibkRuns.get(i).areaUnderPRC(0) + delimiter +
						ibkRuns.get(i).kappa() + delimiter + "\n");
			
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void calculateStatistics(String trainingPath, String testingPath) throws Exception {
		
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
