package de.hpi.naumann.dc.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Set;
import java.util.stream.Collectors;

import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.HashEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class ModuloMinimalCoverSearch implements IMinimalCoverSearch {

	private PredicateBuilder predicateBuilder;
	private static final boolean USE_FASTDC_MC = true;

	public ModuloMinimalCoverSearch(PredicateBuilder predicates) {
		this.predicateBuilder = predicates;
	}

	@Override
	public DenialConstraintSet discoverDenialConstraints(IEvidenceSet evidenceSet) {
		DenialConstraintSet DCs = new DenialConstraintSet();
		long start = System.currentTimeMillis();
		Map<Predicate, IEvidenceSet> predicateSetsModulo = new HashMap<>();
		Set<Predicate> emptySets = new HashSet<>();
		NTreeSearch tree = new NTreeSearch();
		for (Predicate p : predicateBuilder.getPredicates()) {
			IEvidenceSet moduloSet = getEvidenceSetModulo(evidenceSet, p);
			if (moduloSet.isEmpty()) {
				emptySets.add(p);
				DCs.add(new DenialConstraint(p));
				tree.add(PredicateSetFactory.create2(p).getBitset());
			} else {
				predicateSetsModulo.put(p, moduloSet);
			}
		}
		for (Predicate p : emptySets) {
			log.info("EMPTY EVI MODULO SET:" + p);
			for (Predicate Q : p.getInverse().getImplications()) {
				// OKAY, would not be minal
				log.info("Removing (i): " + Q + " " + predicateSetsModulo.containsKey(Q));
				predicateSetsModulo.remove(Q);
				// useless? always empty, because NOT(P) => NOT(Q)
				log.info("Removing (ii): " + Q.getInverse() + " " + predicateSetsModulo.containsKey(Q.getInverse()));
				predicateSetsModulo.remove(Q.getInverse());
			}
		}

		// sort by set size --> addition to FastDC
		List<Entry<Predicate, IEvidenceSet>> entryList = new ArrayList<>(predicateSetsModulo.entrySet());
		entryList.sort((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()));

		Set<Predicate> allowed = new HashSet<>();
		IPredicateSetBinary allowedPS = PredicateSetFactory.create2();
		int i = 0;
		int count = 0;
		for (Entry<Predicate, IEvidenceSet> entry : entryList) {
			++i;

			Predicate p = entry.getKey();
			log.info(i + "/" + predicateSetsModulo.size() + " " + p);

			allowed.add(p);
			allowedPS.add(p);

			Set<Predicate> allowedF = allowed.stream()
					.filter(j -> !j.getOperand1().equals(p.getOperand1()) || !j.getOperand2().equals(p.getOperand2()))
					.collect(Collectors.toSet());

			IPredicateSetBinary allowedFPS = PredicateSetFactory.create2(allowedF);
			IBitSet allowedFBitset = allowedFPS.getBitset();

			boolean invalid = entry.getValue().getSetOfPredicateSets().stream()
					.anyMatch(ps -> allowedFBitset.isSubSetOf(ps.getBitset()));
			if (invalid) {
				log.info("INVALID");
				continue;
			}

			IBitSet allowedBitsets;
			Function<IEvidenceSet, Collection<IBitSet>> getBitsets;
			if (USE_FASTDC_MC) {
//				Set<Predicate> allowedInv = new HashSet<Predicate>();
//				for (Predicate p2 : allowedF) {
//					allowedInv.add(p2.getInverse());
//				}
//				IPredicateSet2 allowedNFPS = PredicateSetFactory.create2(allowedInv);
//				IBitSet allowedNFBitset = allowedNFPS.getBitset();

				PredicateBuilder builder = new PredicateBuilder(allowedF);
				FastDCMinimalCoverSearch ms = new FastDCMinimalCoverSearch(builder.getPredicates());
				getBitsets = (filtered) -> ms.getBitsets(filtered, tree);
				allowedBitsets = allowedFBitset;
			} else {
				PredicateBuilder builder = new PredicateBuilder(allowedF);
				PrefixMinimalCoverSearch ms = new PrefixMinimalCoverSearch(builder);
				allowedBitsets = allowedFBitset;
				getBitsets = (filtered) -> ms.getBitsets(filtered);
			}

			// filtering --> addition to FastDC
			IEvidenceSet filtered = new HashEvidenceSet();
			IBitSet thisP = PredicateSetFactory.create2(p).getBitset();
			entry.getValue().getSetOfPredicateSets().stream().map(ps -> {
				IBitSet bs = ps.getBitset().clone();
				bs.and(allowedBitsets);
				return bs;
			}).filter(ps -> !ps.isEmpty()).forEach(bs -> filtered.add(PredicateSetFactory.create(bs)));

			if (filtered.isEmpty()) {
				log.info("EMPTY");
			} else {
				Collection<IBitSet> res = getBitsets.apply(filtered);

				DenialConstraintSet thisDCs = new DenialConstraintSet();
				for (IBitSet bs : res) {
					bs = bs.clone();
					if (!tree.containsSubset(bs)) {
						bs.or(thisP);
						DenialConstraint dc = new DenialConstraint(PredicateSetFactory.create(bs).convert());
						thisDCs.add(dc);
						//
					}
				}
				thisDCs.minimalize();
				for (DenialConstraint dc : thisDCs) {
					tree.add(PredicateSetFactory.create2(dc.getPredicateSet()).getBitset());
					DCs.add(dc);
					++count;
					// }
				}

				System.out.println(count);
			}
		}
		// Collection<IBitSet> result = new HashSet<IBitSet>(count);
		// tree.forEach(bs -> result.add(bs));
		log.info("modulo " + (System.currentTimeMillis() - start) + "ms");
		// evaluate(evidenceSet, result);

		// result.forEach(bitset -> DCs.add(new
		// DenialConstraint(PredicateSetFactory.create(bitset).convert())));
		return DCs;
	}

	private void evaluate(IEvidenceSet evidenceSet, Collection<IBitSet> result) {

		long startG = System.currentTimeMillis();
		Collection<IBitSet> gold = new HashSet<>(
				new PrefixMinimalCoverSearch(predicateBuilder).getBitsets(evidenceSet));
		long endG = System.currentTimeMillis();

		log.info((endG - startG) + "ms " + result.equals(gold));
		log.info(gold.size() + " vs " + result.size());
		int correct = 0;
		int tooSmall = 0;
		int tooBig = 0;
		for (IBitSet bs : result) {
			if (gold.contains(bs))
				++correct;
			else {
				for (IBitSet gBS : gold) {
					if (bs.isSubSetOf(gBS))
						++tooSmall;
					if (gBS.isSubSetOf(bs))
						++tooBig;
				}
			}
		}
		log.info(correct + " " + tooSmall + " " + tooBig);
	}

	private IEvidenceSet getEvidenceSetModulo(IEvidenceSet set, final Predicate p) {
		IEvidenceSet evidenceSetModulo = new HashEvidenceSet();
		for (IPredicateSetBinary pS : set.getSetOfPredicateSets()) {
			if (!pS.containsPredicate(p))
				continue;

			IPredicateSetBinary setModulo = PredicateSetFactory.create2(pS);
			setModulo.remove(p);
			evidenceSetModulo.add(pS);
		}
		return evidenceSetModulo;
	}

	private static Logger log = LoggerFactory.getLogger(ModuloMinimalCoverSearch.class);

}
