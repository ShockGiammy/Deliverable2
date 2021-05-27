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


import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;


public class TestWekaSampling{
	public static void main(String args[]) throws Exception{
		//load datasets
				DataSource source1 = new DataSource("C:/Program Files/Weka-3-8/data/breast-cancerKnown.arff");
				Instances training = source1.getDataSet();
				DataSource source2 = new DataSource("C:/Program Files/Weka-3-8/data/breast-cancerNOTK.arff");
				Instances testing = source2.getDataSet();
				

				
				int numAttr = training.numAttributes();
				training.setClassIndex(numAttr - 1);
				testing.setClassIndex(numAttr - 1);

				

				RandomForest RandomForest = new RandomForest();

				RandomForest.buildClassifier(training);


				Evaluation eval = new Evaluation(testing);	
				eval.evaluateModel(RandomForest, testing); //not sampled

				

				
				
		    	Resample resample = new Resample();
				resample.setInputFormat(training);
				FilteredClassifier fc = new FilteredClassifier();

				
				RandomForest RandomForest2 = new RandomForest();
				fc.setClassifier(RandomForest2);
				
				fc.setFilter(resample);
				//eventual parameters setting omitted
				
				
				SMOTE smote = new SMOTE();
				smote.setInputFormat(training);
				fc.setFilter(smote);
				
				SpreadSubsample  spreadSubsample = new SpreadSubsample();
				String[] opts = new String[]{ "-M", "1.0"};
				spreadSubsample.setOptions(opts);
				fc.setFilter(spreadSubsample);
				
				fc.buildClassifier(training);
				Evaluation eval2 = new Evaluation(testing);	
				eval2.evaluateModel(fc, testing); //sampled
				
				System.out.println("Correct% nonsampled = "+eval.pctCorrect());
				System.out.println("Correct% sampled= "+eval2.pctCorrect()+ "\n");
				
				System.out.println("Precision nonsampled= "+eval.precision(1));
				System.out.println("Precision sampled= "+eval2.precision(1)+ "\n");
				
				System.out.println("Recall nonsampled= "+eval.recall(1));			
				System.out.println("Recall sampled= "+eval2.recall(1)+ "\n");
				
	}
}
