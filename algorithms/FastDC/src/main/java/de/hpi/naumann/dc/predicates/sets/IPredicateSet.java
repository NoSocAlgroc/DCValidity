package de.hpi.naumann.dc.predicates.sets;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import de.hpi.naumann.dc.predicates.Predicate;

public interface IPredicateSet extends Iterable<Predicate> {

	Set<Predicate> getPredicates();

	Stream<Predicate> stream();

	boolean add(Predicate predicate);

	boolean addAll(Collection<Predicate> add);

	void remove(Predicate q);

	boolean containsPredicate(Predicate p);

	boolean containsAllBut(IPredicateSet pSet, Predicate q);

	boolean isSubsetOf(IPredicateSet superset);

	int size();

	String toString();
	
	IPredicateSetBinary getIPredicateSetBinary();
}