package weka;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CSV2Arff {
	
	private CSV2Arff() {
	    throw new IllegalStateException("Utility class");
	}

	  /**
	   * takes 2 arguments:
	   * - CSV input file
	   * - ARFF output file
	 * @param file 
	 * @param fileArff 
	 * @return 
	 * @throws Exception 
	   */
	  public static void convertCsv2Arff(File file, File fileArff) throws Exception {

	    // load CSV
	    var loader = new CSVLoader();
	    loader.setSource(file);
	    Instances data = loader.getDataSet();

	    // save ARFF
	    var saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(fileArff);
	    saver.writeBatch();
	    
	    List<String> lines = Files.readAllLines(fileArff.toPath(), StandardCharsets.UTF_8);
	    for (String line : lines) {
	    	if (line.contains("bugginess")) {
	    		lines.set(lines.indexOf(line), "@attribute bugginess {YES,NO}");
	    	}
	    }
	    Files.write(fileArff.toPath(), lines, StandardCharsets.UTF_8);
	    
	  }
}