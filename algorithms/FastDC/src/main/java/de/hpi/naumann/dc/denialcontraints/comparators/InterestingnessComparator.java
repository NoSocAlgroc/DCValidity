package de.hpi.naumann.dc.denialcontraints.comparators;

import java.util.Comparator;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;

public final class InterestingnessComparator implements Comparator<DenialConstraint> {
	private boolean inverse;

	public InterestingnessComparator() {
		this.inverse = false;
	}

	public InterestingnessComparator(boolean inverse) {
		this.inverse = inverse;
	}

	@Override
	public int compare(DenialConstraint o1, DenialConstraint o2) {
		if (inverse) {
			return Double.compare(o2.getFeatures().getInterestingness(), o1.getFeatures().getInterestingness());
		} else {
			return Double.compare(o1.getFeatures().getInterestingness(), o2.getFeatures().getInterestingness());
		}

	}
}