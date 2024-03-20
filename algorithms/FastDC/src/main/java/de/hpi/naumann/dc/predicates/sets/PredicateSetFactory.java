package de.hpi.naumann.dc.predicates.sets;

import java.util.Collection;

import ch.javasoft.bitset.IBitSet;
import de.hpi.naumann.dc.predicates.Predicate;

public class PredicateSetFactory {

	static public IPredicateSet create() {
		return new PredicateHashSet();
	}

	public static IPredicateSet create(Predicate... predicates) {
		return new PredicateHashSet(predicates);
	}

	public static PredicateHashSet create(IPredicateSet pS) {
		return new PredicateHashSet(pS);
	}

	public static IPredicateSetBinary create2() {
		return new PredicateBitSet();
	}

	public static IPredicateSetBinary create2(Predicate p) {
		return new PredicateBitSet(p);
	}

	public static IPredicateSetBinary create2(IPredicateSet set) {
		return new PredicateBitSet(set);
	}

	public static IPredicateSetBinary create(IBitSet bitset) {
		return new PredicateBitSet(bitset);
	}

	public static IPredicateSetBinary create2(IPredicateSetBinary pS) {
		return new PredicateBitSet(pS);
	}

	public static IPredicateSetBinary create2(Collection<Predicate> objects) {
		IPredicateSetBinary set = new PredicateBitSet();
		for (Predicate p : objects)
			set.add(p);
		return set;
	}
}
