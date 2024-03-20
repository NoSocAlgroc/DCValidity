package de.hpi.naumann.dc.evidenceset.build;

import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.input.Input;

public interface IEvidenceSetBuilder {

	IEvidenceSet buildEvidenceSet(Input input);

}