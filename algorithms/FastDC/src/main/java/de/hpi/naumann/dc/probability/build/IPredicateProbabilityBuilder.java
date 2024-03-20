package de.hpi.naumann.dc.probability.build;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;

public interface IPredicateProbabilityBuilder {
	
	public void buildIndividualProbabilities();
	public void probability(DenialConstraint dcs);
	public void findProblematicParts(DenialConstraint dc);
	

}
