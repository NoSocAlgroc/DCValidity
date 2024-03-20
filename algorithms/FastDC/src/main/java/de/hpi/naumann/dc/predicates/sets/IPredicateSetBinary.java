package de.hpi.naumann.dc.predicates.sets;

import ch.javasoft.bitset.IBitSet;
import de.hpi.naumann.dc.predicates.Predicate;

public interface IPredicateSetBinary {

	void add(Predicate predicate);

	boolean containsPredicate(Predicate p);

	boolean isSubsetOf(IPredicateSetBinary p);

	public int getIntersectionCount(IPredicateSet iPredicateSet);

	void remove(Predicate p);

	public IBitSet getBitset();

	public IPredicateSet convert();

	void addAll(IPredicateSetBinary iPredicateSet2);
}
