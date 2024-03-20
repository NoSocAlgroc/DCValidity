package de.hpi.naumann.dc.algorithms;

import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;

public interface IDCAlgorithm {

	public DenialConstraintSet run(Input input, PredicateBuilder predicates);


	
}
