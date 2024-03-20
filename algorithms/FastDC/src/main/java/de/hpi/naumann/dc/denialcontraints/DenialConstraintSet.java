package de.hpi.naumann.dc.denialcontraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.sets.Closure;
import de.hpi.naumann.dc.predicates.sets.IPredicateSet;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class DenialConstraintSet implements Iterable<DenialConstraint> {

	private Set<DenialConstraint> constraints = new HashSet<>();

	public boolean contains(DenialConstraint dc) {
		return constraints.contains(dc);
	}

	private static class MinimalDCCandidate {
		DenialConstraint dc;
		IBitSet bitset;

		public MinimalDCCandidate(DenialConstraint dc) {
			this.dc = dc;
			this.bitset = PredicateSetFactory.create2(dc.getPredicateSet()).getBitset();
		}

		public boolean shouldReplace(MinimalDCCandidate prior) {
			if (prior == null)
				return true;

			if (dc.getPredicateCount() < prior.dc.getPredicateCount())
				return true;

			if (dc.getPredicateCount() > prior.dc.getPredicateCount())
				return false;

			return bitset.compareTo(prior.bitset) <= 0;
		}
	}

	public void minimalize() {
		Map<IPredicateSet, MinimalDCCandidate> constraintsClosureMap = new HashMap<>();
		for (DenialConstraint dc : constraints) {
			IPredicateSet predicateSet = dc.getPredicateSet();
			Closure c = new Closure(predicateSet);
			if (c.construct()) {
				MinimalDCCandidate candidate = new MinimalDCCandidate(dc);
				IPredicateSet closure = c.getClosure();
				MinimalDCCandidate prior = constraintsClosureMap.get(closure);
				if (candidate.shouldReplace(prior))
					constraintsClosureMap.put(closure, candidate);
			}
		}

		List<Entry<IPredicateSet, MinimalDCCandidate>> constraints2 = new ArrayList<>(constraintsClosureMap.entrySet());
		//System.out.println("Sym size created " + constraints2.size());

		constraints2.sort((entry1, entry2) -> {
			int res = Integer.compare(entry1.getKey().size(), entry2.getKey().size());
			if (res != 0)
				return res;
			res = Integer.compare(entry1.getValue().dc.getPredicateCount(), entry2.getValue().dc.getPredicateCount());
			if (res != 0)
				return res;
			return entry1.getValue().bitset.compareTo(entry2.getValue().bitset);
		});
		constraints = new HashSet<>();
		NTreeSearch tree = new NTreeSearch();
		for (Entry<IPredicateSet, MinimalDCCandidate> entry : constraints2) {
			if (tree.containsSubset(PredicateSetFactory.create2(entry.getKey()).getBitset()))
				continue;

			DenialConstraint inv = entry.getValue().dc.getInvT1T2DC();
			if (inv != null) {
				Closure c = new Closure(inv.getPredicateSet());
				if (!c.construct())
					continue;
				 if
				 (tree.containsSubset(PredicateSetFactory.create2(c.getClosure()).getBitset()))
				 continue;
			}

			constraints.add(entry.getValue().dc);
			tree.add(entry.getValue().bitset);
			 if(inv != null)
				 tree.add(PredicateSetFactory.create2(inv.getPredicateSet()).getBitset());
		}
		// etmMonitor.render(new SimpleTextRenderer());
	}

	public void add(DenialConstraint dc) {
		constraints.add(dc);
	}

	public void addAll(Collection<DenialConstraint> constraints2) {
		constraints.addAll(constraints2);
	}

	@Override
	public Iterator<DenialConstraint> iterator() {
		return constraints.iterator();
	}

	public Stream<DenialConstraint> stream() {
		return constraints.stream();
	}

	public void addInverse(IPredicateSet s) {
		IPredicateSet pS = PredicateSetFactory.create();
		for (Predicate p : s) {
			pS.add(p.getInverse());
		}
		constraints.add(new DenialConstraint(pS));
	}

	public int size() {
		return constraints.size();
	}

	public void addAll(DenialConstraintSet other) {
		constraints.addAll(other.constraints);
	}
}
