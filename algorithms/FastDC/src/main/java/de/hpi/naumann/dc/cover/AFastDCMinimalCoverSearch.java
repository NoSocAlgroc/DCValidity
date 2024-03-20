package de.hpi.naumann.dc.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.ISubsetBackend;
import ch.javasoft.bitset.search.NTreeSearch;
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

public class AFastDCMinimalCoverSearch extends FastDCMinimalCoverSearch implements IAMinimalCoverSearch {

	private BitSetTranslator translator;
	
	private static long totalEvidenceCount;

	public AFastDCMinimalCoverSearch(Set<Predicate> predicates) {
		super(predicates);
	}

	public DenialConstraintSet discoverDenialConstraints(IEvidenceSet evidenceSet, double approximationRatio) {
		
		AMinimalCoverCandidate.setApproximationLevel(approximationRatio);
		totalEvidenceCount=evidenceSet.getTotalCountEvi();
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

	@Override
	public Collection<IPredicateSet> searchMinimalCovers(IEvidenceSet evidenceSet) {

		int[] evidenceCounts = countEvidences(evidenceSet);
		ArrayIndexComparator comparator = new ArrayIndexComparator(evidenceCounts,
				ArrayIndexComparator.Order.ASCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());

		ISubsetBackend MC = new NTreeSearch();
		AMinimalCoverCandidate c = new AMinimalCoverCandidate(evidenceSet, predicates);
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

	private static Logger log = LoggerFactory.getLogger(AFastDCMinimalCoverSearch.class);

	public Collection<IBitSet> getBitsets(IEvidenceSet evidenceSet, NTreeSearch priorDCs) {
		int[] evidenceCounts = countEvidences(evidenceSet);
		ArrayIndexComparator comparator = new ArrayIndexComparator(evidenceCounts,
				ArrayIndexComparator.Order.ASCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());

		ISubsetBackend MC = new NTreeSearch();
		AMinimalCoverCandidate c = new AMinimalCoverCandidate(evidenceSet, predicates);
		c.searchMinimalCovers(MC, translator, priorDCs);

		Collection<IBitSet> result = new ArrayList<>();
		MC.forEach(bitset -> result.add(translator.bitsetRetransform(bitset)));
		return result;
	}
	
	@Override
	public int[] countEvidences(IEvidenceSet evidenceSet) {

		int[] counts = new int[PredicateBitSet.indexProvider.size()];
		for (IPredicateSetBinary ps : evidenceSet) {
			IBitSet bitset = ps.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i]++;
				//counts[i]=counts[i]+ (int)evidenceSet.getCount(ps);// count based on the number of evidences
			}
		}

		return counts;

	}

	public static long getTotalEvidenceCount() {
		return totalEvidenceCount;
	}

	
	



}
