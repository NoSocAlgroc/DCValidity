package ch.javasoft.bitset.search;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;

public class NTreeSearchArray implements ISubsetBackend {

	private NTreeSearchArray[] subtrees;
	private IBitSet bitset;

	public NTreeSearchArray(int attrCount) {
		subtrees = new NTreeSearchArray[attrCount];
	}

	@Override
	public boolean add(IBitSet bs) {
		add(bs, 0);
		return true;
	}

	private void add(IBitSet bs, int next) {
		int nextBit = bs.nextSetBit(next);
		if (nextBit < 0) {
			bitset = bs;
			return;
		} else {
			NTreeSearchArray nextTree = subtrees[nextBit];
			if (nextTree == null) {
				nextTree = new NTreeSearchArray(subtrees.length);
				subtrees[nextBit] = nextTree;
			}
			nextTree.add(bs, nextBit + 1);
		}

	}

	@Override
	public Set<IBitSet> getAndRemoveGeneralizations(IBitSet invalidFD) {
		HashSet<IBitSet> removed = new HashSet<>();
		getAndRemoveGeneralizations(invalidFD, 0, removed);
		return removed;
	}

	private boolean getAndRemoveGeneralizations(IBitSet invalidFD, int next, Set<IBitSet> removed) {
		if (bitset != null) {
			removed.add(bitset);
			bitset = null;
		}

		int nextBit = invalidFD.nextSetBit(next);
		while (nextBit >= 0) {
			NTreeSearchArray subTree = subtrees[nextBit];
			if (subTree != null)
				if (subTree.getAndRemoveGeneralizations(invalidFD, nextBit + 1, removed))
					subtrees[nextBit] = null;
			nextBit = invalidFD.nextSetBit(nextBit + 1);
		}
		// return subtrees.isEmpty();
		return false;
	}

	@Override
	public boolean containsSubset(IBitSet add) {
		return containsSubset(add, 0);
	}

	private boolean containsSubset(IBitSet add, int next) {
		if (bitset != null)
			return true;

		int nextBit = add.nextSetBit(next);
		while (nextBit >= 0) {
			NTreeSearchArray subTree = subtrees[nextBit];
			if (subTree != null && subTree.containsSubset(add, nextBit + 1))
				return true;
			nextBit = add.nextSetBit(nextBit + 1);
		}

		return false;
	}

	@Override
	public void forEach(Consumer<IBitSet> consumer) {
		if (bitset != null)
			consumer.accept(bitset);
		for (NTreeSearchArray subtree : subtrees) {
			if (subtree != null)
				subtree.forEach(consumer);
		}
	}

}
