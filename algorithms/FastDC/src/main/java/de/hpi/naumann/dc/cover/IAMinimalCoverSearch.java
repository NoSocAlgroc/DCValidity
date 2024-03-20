package de.hpi.naumann.dc.cover;

import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;

public interface IAMinimalCoverSearch {
	public DenialConstraintSet discoverDenialConstraints(IEvidenceSet evidenceSet, double approximationLevel);
	
}
