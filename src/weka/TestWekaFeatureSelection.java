package weka;

/*
 *  How to use WEKA API in Java 
 *  Copyright (C) 2014 
 *  @author Dr Noureddin M. Sadawi (noureddin.sadawi@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it as you wish ... 
 *  I ask you only, as a professional courtesy, to cite my name, web page 
 *  and my YouTube Channel!
 *  
 */

//import required classes
import weka.core.Instances;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.core.converters.ConverterUtils.DataSource;



public class TestWekaFeatureSelection{
	public static void main(String args[]) throws Exception{
				DataSource source2 = new DataSource("C:/Program Files/Weka-3-8/data/breast-cancerNOTK.arff");
				Instances testingNoFilter = source2.getDataSet();
				
		
				DataSource source = new DataSource("C:/Program Files/Weka-3-8/data/breast-cancerKnown.arff");
				Instances noFilterTraining = source.getDataSet();
				//create AttributeSelection object
				AttributeSelection filter = new AttributeSelection();
				//create evaluator and search algorithm objects
				CfsSubsetEval eval = new CfsSubsetEval();
				GreedyStepwise search = new GreedyStepwise();
				//set the algorithm to search backward
				search.setSearchBackwards(true);
				//set the filter to use the evaluator and search algorithm
				filter.setEvaluator(eval);
				filter.setSearch(search);
				//specify the dataset
				filter.setInputFormat(noFilterTraining);
				//apply
				Instances filteredTraining = Filter.useFilter(noFilterTraining, filter);
				
				int numAttrNoFilter = noFilterTraining.numAttributes();
				noFilterTraining.setClassIndex(numAttrNoFilter - 1);
				testingNoFilter.setClassIndex(numAttrNoFilter - 1);
				
				int numAttrFiltered = filteredTraining.numAttributes();
	
				
				System.out.println("No filter attr: "+ numAttrNoFilter);
				System.out.println("Filtered attr: "+ numAttrFiltered);
				
				RandomForest classifier = new RandomForest();

				
				//evaluation with no filtered
				Evaluation evalClass = new Evaluation(testingNoFilter);
				classifier.buildClassifier(noFilterTraining);
			    evalClass.evaluateModel(classifier, testingNoFilter); 
				
				System.out.println("AUC no filter = "+evalClass.areaUnderROC(1));
				System.out.println("Kappa no filter = "+evalClass.kappa());
			
				//evaluation with filtered
				filteredTraining.setClassIndex(numAttrFiltered - 1);
				Instances testingFiltered = Filter.useFilter(testingNoFilter, filter);
				testingFiltered.setClassIndex(numAttrFiltered - 1);
				classifier.buildClassifier(filteredTraining);
			    evalClass.evaluateModel(classifier, testingFiltered);
				
				System.out.println("AUC filtered = "+evalClass.areaUnderROC(1));
				System.out.println("Kappa filtered = "+evalClass.kappa());



	}
}
