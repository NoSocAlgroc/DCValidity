package de.hpi.naumann.dc.denialcontraints.comparators;

import java.util.Comparator;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;

public final class PredicateCountComparator implements Comparator<DenialConstraint> {
	private boolean inverse;

	public PredicateCountComparator() {
		this.inverse = false;
	}

	public PredicateCountComparator(boolean inverse) {
		this.inverse = inverse;
	}

	@Override
	public int compare(DenialConstraint o1, DenialConstraint o2) {
		if (inverse) {
			return Integer.compare(o2.getPredicateCount(), o1.getPredicateCount());
		} else {
			return Integer.compare(o1.getPredicateCount(), o2.getPredicateCount());
		}

	}
}