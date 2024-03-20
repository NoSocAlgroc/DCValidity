package de.hpi.naumann.dc.algorithms.fastdc;

import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.ISubsetBackend;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.algorithms.IDCAlgorithm;
import de.hpi.naumann.dc.cover.FastDCMinimalCoverSearch;
import de.hpi.naumann.dc.cover.IMinimalCoverSearch;
import de.hpi.naumann.dc.data_structures.PredicateCombinationBitSet;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.denialcontraints.classifiers.DCClassifier;
import de.hpi.naumann.dc.denialcontraints.comparators.CoverageComparator;
import de.hpi.naumann.dc.denialcontraints.comparators.InterestingnessComparator;
import de.hpi.naumann.dc.evidenceset.AtomicLongEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.evidenceset.build.IEvidenceSetBuilder;
import de.hpi.naumann.dc.evidenceset.build.ParallelEvidenceSetBuilder;
import de.hpi.naumann.dc.experiments.reports.Reporter;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;
import de.hpi.naumann.dc.probability.build.IPredicateProbabilityBuilder;
import de.hpi.naumann.dc.probability.build.PredicateProbabilityBuilder;
import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

public class FastDC implements IDCAlgorithm {

	@Override
	public DenialConstraintSet run(Input input, PredicateBuilder predicates) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		IEvidenceSetBuilder evidenceSetbuilder = new ParallelEvidenceSetBuilder(predicates);
		IEvidenceSet evidenceSet = evidenceSetbuilder.buildEvidenceSet(input);

		Iterator<IPredicateSetBinary> iter = evidenceSet.iterator();

		// while (iter.hasNext()) {
		// IPredicateSetBinary evi = iter.next();
		// System.out.println(
		// PredicateBitSet.indexProvider.getObjects(evi.getBitset()) + ": " +
		// evidenceSet.getCount(evi));
		// }

		IMinimalCoverSearch minimalCoverSearch = new FastDCMinimalCoverSearch(predicates.getPredicates());
		DenialConstraintSet dcs = minimalCoverSearch.discoverDenialConstraints(evidenceSet);

		//DenialConstraint sortedDCS[] = estimateAndStoreDCFeatures(dcs, evidenceSet);

		// System.out.println();
		// for (DenialConstraint dc : sortedDCS) {
		// System.out.println(dc + " cov:" + dc.getFeatures().getCoverage());
		// }
		// System.out.println(dcs.size());
		
		
		IPredicateProbabilityBuilder ppbuilder = new PredicateProbabilityBuilder(evidenceSet, predicates);
		ppbuilder.buildIndividualProbabilities();
		//Reporter.context.put("predicates", predicates.getPredicates());

		DCClassifier classifier = new DCClassifier(dcs);
		classifier.classifyDCs();
		//DenialConstraintSet brules = classifier.getBrules();
		//DenialConstraintSet uccs = classifier.getUccs();
		//DenialConstraintSet fds = classifier.getFds();

		//DenialConstraint sortedBRules[] = estimateAndStoreDCFeatures(brules, evidenceSet);
		//DenialConstraint sortedUCCs[] = estimateAndStoreDCFeatures(uccs, evidenceSet);
		//DenialConstraint sortedFDs[] = estimateAndStoreDCFeatures(fds, evidenceSet);

		//Reporter.context.put("brules", sortedBRules);
		//Reporter.context.put("uccs", sortedUCCs);
		//Reporter.context.put("fds", sortedFDs);
		//Reporter.context.put("numdcs", dcs.size());
		

		

		


		
	

		// DenialConstraint sortedBRules[] = estimateAndStoreDCFeatures(dcs,
		// evidenceSet);
		//

		// return null;
		return dcs;
	}

	public DenialConstraint[] estimateAndStoreDCFeatures(DenialConstraintSet dcs, IEvidenceSet evidenceSet) {

		DenialConstraint sortedDCs[] = dcs.stream().toArray(DenialConstraint[]::new);

		for (DenialConstraint dc : sortedDCs) {
			dc.setCoverageAndVioProb(evidenceSet);
			dc.setSuccinctness();
			
			
			
			double predicateMulti=1;
			for(Predicate p: dc.getPredicateSet()) {
				predicateMulti=predicateMulti*p.getProbability();
			}
			dc.getFeatures().setPredicateMulti(predicateMulti);
			
		}

		Arrays.sort(sortedDCs, new InterestingnessComparator(true));
		// Arrays.sort(sortedDCs, new CoverageComparator(true));
		// Arrays.sort(sortedDCs, new DCProbabilityComparator(true));
		return sortedDCs;

	}

	private static Logger log = LoggerFactory.getLogger(FastDC.class);

}
