package de.hpi.naumann.dc.algorithms;

import java.io.File;
import java.io.FileNotFoundException;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.backend.input.file.DefaultFileInputGenerator;

public class App {

	public static File selectFile() {

		//return new File("/home/eduardo/doutorado_code/hpi/adc/adc/datasets/example.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/adc/adc/datasets/1000_tax.csv");
		//return new File("/home/eduardo/Downloads/FastDC_cover_search_%2f%2f_AHYDRA/AHYDRA/AHYDRA/datasets/tax6cols.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/adc/adc/datasets/500tax.csv");
		//return new File("/home/eduardo/doutorado_code/hpi/fastdc/datasets/Demographic_Statistics_By_Zip_Code.csv");
		return new File("/home/eduardo/doutorado_code/hpi/fastdc/datasets/10000_tax.csv");

	}

	public static void main(String[] args) {
		

		ConfigurationSettingFileInput conf = new ConfigurationSettingFileInput("test", true, ',', '\'', '\n', false,
				false, 0, true, true, "NULL");
		File file = selectFile();
		
		FastDCMetanome fastdcRunner = new FastDCMetanome();
		RelationalInputGenerator inputGen = null;

		try {
			inputGen = new DefaultFileInputGenerator(file, conf);
			fastdcRunner.setRelationalInputConfigurationValue("input file", inputGen);
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
