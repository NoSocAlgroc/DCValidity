package de.hpi.naumann.dc.data_structures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ch.javasoft.bitset.BitSetFactory;
import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet.LongBitSetFactory;



public class PredicateCombinationBitSet {

	private static BitSetFactory bf = new LongBitSetFactory();

	protected IBitSet bitset;
	protected long size = 0;

	public PredicateCombinationBitSet(IBitSet bitset) {
		this.bitset = bitset;
		this.size = bitset.cardinality();
	}

	public PredicateCombinationBitSet(int... predicateIndexes) {
		bitset = bf.create();

		for (int columnIndex : predicateIndexes) {
			// If the bit was not yet set, increase the size.
			addPredicate(columnIndex);
		}
	}

	/**
	 * Creates a copy of the current instance.
	 *
	 * @param predicateCombination
	 *            that is cloned to the new instance
	 */
	public PredicateCombinationBitSet(PredicateCombinationBitSet predicateCombination) {
		setColumns(predicateCombination.bitset.clone());
	}

	/**
	 * Adds a predicate to the bit set.
	 *
	 * @param predicateIndex
	 *            of column to add
	 * @return the predicate combination
	 */
	public PredicateCombinationBitSet addPredicate(int predicateIndex) {
		if (!bitset.get(predicateIndex)) {
			size++;
		}

		bitset.set(predicateIndex);

		return this;
	}

	/**
	 * @param predicateIndex
	 *            index of bit to test
	 * @return true iff the bit at predicateIndex is set
	 */
	public boolean containsColumn(int predicateIndex) {
		return bitset.get(predicateIndex);
	}

	/**
	 * @param predicateIndices
	 *            index of bit to test
	 * @return true iff the bits in the predicateIndices are set
	 */
	public boolean containsColumn(int... predicateIndices) {
		for (int predicateIndex : predicateIndices) {
			if (!bitset.get(predicateIndex)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns all subset of the predicate combination (including the empty
	 * {@link PredicateCombinationBitSet}). The subsets include the original
	 * predicate combination (not proper subsets).
	 *
	 * @return subsets
	 */
	public List<PredicateCombinationBitSet> getAllSubsets() {
		List<PredicateCombinationBitSet> subsets = new LinkedList<>();

		Queue<PredicateCombinationBitSet> currentLevel = new LinkedList<>();
		currentLevel.add(this);
		subsets.add(this);
		Set<PredicateCombinationBitSet> nextLevel = new HashSet<>();
		for (int level = size(); level > 0; level--) {
			while (!currentLevel.isEmpty()) {
				PredicateCombinationBitSet currentColumnCombination = currentLevel.remove();
				nextLevel.addAll(currentColumnCombination.getDirectSubsets());
			}
			currentLevel.addAll(nextLevel);
			subsets.addAll(nextLevel);
			nextLevel.clear();
		}

		return subsets;
	}

	/**
	 * Generates the direct subset predicate combinations.
	 *
	 * @return the direct sub sets
	 */
	public List<PredicateCombinationBitSet> getDirectSubsets() {
		return getDirectSubsetsSupersetOfFast(null);
	}

	/**
	 * Generates the direct subset predicate combinations that are superset of the
	 * given sub set. If the subSet is null all direct subsets are returned.
	 *
	 * @param subSet
	 *            that all generated subsets are super set of
	 * @return the direct sub sets super set of the sub set
	 */
	protected List<PredicateCombinationBitSet> getDirectSubsetsSupersetOfFast(PredicateCombinationBitSet subSet) {

		PredicateCombinationBitSet predicatesToRemove;
		if (subSet == null) {
			predicatesToRemove = this;
		} else {
			predicatesToRemove = this.minus(subSet);
		}

		List<PredicateCombinationBitSet> subsets = new ArrayList<>(size());

		PredicateCombinationBitSet generatedSubset;
		for (int columnIndex : predicatesToRemove.getSetBits()) {
			generatedSubset = new PredicateCombinationBitSet(this);
			generatedSubset.removeColumn(columnIndex);
			subsets.add(generatedSubset);
		}

		return subsets;
	}

	/**
	 * Removes a predicate from the bit set.
	 *
	 * @param predicateIndex
	 *            of predicate to remove
	 * @return the predicate combination
	 */
	public PredicateCombinationBitSet removeColumn(int predicateIndex) {
		if (bitset.get(predicateIndex)) {
			size--;
		}

		bitset.clear(predicateIndex);

		return this;
	}

	/**
	 * Returns a list of all set predicate indices.
	 *
	 * @return the list of indices with set bits
	 */
	public List<Integer> getSetBits() {
		List<Integer> setBits = new ArrayList<>(size());

		int setBitIndex = 0;
		while (true) {
			setBitIndex = bitset.nextSetBit(setBitIndex);

			if (setBitIndex == -1) {
				break;
			} else {
				setBits.add(setBitIndex);
			}

			setBitIndex++;
		}

		return setBits;
	}

	/**
	 * Returns the difference between the two sets.
	 *
	 * @param otherPredicateCombination
	 *            predicate combination to be subtracted
	 * @return The difference {@link PredicateCombinationBitset}
	 */
	public PredicateCombinationBitSet minus(PredicateCombinationBitSet otherPredicateCombination) {

		IBitSet temporaryBitset = bitset.clone();
		temporaryBitset.andNot(otherPredicateCombination.bitset);

		return new PredicateCombinationBitSet().setColumns(temporaryBitset);
	}

	/**
	 * Sets the given {@link OpenBitSet}, the previous state is overwritten!
	 *
	 * @param bitset
	 *            set on the existing PredicateCombinationBitset
	 * @return the instance
	 */
	protected PredicateCombinationBitSet setColumns(IBitSet bitset) {
		this.bitset = bitset;
		size = bitset.cardinality();

		return this;
	}

	/**
	 * Returns the number of predicates in the combination.
	 *
	 * @return the number of predicates in the combination.
	 */
	public int size() {
		return (int) size;
	}

	public IBitSet getBitset() {
		return bitset;
	}


	
	

}
