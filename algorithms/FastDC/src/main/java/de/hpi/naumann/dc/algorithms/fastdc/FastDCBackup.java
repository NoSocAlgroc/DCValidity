package de.hpi.naumann.dc.algorithms.fastdc;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.hpi.naumann.dc.algorithms.IDCAlgorithm;
import de.hpi.naumann.dc.cover.FastDCMinimalCoverSearch;
import de.hpi.naumann.dc.cover.IMinimalCoverSearch;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.denialcontraints.classifiers.DCClassifier;
import de.hpi.naumann.dc.denialcontraints.comparators.InterestingnessComparator;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.evidenceset.build.IEvidenceSetBuilder;
import de.hpi.naumann.dc.evidenceset.build.ParallelEvidenceSetBuilder;
import de.hpi.naumann.dc.experiments.reports.Reporter;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;

public class FastDCBackup implements IDCAlgorithm {

	@Override
	public DenialConstraintSet run(Input input, PredicateBuilder predicates) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		IEvidenceSetBuilder evidenceSetbuilder = new ParallelEvidenceSetBuilder(predicates);
		IEvidenceSet evidenceSet = evidenceSetbuilder.buildEvidenceSet(input);

		IMinimalCoverSearch minimalCoverSearch = new FastDCMinimalCoverSearch(predicates.getPredicates());
		DenialConstraintSet dcs = minimalCoverSearch.discoverDenialConstraints(evidenceSet);
		dcs.minimalize();


		DCClassifier classifier = new DCClassifier(dcs);
		classifier.classifyDCs();
		DenialConstraintSet brules = classifier.getBrules();
		DenialConstraintSet uccs = classifier.getUccs();
		DenialConstraintSet fds = classifier.getFds();

		DenialConstraint sortedBRules[] = estimateAndStoreDCFeatures(brules, evidenceSet);
		DenialConstraint sortedUCCs[] = estimateAndStoreDCFeatures(uccs, evidenceSet);
		DenialConstraint sortedFDs[] = estimateAndStoreDCFeatures(fds, evidenceSet);

		Reporter.context.put("brules", sortedBRules);
		Reporter.context.put("uccs", sortedUCCs);
		Reporter.context.put("fds", sortedFDs);
		Reporter.context.put("numdcs", dcs.size());

		// for(DenialConstraint dc : sortedDCs) {
		// System.out.println(dc + " " + dc.getFeatures().getInterestingness());
		// }

		return dcs;
		// return null;
	}

	public DenialConstraint[] estimateAndStoreDCFeatures(DenialConstraintSet dcs, IEvidenceSet evidenceSet) {

		DenialConstraint sortedDCs[] = dcs.stream().toArray(DenialConstraint[]::new);

		for (DenialConstraint dc : sortedDCs) {
			dc.setCoverageAndVioProb(evidenceSet);
			dc.setSuccinctness();
		}

		Arrays.sort(sortedDCs, new InterestingnessComparator(true));
		return sortedDCs;

	}

	private static Logger log = LoggerFactory.getLogger(FastDCBackup.class);

}
