package de.hpi.naumann.dc.cover;

import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;

public interface IMinimalCoverSearch {
	public DenialConstraintSet discoverDenialConstraints(IEvidenceSet evidenceSet);
	
}
