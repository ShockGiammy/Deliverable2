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

import src.Utilities;
import src.VersionInfo;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;


public class TestWeka{
	
	List<Evaluation> randomForestRuns = new ArrayList<>();
	List<Evaluation> naiveBayesRuns = new ArrayList<>();
	List<Evaluation> ibkRuns = new ArrayList<>();
	private int releases;
	private String delimiter = ",";
	
	private int originalRows;
	private List<Integer> trainingRows = new ArrayList<>();
	private List<Integer> defectsTrain = new ArrayList<>();
	
	public TestWeka(String projName, List<VersionInfo> versionInfo, int remainingReleases) throws Exception {
		
		this.releases = remainingReleases;
		var user = "Gian Marco/";
		String basePath = "C:/Users/" +  user + "Desktop/Deliverable2/dataset/" + projName;
		
		var path = basePath + "_dataset.csv";
		var originalDataset = new File(path);
		
		var trainingPath = basePath + "_training.csv";
		var trainingFile = new File(trainingPath);
		
		var testingPath = basePath + "_testing.csv";
		var testingFile = new File(testingPath);
		
		String header = getHeaderFile(originalDataset);
		
		String line = null;
		for (var i = 1; i < releases; i++) {
			Utilities.logParametrizedMsg(i);
			var count = 0;
			var defects = 0;
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
							if (line.contains("YES")) {
								defects++;
							}
						}
						if (j != i-1) {
							trainingWriter.println(line);
						}
					}
					createTestingDataset(line, header, reader, i, versionInfo, testingFile);
				}
			}
			
			trainingRows.add(count);
			defectsTrain.add(defects);
			var trainingArff = basePath + "_training.arff";
			var trainingFileArff = new File(trainingArff);
			CSV2Arff.convertCsv2Arff(trainingFile, trainingFileArff);
			
			var testingArff = basePath + "_testing.arff";
			var testingFileArff = new File(testingArff);
			CSV2Arff.convertCsv2Arff(testingFile, testingFileArff);
			
			
			calculateStatistics(trainingArff, testingArff);
        }
		
		var writer = new WriteResults(projName);
		writer.setParametres(randomForestRuns, naiveBayesRuns, ibkRuns, originalRows, releases, trainingRows, defectsTrain);
		writer.writeStatisticsOnFile();
	}
	
	
	private void createTestingDataset(String line, String header, BufferedReader reader, int i, List<VersionInfo> versionInfo, File testingFile) throws IOException {
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
	
	
	int[] indices = {0, 1};
	private void calculateStatistics(String trainingPath, String testingPath) throws Exception {
		
		var training = new Instances(new BufferedReader(new FileReader(trainingPath)));
		
		var testing = new Instances(new BufferedReader(new FileReader(testingPath)));

		var removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(indices);
		removeFilter.setInputFormat(training);
		training = Filter.useFilter(training, removeFilter);
		removeFilter.setInputFormat(testing);
		testing = Filter.useFilter(testing, removeFilter);
		
		for (var i = 0; i < 4; i++) {
			for (var j = 0; j < 2; j++) {
				for (var k = 0; k < 3; k++) {
					applyFiltersAndCalculate(training, testing, i, j, k);
				}
			}
		}
	}
	
	private void applyFiltersAndCalculate(Instances training, Instances testing, int i, int j, int k) throws Exception {
		
		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);

		List<CostSensitiveClassifier> csClassifiers = null;
		if (i == 1) {
			training = calculateOverSampling(training);
		}
		else if (i == 2) {
			training = calculateUnderSampling(training);
		}
		else if (i == 3) {			
			training = calculateSmote(training);
		}

		if (j == 1) {
			List<Instances> instances =  calculateBestFirst(training, testing);
			training = instances.get(0);
			testing = instances.get(1);
		}

		if (k == 1) {
			csClassifiers = calculateSensitiveThreshold();
		}
		else if (k == 2) {
			csClassifiers = calculateSensitiveLearning();
		}
		calculate(training, testing, csClassifiers);
	}
	
	
	private void calculate(Instances training, Instances testing, List<CostSensitiveClassifier> csClassifiers) throws Exception {
		
		
		Evaluation evalRandomForest = null;
		Evaluation evalNaiveBayes = null;
		Evaluation evalIBk = null;
		if (csClassifiers == null) {
			var randomForest = new RandomForest();
			var naiveBayes = new NaiveBayes();
			var ibk = new IBk();
			
			randomForest.buildClassifier(training);
			evalRandomForest = new Evaluation(testing);	
			evalRandomForest.evaluateModel(randomForest, testing);
			
			naiveBayes.buildClassifier(training);
			evalNaiveBayes = new Evaluation(testing);	
			evalNaiveBayes.evaluateModel(naiveBayes, testing);
			
			ibk.buildClassifier(training);
			evalIBk = new Evaluation(testing);	
			evalIBk.evaluateModel(ibk, testing);
		}
		else {
			
			var costSensitiveRF = csClassifiers.get(0);
			costSensitiveRF.buildClassifier(training);	
			evalRandomForest = new Evaluation(testing, costSensitiveRF.getCostMatrix());
			evalRandomForest.evaluateModel(costSensitiveRF, testing);
			
			var costSensitiveNB = csClassifiers.get(1);
			costSensitiveNB.buildClassifier(training);	
			evalNaiveBayes = new Evaluation(testing, costSensitiveNB.getCostMatrix());
			evalNaiveBayes.evaluateModel(costSensitiveNB, testing);
			
			var costSensitiveIBk = csClassifiers.get(2);
			costSensitiveIBk.buildClassifier(training);	
			evalIBk = new Evaluation(testing, costSensitiveIBk.getCostMatrix());
			evalIBk.evaluateModel(costSensitiveIBk, testing);
		}
		
		randomForestRuns.add(evalRandomForest);
		naiveBayesRuns.add(evalNaiveBayes);
		ibkRuns.add(evalIBk);
	}
	
	
	private List<Instances> calculateBestFirst(Instances training, Instances testing) throws Exception {
		var filter = new AttributeSelection();
		var eval = new CfsSubsetEval();
		var search = new BestFirst();
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(training);
		Instances bestFirstTraining = Filter.useFilter(training, filter);
		Instances bestFirstTesting = Filter.useFilter(testing, filter);
		List<Instances> instances = new ArrayList<>();
		instances.add(bestFirstTraining);
		instances.add(bestFirstTesting);
		
		return instances;
	}
	
	private Instances calculateOverSampling(Instances training) throws Exception {
		
		var fc = new FilteredClassifier();
		
		var countYes = 0;
		var countNo = 0;
		for (var i = 0; i < training.size(); i ++) {
			if (training.get(i).stringValue(training.get(i).attribute(training.numAttributes()-1)).contains("YES")) {
				countYes++;
			}
			else {
				countNo++;
			}
		}
		
		double majorityPerc;
		if (countYes > countNo) {
			majorityPerc = Double.valueOf(countYes)/training.size()*100;
		}
		else {
			majorityPerc = Double.valueOf(countNo)/training.size()*100;
		}
		
		
		var resample = new Resample();
		var opts = new String[]{ "-B", "1.0", "-Z", String.valueOf(majorityPerc*2)};
		resample.setOptions(opts);
		resample.setInputFormat(training);
		fc.setFilter(resample);
		
		return Filter.useFilter(training, resample);
	}
	
	private Instances calculateUnderSampling(Instances training) throws Exception {

		var fc = new FilteredClassifier();
		
		var  spreadSubsample = new SpreadSubsample();
		var opts = new String[]{ "-M", "1.0"};
		spreadSubsample.setOptions(opts);
		spreadSubsample.setInputFormat(training);
		fc.setFilter(spreadSubsample);
		
		return Filter.useFilter(training, spreadSubsample);
	}
	
	private Instances calculateSmote(Instances training) throws Exception {

		
		var countYes = 0;
		var countNo = 0;
		for (var i = 0; i < training.size(); i ++) {
			if (training.get(i).stringValue(training.get(i).attribute(training.numAttributes()-1)).contains("YES")) {
				countYes++;
			}
			else {
				countNo++;
			}
		}
		double perc;
		if (countYes > countNo && countNo!=0) {
			perc = ((Double.valueOf(countYes)/countNo)-1)*100;
		}
		else if (countNo > countYes && countYes!=0){
			perc = ((Double.valueOf(countNo)/countYes)-1)*100;
		}
		else {
			perc = 100;
		}
		
		var fc = new FilteredClassifier();
		var smote = new SMOTE();
		var opts = new String[]{ "-P", String.valueOf(perc)};
		smote.setOptions(opts);
		smote.setInputFormat(training);
		fc.setFilter(smote);

		return Filter.useFilter(training, smote);
	}
	
	private CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
	    var costMatrix = new CostMatrix(2);
	    costMatrix.setCell(0, 0, 0.0);
	    costMatrix.setCell(1, 0, weightFalsePositive);
	    costMatrix.setCell(0, 1, weightFalseNegative);
	    costMatrix.setCell(1, 1, 0.0);
	    return costMatrix;
	}

	private List<CostSensitiveClassifier> calculateSensitiveThreshold() {
		
		List<CostSensitiveClassifier> csClassifiers = new ArrayList<>();
		
		var costSensitiveRF = new CostSensitiveClassifier();
		costSensitiveRF.setClassifier(new RandomForest());
		costSensitiveRF.setCostMatrix(createCostMatrix(1, 10));
		costSensitiveRF.setMinimizeExpectedCost(true);
		csClassifiers.add(costSensitiveRF);
		
		var costSensitiveNB = new CostSensitiveClassifier();
		costSensitiveNB.setClassifier(new NaiveBayes());
		costSensitiveNB.setCostMatrix(createCostMatrix(1, 10));
		costSensitiveNB.setMinimizeExpectedCost(true);
		csClassifiers.add(costSensitiveNB);
		
		var costSensitiveIBk = new CostSensitiveClassifier();
		costSensitiveIBk.setClassifier(new IBk());
		costSensitiveIBk.setCostMatrix(createCostMatrix(1, 10));
		costSensitiveIBk.setMinimizeExpectedCost(true);
		csClassifiers.add(costSensitiveIBk);
		
		return csClassifiers;
	}
	
	private List<CostSensitiveClassifier> calculateSensitiveLearning() {
		
		List<CostSensitiveClassifier> csClassifiers = new ArrayList<>();
		
		var costSensitiveRF = new CostSensitiveClassifier();
		costSensitiveRF.setClassifier(new RandomForest());
		costSensitiveRF.setCostMatrix(createCostMatrix(1, 10));
		costSensitiveRF.setMinimizeExpectedCost(false);
		csClassifiers.add(costSensitiveRF);
		
		var costSensitiveNB = new CostSensitiveClassifier();
		costSensitiveNB.setClassifier(new NaiveBayes());
		costSensitiveNB.setCostMatrix(createCostMatrix(1, 10));
		costSensitiveNB.setMinimizeExpectedCost(false);
		csClassifiers.add(costSensitiveNB);
		
		var costSensitiveIBk = new CostSensitiveClassifier();
		costSensitiveIBk.setClassifier(new IBk());
		costSensitiveIBk.setCostMatrix(createCostMatrix(1, 10));
		costSensitiveIBk.setMinimizeExpectedCost(false);
		csClassifiers.add(costSensitiveIBk);
		
		return csClassifiers;
	}
}
