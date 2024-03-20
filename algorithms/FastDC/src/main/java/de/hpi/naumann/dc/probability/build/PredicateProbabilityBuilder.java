package de.hpi.naumann.dc.probability.build;

import java.text.DecimalFormat;

import ch.javasoft.bitset.IBitSet;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;

public class PredicateProbabilityBuilder implements IPredicateProbabilityBuilder {

	IEvidenceSet evidenceSet;
	PredicateBuilder predicates;

	public PredicateProbabilityBuilder(IEvidenceSet evidenceSet, PredicateBuilder predicates) {
		this.evidenceSet = evidenceSet;
		this.predicates = predicates;
	}

	@Override
	public void buildIndividualProbabilities() {
		// try other implementations, use a vector of |P| positions, run through the evi
		// and them increment the prob for each predicate
		for (Predicate p : predicates.getPredicates()) {
			double probP = 0.0D;
			for (IPredicateSetBinary evi : evidenceSet) {
				if (evi.containsPredicate(p)) {
					probP += evidenceSet.getCount(evi);
				}
			}
			probP = probP / evidenceSet.getTotalCountEvi();
			p.setProbability(probP);

		}
	}
	
	
	public void findProblematicParts(DenialConstraint dc) {
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		
		double probs[]= dc.getFeatures().getViolationProbabilities();
		
		int topProbIdx = 0;
		for (int i = 0; i < probs.length; i++) {
			topProbIdx = probs[i] > probs[topProbIdx] ? i : topProbIdx;
		}
		System.out.println(dc + " with " + df.format(probs[topProbIdx]) +" prob of being violated at level " + topProbIdx);
		for(Predicate p: dc.getPredicateSet()) {
			System.out.print(p+" has prob "+ df.format(p.getProbability())+" | ");
		}
		System.out.println();
		System.out.println();
		
		
	}
	

	public void probability(DenialConstraint dc) {
		
		
		IBitSet dcBitset= dc.getPredicateSet().getIPredicateSetBinary().getBitset();
		
		
		double level0=0;
		double level1=0;
		double level2=0;
		double level3=0;
		double level4=0;
		
		for(IPredicateSetBinary evidenceBitset: evidenceSet) {
			
			
			if(evidenceBitset.getBitset().getAndCardinality(dcBitset) == (dc.getPredicateSet().size())) {
				level0+=evidenceSet.getCount(evidenceBitset);
			}
			
			if(evidenceBitset.getBitset().getAndCardinality(dcBitset) == (dc.getPredicateSet().size()-1)) {
				level1+=evidenceSet.getCount(evidenceBitset);
			}
			
			if(evidenceBitset.getBitset().getAndCardinality(dcBitset) == (dc.getPredicateSet().size()-2)) {
				level2+=evidenceSet.getCount(evidenceBitset);
			}
			
			if(evidenceBitset.getBitset().getAndCardinality(dcBitset) == (dc.getPredicateSet().size()-3)) {
				level3+=evidenceSet.getCount(evidenceBitset);
			}
			
			if(evidenceBitset.getBitset().getAndCardinality(dcBitset) == (dc.getPredicateSet().size()-4)) {
				level4+=evidenceSet.getCount(evidenceBitset);
			}
		}
		
		 System.out.println("Probability of DC being violated by every predicate (level(0)): "+level0);
		 System.out.println("Probability of DC being violated by one predicate (level(1)): "+level1);
		 System.out.println("Probability of DC being violated by two predicate (level(2)): "+level2);
		 System.out.println("Probability of DC being violated by three predicate (level(3)): "+level3);
		 System.out.println("Probability of DC being violated by four predicate (level(4)): "+level4);
		
		
		
		
	}

}
