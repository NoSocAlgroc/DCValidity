package de.hpi.naumann.dc.predicates.sets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hpi.naumann.dc.predicates.Predicate;

public class PredicateHashSet implements IPredicateSet {

	private Set<Predicate> predicates = new HashSet<Predicate>();

	public PredicateHashSet() {
		this.predicates = new HashSet<Predicate>();
	}

	public PredicateHashSet(Predicate[] predicates) {
		this();
		Collections.addAll(this.predicates, predicates);
	}

	public PredicateHashSet(Set<Predicate> predicates) {
		this.predicates = new HashSet<Predicate>(predicates);
	}

	public PredicateHashSet(IPredicateSet path) {
		this.predicates = new HashSet<Predicate>(path.getPredicates());
	}

	@Override
	public Set<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	public void remove(Predicate q) {
		this.predicates.remove(q);
	}

	@Override
	public Stream<Predicate> stream() {
		return predicates.stream();
	}

	@Override
	public boolean addAll(Collection<Predicate> add) {
		return predicates.addAll(add);
	}

	@Override
	public boolean add(Predicate predicate) {
		return predicates.add(predicate);
	}

	@Override
	public boolean containsPredicate(Predicate p) {
		return predicates.contains(p);
	}

	@Override
	public Iterator<Predicate> iterator() {
		return predicates.iterator();
	}

	@Override
	public int size() {
		return predicates.size();
	}

	@Override
	public boolean containsAllBut(IPredicateSet pSet, Predicate q) {
		for (Predicate p : pSet) {
			if (p != q && !containsPredicate(p))
				return false;
		}
		return true;
	}

	@Override
	public boolean isSubsetOf(IPredicateSet superset) {
		for (Predicate p : this) {
			if (!superset.containsPredicate(p)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		//return "[PC: " + stream().map(p -> p.toString()).collect(Collectors.joining(", ")) + "]";
		return stream().map(p -> p.toString()).collect(Collectors.joining(" & ")) ;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((predicates == null) ? 0 : predicates.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredicateHashSet other = (PredicateHashSet) obj;
		if (predicates == null) {
			if (other.predicates != null)
				return false;
		} else if (!predicates.equals(other.predicates))
			return false;
		return true;
	}

	@Override
	public IPredicateSetBinary getIPredicateSetBinary() {
		// TODO Auto-generated method stub
		return null;
	}
}
