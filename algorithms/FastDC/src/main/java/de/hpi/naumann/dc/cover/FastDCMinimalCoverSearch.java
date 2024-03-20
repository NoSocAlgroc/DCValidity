package de.hpi.naumann.dc.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.ISubsetBackend;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.data_structures.PredicateCombinationBitSet;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.helpers.ArrayIndexComparator;
import de.hpi.naumann.dc.helpers.BitSetTranslator;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.sets.IPredicateSet;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateHashBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class FastDCMinimalCoverSearch implements IMinimalCoverSearch {

	protected Set<Predicate> predicates;
	private BitSetTranslator translator;

	public FastDCMinimalCoverSearch(Set<Predicate> predicates) {
		this.predicates = predicates;
	}

	public DenialConstraintSet discoverDenialConstraints(IEvidenceSet evidenceSet) {
		Collection<IPredicateSet> MC = searchMinimalCovers(evidenceSet);

		log.info("Building DCs ...");
		DenialConstraintSet dcs = new DenialConstraintSet();
		for (IPredicateSet mc : MC) {
			DenialConstraint dc = new DenialConstraint(mc);
			dcs.add(dc);
		}
		dcs.minimalize();

		return dcs;
	}

	public Collection<IPredicateSet> searchMinimalCovers(IEvidenceSet evidenceSet) {

		int[] evidenceCounts = countEvidences(evidenceSet);
		ArrayIndexComparator comparator = new ArrayIndexComparator(evidenceCounts,
				ArrayIndexComparator.Order.ASCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());

		ISubsetBackend MC = new NTreeSearch();
		MinimalCoverCandidate c = new MinimalCoverCandidate(evidenceSet, predicates);
		c.searchMinimalCovers(MC, translator, null);

		Collection<IPredicateSet> result = new ArrayList<IPredicateSet>();

		// Needs review
		MC.forEach(bitset -> result
				.add(new PredicateHashBitSet(PredicateSetFactory.create(translator.bitsetRetransform(bitset)).convert(),
						PredicateSetFactory.create(translator.bitsetRetransform(bitset)))));

		// MC.forEach(bitset ->
		// result.add(PredicateSetFactory.create(translator.bitsetRetransform(bitset)).convert()));

		return result;
	}

	private static Logger log = LoggerFactory.getLogger(FastDCMinimalCoverSearch.class);

	public Collection<IBitSet> getBitsets(IEvidenceSet evidenceSet, NTreeSearch priorDCs) {
		int[] evidenceCounts = countEvidences(evidenceSet);
		ArrayIndexComparator comparator = new ArrayIndexComparator(evidenceCounts,
				ArrayIndexComparator.Order.ASCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());

		ISubsetBackend MC = new NTreeSearch();
		MinimalCoverCandidate c = new MinimalCoverCandidate(evidenceSet, predicates);
		c.searchMinimalCovers(MC, translator, priorDCs);

		Collection<IBitSet> result = new ArrayList<>();
		MC.forEach(bitset -> result.add(translator.bitsetRetransform(bitset)));
		return result;
	}

	public int[] countEvidences(IEvidenceSet evidenceSet) {

		int[] counts = new int[PredicateBitSet.indexProvider.size()];
		for (IPredicateSetBinary ps : evidenceSet) {
			IBitSet bitset = ps.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i]++;
			}
		}

		return counts;

	}

	public void checkDCsLevel(DenialConstraint dc, IEvidenceSet evidenceSet) {

		IBitSet bitset = dc.getPredicateSet().getIPredicateSetBinary().getBitset();
		PredicateCombinationBitSet combination = new PredicateCombinationBitSet(bitset);

		int cardinalitydc = 0;
		Iterator<IPredicateSetBinary> iterdc = evidenceSet.iterator();
		while (iterdc.hasNext()) {
			IPredicateSetBinary evi = iterdc.next();
			if (evi.getBitset().getAndCardinality(bitset) == bitset.cardinality())
				cardinalitydc = cardinalitydc + (int) evidenceSet.getCount(evi);
		}
		System.out.println(dc + " " + cardinalitydc);

		List<PredicateCombinationBitSet> subsets = combination.getDirectSubsets();

		// combination.getDirectSubsets()
		// .forEach(sub ->
		// System.out.println(PredicateBitSet.indexProvider.getObjects(sub.getBitset())));
		for (PredicateCombinationBitSet subset : subsets) {
			IBitSet subBitset = subset.getBitset();
			int cardinality = 0;
			Iterator<IPredicateSetBinary> iter = evidenceSet.iterator();
			while (iter.hasNext()) {
				IPredicateSetBinary evi = iter.next();
				if (evi.getBitset().getAndCardinality(subBitset) == subBitset.cardinality())
					cardinality = cardinality + (int) evidenceSet.getCount(evi);
			}
			System.out.println(PredicateBitSet.indexProvider.getObjects(subBitset) + " " + cardinality);

		}

	}

	public void checkDCsLevel1(DenialConstraint dc, IEvidenceSet evidenceSet) {

		System.out.println("L0: " + dc + "\n");

		IBitSet mainBitset = dc.getPredicateSet().getIPredicateSetBinary().getBitset();
		List<PredicateCombinationBitSet> predicateCombination1L = new PredicateCombinationBitSet(mainBitset)
				.getDirectSubsets();

		for (PredicateCombinationBitSet subset : predicateCombination1L) {
			System.out.print("L1: ");
			presentSubsetInfo(evidenceSet, subset);
			System.out.println();

		}

	}

	public void checkDCsLevel2(DenialConstraint dc, IEvidenceSet evidenceSet) {

		System.out.println("L0: " + dc + "\n");

		IBitSet mainBitset = dc.getPredicateSet().getIPredicateSetBinary().getBitset();
		List<PredicateCombinationBitSet> predicateCombination1L = new PredicateCombinationBitSet(mainBitset)
				.getDirectSubsets();

		for (PredicateCombinationBitSet subset : predicateCombination1L) {
			System.out.print("L1: ");
			presentSubsetInfo(evidenceSet, subset);
			System.out.println();

			for (PredicateCombinationBitSet subset2 : subset.getDirectSubsets()) {
				System.out.print("L2: ");
				presentSubsetInfo(evidenceSet, subset2);

			}

			System.out.println();

		}

	}

	public void checkDCsLevel3(DenialConstraint dc, IEvidenceSet evidenceSet) {

		System.out.println("L0: " + dc + "\n");

		IBitSet mainBitset = dc.getPredicateSet().getIPredicateSetBinary().getBitset();
		List<PredicateCombinationBitSet> predicateCombination1L = new PredicateCombinationBitSet(mainBitset)
				.getDirectSubsets();

		for (PredicateCombinationBitSet subset : predicateCombination1L) {
			System.out.print("L1: ");
			presentSubsetInfo(evidenceSet, subset);
			System.out.println();

			for (PredicateCombinationBitSet subset2 : subset.getDirectSubsets()) {
				System.out.print("L2: ");
				presentSubsetInfo(evidenceSet, subset2);
				System.out.println();

				for (PredicateCombinationBitSet subset3 : subset2.getDirectSubsets()) {
					System.out.print("L3: ");
					presentSubsetInfo(evidenceSet, subset3);

				}

				System.out.println();

			}

			System.out.println();

		}

	}

	public void presentSubsetInfo(IEvidenceSet evidenceSet, PredicateCombinationBitSet subset) {
		IBitSet subBitset = subset.getBitset();
		int cardinality = 0;
		Iterator<IPredicateSetBinary> iter = evidenceSet.iterator();
		while (iter.hasNext()) {
			IPredicateSetBinary evi = iter.next();
			if (evi.getBitset().getAndCardinality(subBitset) == subBitset.cardinality())
				cardinality = cardinality + (int) evidenceSet.getCount(evi);
		}
		System.out.println(PredicateBitSet.indexProvider.getObjects(subBitset) + " " + cardinality);
	}

}
