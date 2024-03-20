package de.hpi.naumann.dc.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import de.hpi.naumann.dc.algorithms.FastDCMetanome;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.backend.input.file.DefaultFileInputGenerator;

public class ExperimentDifferentDatasets {

	public static File selectFile() {

		//return new File("/home/eduardo/doutorado_code/hpi/datasets/example/example.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/datasets/spstock/spstock.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/datasets/sid/sid.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/datasets/flights/flights.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/datasets/despesas/despesas.csv");
		return new File("/home/eduardo/doutorado_code/hpi/datasets/tax/tax.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/datasets/minitax/minitax.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/datasets/driver/driver.csv");

	}

	public static void main(String[] args) {

		

		ConfigurationSettingFileInput conf = new ConfigurationSettingFileInput("input file", true, ',', '\'', '\n', false,
				false, 0, true, true, "NULL");

		File file = selectFile();

		FastDCMetanome fastdcRunner = new FastDCMetanome();

		RelationalInputGenerator inputGen = null;

		try {

			inputGen = new DefaultFileInputGenerator(file, conf);
			fastdcRunner.setRelationalInputConfigurationValue("input file", inputGen);
			fastdcRunner.setIntegerConfigurationValue(FastDCMetanome.INPUT_ROW_LIMIT, 50000);
			fastdcRunner.execute();

		} catch (AlgorithmConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (AlgorithmExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}
