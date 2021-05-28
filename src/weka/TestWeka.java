package weka;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import src.VersionInfo;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.lazy.IBk;


public class TestWeka{
	
	List<Evaluation> randomForestRuns = new ArrayList<>();
	List<Evaluation> naiveBayesRuns = new ArrayList<>();
	List<Evaluation> ibkRuns = new ArrayList<>();
	private int releases;
	private String delimiter = ",";
	
	private int originalRows;
	private List<Integer> trainingRows = new ArrayList<>();
	
	
	public TestWeka(String projName, List<VersionInfo> versionInfo, int remainingReleases) throws Exception {
		
		this.releases = remainingReleases;
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
		for (var i = 1; i < releases; i++) {	
			var count = 0;
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
							count++;
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
			
			trainingRows.add(count);
			var trainingArff = basePath + "_training.arff";
			var trainingFileArff = new File(trainingArff);
			CSV2Arff.convertCsv2Arff(trainingFile, trainingFileArff);
			
			var testingArff = basePath + "_testing.arff";
			var testingFileArff = new File(testingArff);
			CSV2Arff.convertCsv2Arff(testingFile, testingFileArff);
			
			
			calculateStatistics(trainingArff, testingArff);
        }
		
		var writer = new WriteResults(projName, randomForestRuns, naiveBayesRuns, ibkRuns, originalRows, releases, trainingRows);
		writer.writeStatisticsOnFile();
	}
	
	private String getHeaderFile(File originalDataset) throws IOException {
		var count = 0;
		String header = null;
		try (			
				var reader = new BufferedReader(new FileReader(originalDataset));
				) {
			String line;
			while ((line = reader.readLine()) != null) {
				count++;
				if (count == 1) {
					header = line;
				}
			}
		}
		this.originalRows = count;
		return header;
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
		
		calculate(training, testing);
		calculateBestFirst(training, testing);
	}
	
	
	private void calculate(Instances training, Instances testing) throws Exception {
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
	}
	
	
	private void calculateBestFirst(Instances training, Instances testing) throws Exception {
		var filter = new AttributeSelection();
		var eval = new CfsSubsetEval();
		var search = new BestFirst();
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(training);
		Instances bestFirstTraining = Filter.useFilter(training, filter);
		Instances bestFirstTesting = Filter.useFilter(testing, filter);
		
		calculate(bestFirstTraining, bestFirstTesting);
	}
}
