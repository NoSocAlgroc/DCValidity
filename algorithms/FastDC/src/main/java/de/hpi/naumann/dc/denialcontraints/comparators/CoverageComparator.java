package de.hpi.naumann.dc.denialcontraints.comparators;

import java.util.Comparator;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;

public final class CoverageComparator implements Comparator<DenialConstraint> {
	private boolean inverse;

	public CoverageComparator() {
		this.inverse = false;
	}

	public CoverageComparator(boolean inverse) {
		this.inverse = inverse;
	}

	@Override
	public int compare(DenialConstraint o1, DenialConstraint o2) {
		if (inverse) {
			return Double.compare(o2.getFeatures().getCoverage(), o1.getFeatures().getCoverage());
		} else {
			return Double.compare(o1.getFeatures().getCoverage(), o2.getFeatures().getCoverage());
		}

	}
}