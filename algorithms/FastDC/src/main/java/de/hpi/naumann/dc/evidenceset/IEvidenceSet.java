package de.hpi.naumann.dc.evidenceset;

import java.util.Iterator;
import java.util.Set;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;

public interface IEvidenceSet extends Iterable<IPredicateSetBinary> {

	boolean add(IPredicateSetBinary predicateSet);

	boolean add(IPredicateSetBinary create, long count);

	long getCount(IPredicateSetBinary predicateSet);

	boolean contains(IPredicateSetBinary i);

	long[] getKEvidencesCounts(DenialConstraint dc);

	int getSubsetCount(IPredicateSetBinary dcPredicates);

	Iterator<IPredicateSetBinary> iterator();

	Set<IPredicateSetBinary> getSetOfPredicateSets();

	int size();

	boolean isEmpty();
	
	public long getTotalCountEvi();

}