package weka;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.lazy.IBk;


public class TestWeka{
	
	public static void main(String args[]) throws Exception {
		
		var numberParts = 10;
		var projName = "AVRO"; //"BOOKKEEPER";
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
		
		naiveBayes.buildClassifier(training);
		var evalNaiveBayes = new Evaluation(testing);	
		evalNaiveBayes.evaluateModel(naiveBayes, testing); 
		
		ibk.buildClassifier(training);
		var evalIBk = new Evaluation(testing);	
		evalIBk.evaluateModel(ibk, testing);
			
		System.out.println("RandomForest: \n");
		System.out.println("Precision = "+evalRandomForest.precision(0));
		System.out.println("Recall = "+evalRandomForest.recall(0));
		System.out.println("AUC = "+evalRandomForest.areaUnderPRC(0));
		System.out.println("kappa = "+evalRandomForest.kappa());
		
		System.out.println("NaiveBayes: \n");
		System.out.println("Precision = "+evalNaiveBayes.precision(0));
		System.out.println("Recall = "+evalNaiveBayes.recall(0));
		System.out.println("AUC = "+evalNaiveBayes.areaUnderPRC(0));
		System.out.println("kappa = "+evalNaiveBayes.kappa());
		
		System.out.println("IBk: \n");
		System.out.println("Precision = "+evalIBk.precision(0));
		System.out.println("Recall = "+evalIBk.recall(0));
		System.out.println("AUC = "+evalIBk.areaUnderPRC(0));
		System.out.println("kappa = "+evalIBk.kappa());
	}
}
