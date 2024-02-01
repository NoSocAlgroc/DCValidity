package de.metanome.algorithms.dcfinder.denialconstraints;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.Closure;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;

import java.util.List;


public class DenialConstraint {

	private PredicateSet predicateSet;

	public DenialConstraint(Predicate... predicates) {
		predicateSet = PredicateSetFactory.create(predicates);
	}


	public DenialConstraint(PredicateSet predicateSet) {
		this.predicateSet = predicateSet;
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

	public boolean isImpliedBy(NTreeSearch tree, PredicateSet closure) {
		IBitSet subset = tree.getSubset(PredicateSetFactory.create(closure).getBitset());
		if (subset != null) {
			return true;
		}

		DenialConstraint sym = getInvT1T2DC();
		if (sym != null) {
			Closure c = new Closure(sym.getPredicateSet());
			if (!c.construct())
				return true;
			IBitSet subset2 = tree.getSubset(PredicateSetFactory.create(c.getClosure()).getBitset());
			return subset2 != null;
		}

		return false;

	}

	public boolean containsPredicate(Predicate p) {
		return predicateSet.containsPredicate(p) || predicateSet.containsPredicate(p.getSymmetric());
	}

	public DenialConstraint getInvT1T2DC() {
		PredicateSet invT1T2 = PredicateSetFactory.create();
		for (Predicate predicate : predicateSet) {
			Predicate sym = predicate.getInvT1T2();
			if (sym == null)
				return null;
			invT1T2.add(sym);
		}
		return new DenialConstraint(invT1T2);
	}

	public PredicateSet getPredicateSet() {
		return predicateSet;
	}

	public int getPredicateCount() {
		return predicateSet.size();
	}

	private boolean containedIn(PredicateSet otherPS) {
		for (Predicate p : predicateSet) {
			if (!otherPS.containsPredicate(p) && !otherPS.containsPredicate(p.getSymmetric()))
				return false;
		}
		return true;
	}


	@Override
	public int hashCode() {
		// final int prime = 31;
		int result1 = 0;
		for (Predicate p : predicateSet) {
			result1 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
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
			PredicateSet otherPS = other.predicateSet;
			return containedIn(otherPS) || getInvT1T2DC().containedIn(otherPS)
					|| containedIn(other.getInvT1T2DC().predicateSet);
		}
	}

	/* Features */

	private double support;
	private double wsupport;
	private double confidence;
	private double cosine;
	private double coverage;
	private long violations;

	public void setConfidenceMeasures(double support, double confidence, double cosine) {
		this.support = support;
		this.confidence = confidence;
		this.cosine = cosine;

	}

	public void setConfidenceMeasures(double support, double wsupport, double confidence, double cosine) {

		this.support = support;
		this.wsupport = wsupport;
		this.confidence = confidence;
		this.cosine = cosine;

	}

	public double getWsupport() {
		return wsupport;
	}

	public void setWsupport(double wsupport) {
		this.wsupport = wsupport;
	}

	public double getSupport() {
		return support;
	}

	public void setSupport(double support) {
		this.support = support;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public double getCosine() {
		return cosine;
	}

	public void setCosine(double cosine) {
		this.cosine = cosine;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public long getViolations() {
		return violations;
	}

	public void setViolations(long violations) {
		this.violations = violations;
	}
	public static final String NOT = "\u00AC";
	public static final String AND = "∧";
	public String toString() {
		StringBuffer s = new StringBuffer();
		//s.append(NOT+"("+getPredicate(0).toString());
		s.append(NOT+"(");
		int count = 0;
		for (Predicate predicate : this.predicateSet) {
			//s.append(AND+predicate.toString());
			if(count == 0){
				s.append(predicate.toString());
			}
			else{
				s.append(AND+predicate.toString());
			}
			count++;
		}
		return s.toString();
//		StringBuffer s = new StringBuffer();
//		s.append("[DC: ");
//		int cou = 0;
//		for (Predicate p : predicateSet) {
////			if(p.getOperator()==Operator.UNEQUAL) count++;
////			s=s+ "["+p.getOperand1().getColumn().getColumnIdentifier()+"]"+p.getOperator()+",";
//			//s.append(NOT+"("+getPredicate(0).toString());
//			s.append(NOT+"(");
//			//s.append(AND+predicate.toString());
//			if(cou == 0){
//				s.append(p.toString());
//			}
//			else{
//				s.append(AND+p.toString());
//			}
//			cou++;
//
//		}
//		s.append("]");
////		if(count>=2) System.out.println(s);
////		s.append("\n");
//		return s.toString();
	}


}
