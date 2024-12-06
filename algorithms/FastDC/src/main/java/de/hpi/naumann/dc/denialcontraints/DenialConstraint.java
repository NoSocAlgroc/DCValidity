package de.hpi.naumann.dc.denialcontraints;

import java.util.HashSet;
import java.util.Set;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.predicates.Operator;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.sets.Closure;
import de.hpi.naumann.dc.predicates.sets.IPredicateSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;
import de.metanome.algorithm_integration.PredicateVariable;

public class DenialConstraint {

	private IPredicateSet predicateSet;
	private DCFeatures features;

	public DenialConstraint(Predicate... predicates) {
		predicateSet = PredicateSetFactory.create(predicates);
		features = new DCFeatures();
	}

	public DenialConstraint(IPredicateSet predicateSet) {
		this.predicateSet = predicateSet;
		features = new DCFeatures();
	}

	public boolean isTrivial() {
		return !new Closure(predicateSet).construct();
	}

	public boolean isImpliedBy(NTreeSearch tree) {
		Closure c = new Closure(predicateSet);
		if (!c.construct())
			return true;

		return isImpliedBy(tree, c.getClosure());
	}

	public boolean isImpliedBy(NTreeSearch tree, IPredicateSet closure) {
		IBitSet subset = tree.getSubset(PredicateSetFactory.create2(closure).getBitset());
		if (subset != null) {
			return true;
		}

		DenialConstraint sym = getInvT1T2DC();
		if (sym != null) {
			Closure c = new Closure(sym.getPredicateSet());
			if (!c.construct())
				return true;
			IBitSet subset2 = tree.getSubset(PredicateSetFactory.create2(c.getClosure()).getBitset());
			return subset2 != null;
		}

		return false;

	}

	public boolean containsPredicate(Predicate p) {
		return predicateSet.containsPredicate(p) || predicateSet.containsPredicate(p.getSymmetric());
	}

	public DenialConstraint getInvT1T2DC() {
		IPredicateSet invT1T2 = PredicateSetFactory.create();
		for (Predicate predicate : predicateSet) {
			Predicate sym = predicate.getInvT1T2();
			if (sym == null)
				return null;
			invT1T2.add(sym);
		}
		return new DenialConstraint(invT1T2);
	}

	public IPredicateSet getPredicateSet() {
		return predicateSet;
	}

	/*
	 * RANKING - SUCCINCTNESS
	 */

	public int getPredicateCount() {
		return predicateSet.size();
	}

	public double getSuccinctnessPredicateCount() {
		return 1.0d / getPredicateCount();
	}

	public int getAlphabetSize() {
		Set<Object> alphabet = new HashSet<>();
		for (Predicate p : predicateSet) {
			alphabet.addAll(p.getAlphabet());
		}

		return alphabet.size();

	}

	public void setSuccinctness() {
		double succinctness = 4.0d / getAlphabetSize(); // The shortest possible DC is of length 4,
		features.setSuccinctness(succinctness);
	}

	/*
	 * RANKING - COVERAGE
	 */

	public void setCoverageAndVioProb(IEvidenceSet evidenceSet) {
		// int[] kEvidenceCounts = evidenceSet.getKEvidencesCounts(this);
		long[] kEvidenceCounts = evidenceSet.getKEvidencesCounts(this);

		long totalCount = 0;
		double weightedCount = 0;
		double weightForKe = 0;

		for (int k = 0; k < getPredicateCount(); ++k) {
			totalCount += kEvidenceCounts[k];
			weightForKe = (k + 1) / getPredicateCount();
			weightedCount += kEvidenceCounts[k] * weightForKe;
		}

		double coverage = weightedCount / totalCount;
		features.setCoverage(coverage);

		double violationProbabilities[] = new double[kEvidenceCounts.length];
		for (int i = 0; i < kEvidenceCounts.length; i++) {
			violationProbabilities[i] = (double) kEvidenceCounts[i] / evidenceSet.getTotalCountEvi();
		}
		features.setViolationProbabilities(violationProbabilities);
		features.setCounters(kEvidenceCounts);
	}

	/*
	 * RANKING - INTERESTINGNESS
	 */
	// public double getInterestingness(double alpha, IEvidenceSet evidenceSet) {
	// return alpha * getSuccinctness() + (1.0 - alpha) * getCoverage(evidenceSet);
	// }

	@Override
	public String toString() {
		// return "[DC: " + predicateSet.toString() + "]";
		// return predicateSet.toString() ;
		return "not( " + predicateSet.toString() + " )";
	}

	public String invToString() {
		StringBuilder sb = new StringBuilder();
		for (Predicate p : predicateSet.getPredicates()) {
			sb.append(p.getInverse() + ", ");
		}

		return sb.toString();
	}

	// @Override
	// public int hashCode() {
	// final int prime = 31;
	// int result = 1;
	// result = prime * result + ((predicateSet == null) ? 0 :
	// predicateSet.hashCode());
	// return result;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (getClass() != obj.getClass())
	// return false;
	// DenialConstraint other = (DenialConstraint) obj;
	// if (predicateSet == null) {
	// if (other.predicateSet != null)
	// return false;
	// } else if (!predicateSet.equals(other.predicateSet))
	// return false;
	// return true;
	// }

	@Override
	public int hashCode() {
		// final int prime = 31;
		// int result = 1;
		// result = prime * result + ((predicateSet == null) ? 0 :
		// predicateSet.hashCode());
		int result1 = 0;
		for (Predicate p : predicateSet) {
			result1 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
			// result += ;
		}
		int result2 = 0;
		if (getInvT1T2DC() != null)
			for (Predicate p : getInvT1T2DC().predicateSet) {
				result2 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
			}
		return Math.max(result1, result2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DenialConstraint other = (DenialConstraint) obj;
		if (predicateSet == null) {
			return other.predicateSet == null;
		} else if (predicateSet.size() != other.predicateSet.size()) {
			return false;
		} else {
			IPredicateSet otherPS = other.predicateSet;
			return containedIn(otherPS) || getInvT1T2DC().containedIn(otherPS)
					|| containedIn(other.getInvT1T2DC().predicateSet);
		}
	}

	private boolean containedIn(IPredicateSet otherPS) {
		for (Predicate p : predicateSet) {
			if (!otherPS.containsPredicate(p) && !otherPS.containsPredicate(p.getSymmetric()))
				return false;
		}
		return true;
	}

	public DCFeatures getFeatures() {
		return features;
	}

	public void setFeatures(DCFeatures features) {
		this.features = features;
	}


	public de.metanome.algorithm_integration.results.DenialConstraint toResult() {
		PredicateVariable[] predicates = new PredicateVariable[predicateSet.size()];
		int i = 0;
		for (Predicate p : predicateSet) {
			Operator o1=p.getOperator();
			de.metanome.algorithm_integration.Operator o2=null;
			if(o1==Operator.EQUAL) o2=de.metanome.algorithm_integration.Operator.EQUAL;
			else if(o1==Operator.UNEQUAL) o2=de.metanome.algorithm_integration.Operator.UNEQUAL;
			else if(o1==Operator.GREATER) o2=de.metanome.algorithm_integration.Operator.GREATER;
			else if(o1==Operator.GREATER_EQUAL) o2=de.metanome.algorithm_integration.Operator.GREATER_EQUAL;
			else if(o1==Operator.LESS) o2=de.metanome.algorithm_integration.Operator.LESS;
			else if(o1==Operator.LESS_EQUAL) o2=de.metanome.algorithm_integration.Operator.LESS_EQUAL;
			predicates[i] = new PredicateVariable(((ColumnOperand<?>) p.getOperand1()).getColumn().getColumnIdentifier(),
			((ColumnOperand<?>) p.getOperand1()).getIndex(), o2, ((ColumnOperand<?>) p.getOperand2()).getColumn().getColumnIdentifier(),
			((ColumnOperand<?>) p.getOperand2()).getIndex());
			++i;
		}

		return new de.metanome.algorithm_integration.results.DenialConstraint(predicates);
	}
}
