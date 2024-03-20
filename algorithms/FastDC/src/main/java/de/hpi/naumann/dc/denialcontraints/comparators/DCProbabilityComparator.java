package de.hpi.naumann.dc.denialcontraints.comparators;

import java.util.Comparator;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;

public final class DCProbabilityComparator implements Comparator<DenialConstraint> {
	private boolean inverse;

	public DCProbabilityComparator() {
		this.inverse = false;
	}

	public DCProbabilityComparator(boolean inverse) {
		this.inverse = inverse;
	}

	@Override
	public int compare(DenialConstraint o1, DenialConstraint o2) {
		if (inverse) {
			return Double.compare(o2.getFeatures().getViolationProbabilities()[0], o1.getFeatures().getViolationProbabilities()[0]);
		} else {
			return Double.compare(o1.getFeatures().getViolationProbabilities()[0], o2.getFeatures().getViolationProbabilities()[0]);
		}

	}
}