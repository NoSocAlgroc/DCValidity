package de.hpi.naumann.dc.algorithms;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.algorithms.fastdc.AFastDC;
import de.hpi.naumann.dc.algorithms.fastdc.FastDC;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.experiments.reports.Reporter;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.PredicateSerializer;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public class FastDCMetanome
		implements RelationalInputParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm {
	public static final String INPUT_RELATION_FILE = "input file";
	
	public static final String INPUT_ROW_LIMIT = "number of lines";
	//public static final String INPUT_ROW_LIMIT = "1000";
	public static final String INPUT_PREDICATES_FILE = "predicate file";
	public static final String INPUT_EVIDENCE_FILE = "input evidence file";

	private RelationalInputGenerator inputGenerator;
	private Input input;
	private int rowLimit;
	private String predicateFile;
	private String evidenceFile;
	
	
	
	private static Logger log = LoggerFactory.getLogger(FastDCMetanome.class);

	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> reqs = new ArrayList<>();
		reqs.add(new ConfigurationRequirementRelationalInput(INPUT_RELATION_FILE));
		reqs.add(new ConfigurationRequirementRelationalInput(INPUT_ROW_LIMIT));
		reqs.add(new ConfigurationRequirementRelationalInput(INPUT_PREDICATES_FILE));
		reqs.add(new ConfigurationRequirementRelationalInput(INPUT_EVIDENCE_FILE));
		return reqs;
	}
	
	FastDC algorithmExecution;
	
	

	



	public void execute() throws AlgorithmExecutionException {
		
		
		input = new Input(inputGenerator.generateNewCopy(), rowLimit);
		log.info("Read " + input.getLineCount() + " lines.");
		
		String dataset=input.name.replace(".csv", "");		
		Reporter.initialize(dataset);
		Reporter.context.put("dataset", dataset);
		Reporter.context.put("numrecords", input.getLineCount());
		Reporter.context.put("numcols", input.getColumns().length);
		
	
		
	
		
		PredicateBuilder predicateBuilder = null;
		if (predicateFile != null) {
			predicateBuilder = PredicateSerializer.readPredicateSpace(getPredicatePath(), input);
		}
		if (predicateBuilder == null) {
			predicateBuilder = new PredicateBuilder(input);
		}
		
//		for(Predicate p: predicateBuilder.getPredicates()) {
//			System.out.println(p);
//		}

		log.info("Predicate Space Size: " + predicateBuilder.getPredicates().size() );

		IDCAlgorithm  algorithm = new FastDC();
		//IDCAlgorithm  algorithm = new AFastDC();
		
		DenialConstraintSet result =  algorithm.run(input, predicateBuilder);
		
		//algorithmExecution = (AFastDC) algorithm;


		
		Reporter.generateReport();

	}
	
	public FastDC getAlgorithmExecution() {
		return algorithmExecution;
	}
	
	

	private String getEvidencePath() {
		return System.getProperty("user.dir") + "/" + evidenceFile;
	}

	private String getPredicatePath() {
		return System.getProperty("user.dir") + "/" + predicateFile;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
			throws AlgorithmConfigurationException {
		if (identifier.equals(INPUT_RELATION_FILE)) {
			this.inputGenerator = values[0];
		}
	}

	

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values)
			throws AlgorithmConfigurationException {
		
		if (identifier.equals(INPUT_ROW_LIMIT)) {
			
			this.rowLimit = values[0];
		}
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values)
			throws AlgorithmConfigurationException {
		if (identifier.equals(INPUT_PREDICATES_FILE) && values != null) {
			this.predicateFile = values[0];
		}
		if (identifier.equals(INPUT_EVIDENCE_FILE) && values != null) {
			this.evidenceFile = values[0];
		}
		
	}
	
	

	@Override
	public String getAuthors() {
		
		return null;
	}

	@Override
	public String getDescription() {
		
		return null;
	}

}
