package de.hpi.naumann.dc.algorithms.fastdc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.hpi.naumann.dc.algorithms.IDCAlgorithm;
import de.hpi.naumann.dc.cover.AFastDCMinimalCoverSearch;
import de.hpi.naumann.dc.cover.IAMinimalCoverSearch;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.evidenceset.build.IEvidenceSetBuilder;
import de.hpi.naumann.dc.evidenceset.build.ParallelEvidenceSetBuilder;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;

public class AFastDC extends FastDC implements IDCAlgorithm {

	@Override
	public DenialConstraintSet run(Input input, PredicateBuilder predicates) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		IEvidenceSetBuilder evidenceSetbuilder = new ParallelEvidenceSetBuilder(predicates);
		IEvidenceSet evidenceSet = evidenceSetbuilder.buildEvidenceSet(input);

		
		
		IAMinimalCoverSearch minimalCoverSearch;
		DenialConstraintSet dcs=null;
		
		
		
		
		Map<Double,Integer> results = new HashMap<Double,Integer>();
		
		
		
		for(double approximmation=0.0000001; approximmation<= 0.0000025; approximmation=approximmation+0.0000001) {
			
			minimalCoverSearch = new AFastDCMinimalCoverSearch(predicates.getPredicates());
			dcs = minimalCoverSearch.discoverDenialConstraints(evidenceSet, approximmation);
			//System.out.println(dcs);
			//System.out.println("Aproximation Level" + approximmation + " dcs " + dcs.size());
			results.put(approximmation,dcs.size());
		}
//		
//
//		
//		
//		
		for(Map.Entry<Double,Integer> result: results.entrySet()) {
			System.out.println("Appr. level: " + result.getKey() + " n dcs. " +result.getValue());
		}
//		

		

		return dcs;
	}

	private static Logger log = LoggerFactory.getLogger(AFastDC.class);

}
