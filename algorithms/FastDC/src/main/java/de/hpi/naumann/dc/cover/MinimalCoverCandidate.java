
package de.hpi.naumann.dc.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.HashMultiset;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.ISubsetBackend;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.evidenceset.HashEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.helpers.BitSetTranslator;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.sets.Closure;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class MinimalCoverCandidate {

	final IEvidenceSet evidenceSet;
	final Collection<Predicate> addablePredicates;
	final Closure closure;
	final IPredicateSetBinary current;

	protected final static boolean ENABLE_TRANSITIVE_CHECK = false;
	protected static final boolean ENABLE_CLOSURE_CHECK = false;

	public MinimalCoverCandidate(IEvidenceSet evidenceSet, Collection<Predicate> addablePredicates) {
		this.evidenceSet = evidenceSet;
		this.addablePredicates = addablePredicates;
		this.closure = new Closure(PredicateSetFactory.create());
		this.current = PredicateSetFactory.create2();
	}

	public MinimalCoverCandidate(IEvidenceSet evidenceSet, Collection<Predicate> addablePredicates,
			IPredicateSetBinary current, Closure closure) {
		this.evidenceSet = evidenceSet;
		this.addablePredicates = addablePredicates;
		this.closure = closure;
		this.current = current;
	}

	

	public void searchMinimalCovers(ISubsetBackend mC, BitSetTranslator translator, NTreeSearch priorDCs) {
//		int counter = 0;
//		for (IPredicateSetBinary ps2 : evidenceSet) {
//			counter += evidenceSet.getCount(ps2);
//		}
		

		if(evidenceSet.isEmpty()) {
//		if (counter <= 3) {
			mC.add(translator.bitsetTransform(current.getBitset()));
		} else if (addablePredicates.size() > 0) {
			List<Predicate> pOrder = getSortedPredicates();
			List<Predicate> allowed = new ArrayList<>();
			Iterator<Predicate> iter = pOrder.iterator();
			while (iter.hasNext()) {
				Predicate add = iter.next();
				iter.remove();
				MinimalCoverCandidate c = getCandidate(allowed, add, mC, translator, priorDCs);
				allowed.add(add);
				if (c != null)
					c.searchMinimalCovers(mC, translator, priorDCs);
			}
		}
	}

	private MinimalCoverCandidate getCandidate(List<Predicate> pOrder, Predicate addInverse, ISubsetBackend MC,
			BitSetTranslator translator, NTreeSearch priorDCs) {
		IPredicateSetBinary newPS = PredicateSetFactory.create2(current);
		newPS.add(addInverse);

		boolean contains = MC.containsSubset(translator.bitsetTransform(newPS.getBitset()));
		if (contains)
			return null;

		if (ENABLE_TRANSITIVE_CHECK) {
			for (Predicate p : newPS.convert()) {
				IPredicateSetBinary dc2 = PredicateSetFactory.create2(newPS);
				dc2.remove(p);
				for (Predicate p2 : p.getInverse().getImplications())
					dc2.add(p2);
				if (MC.containsSubset(translator.bitsetTransform(dc2.getBitset()))
						|| priorDCs != null && priorDCs.containsSubset(dc2.getBitset()))
					return null;
			}
		}

		boolean containsDC = priorDCs != null && priorDCs.containsSubset(newPS.getBitset());
		if (containsDC)
			return null;

		Closure closureNew = null;
		if (ENABLE_CLOSURE_CHECK) {
			closureNew = new Closure(closure, addInverse);
			if (!closureNew.construct())
				return null;

			if (MC.containsSubset(
					translator.bitsetTransform(PredicateSetFactory.create2(closureNew.getClosure()).getBitset()))
					|| priorDCs != null && priorDCs
							.containsSubset(PredicateSetFactory.create2(closureNew.getClosure()).getBitset()))
				return null;

		}
		List<Predicate> newOrder = new ArrayList<>(pOrder.size());
		for (Predicate p : pOrder)
			if (!p.getOperand1().equals(addInverse.getOperand1()) || !p.getOperand2().equals(addInverse.getOperand2()))
				if (closureNew == null || !closureNew.getClosure().containsPredicate(p))
					newOrder.add(p);

		IEvidenceSet filtered = getFilteredEvidenceSet(addInverse, newOrder);
		if (filtered == null)
			return null;
		return new MinimalCoverCandidate(filtered, newOrder, newPS, closureNew);
	}

	private IEvidenceSet getFilteredEvidenceSet(Predicate add, Collection<Predicate> addablePredicates) {
		IEvidenceSet filteredEvidence = new HashEvidenceSet();
		IBitSet addables = PredicateSetFactory.create2(addablePredicates).getBitset();
		Iterator<IPredicateSetBinary> iter = evidenceSet.iterator();
		int addIndex = PredicateBitSet.getIndex(add);
		while (iter.hasNext()) {
			IPredicateSetBinary pSet = iter.next();
			IBitSet bs = pSet.getBitset();
			if (bs.get(addIndex)) {
				if (addables.isSubSetOf(bs))
					return null;

				bs = bs.clone();
				bs.and(addables);
				filteredEvidence.add(PredicateSetFactory.create(bs));
			}
		}
		
		return filteredEvidence;
	}

	private List<Predicate> getSortedPredicates() {
		int[] counts = getPredicateCounts();

		HashMultiset<Predicate> pCounts = HashMultiset.create();
		for (int i = 0; i < counts.length; ++i) {
			pCounts.add(PredicateBitSet.getPredicate(i), counts[i]);
		}

		List<Predicate> pOrder = new ArrayList<>(addablePredicates.size());
		for (Predicate p : addablePredicates)
			if (pCounts.count(p) < evidenceSet.size())
				pOrder.add(p);

		pOrder.sort(new Comparator<Predicate>() {
			@Override
			public int compare(Predicate o1, Predicate o2) {
				return Integer.compare(pCounts.count(o2), pCounts.count(o1));
			}
		});
		return pOrder;
	}

	protected int[] getPredicateCounts() {
		int[] counts = new int[PredicateBitSet.indexProvider.size()];
		for (IPredicateSetBinary pSet : evidenceSet) {
			IBitSet bitset = pSet.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i]++;
			}

		}
		return counts;
	}

	// private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
}
