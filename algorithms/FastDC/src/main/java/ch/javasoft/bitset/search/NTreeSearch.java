package ch.javasoft.bitset.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;

public class NTreeSearch implements ISubsetBackend, ITreeSearch {

	private HashMap<Integer, NTreeSearch> subtrees = new HashMap<>();
	private IBitSet bitset;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.javasoft.bitset.search.ITreeSearch#add(ch.javasoft.bitset.IBitSet)
	 */
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
			NTreeSearch nextTree = subtrees.get(nextBit);
			if (nextTree == null) {
				nextTree = new NTreeSearch();
				subtrees.put(nextBit, nextTree);
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
			NTreeSearch subTree = subtrees.get(nextBit);
			if (subTree != null)
				if (subTree.getAndRemoveGeneralizations(invalidFD, nextBit + 1, removed))
					subtrees.remove(nextBit);
			nextBit = invalidFD.nextSetBit(nextBit + 1);
		}
		return subtrees.isEmpty();
	}

	@Override
	public boolean containsSubset(IBitSet add) {
		return getSubset(add, 0) != null;
	}

	public IBitSet getSubset(IBitSet add) {
		return getSubset(add, 0);
	}

	private IBitSet getSubset(IBitSet add, int next) {
		if (bitset != null)
			return bitset;

		int nextBit = add.nextSetBit(next);
		while (nextBit >= 0) {
			NTreeSearch subTree = subtrees.get(nextBit);
			if (subTree != null) {
				IBitSet res = subTree.getSubset(add, nextBit + 1);
				if (res != null)
					return res;
			}
			nextBit = add.nextSetBit(nextBit + 1);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.javasoft.bitset.search.ITreeSearch#forEachSuperSet(ch.javasoft.bitset.
	 * IBitSet, java.util.function.Consumer)
	 */
	@Override
	public void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer) {
		forEachSuperSet(bitset, consumer, 0);
	}

	private void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer, int next) {
		int nextBit = bitset.nextSetBit(next);
		if (nextBit < 0)
			forEach(consumer);

		// for(int i = next; i <= nextBit; ++i) {
		for (Entry<Integer, NTreeSearch> entry : subtrees.entrySet()) {
			if (entry.getKey() > nextBit)
				continue;
			NTreeSearch subTree = entry.getValue();
			if (subTree != null)
				subTree.forEachSuperSet(bitset, consumer, entry.getKey() + 1);
		}
	}
	
	public boolean containsSuperSet(IBitSet bitset) {
		return containsSuperSet(bitset, 0);
	}

	private boolean containsSuperSet(IBitSet bitset, int next) {
		int nextBit = bitset.nextSetBit(next);
		if (nextBit < 0)
			return true;

		// for(int i = next; i <= nextBit; ++i) {
		for (Entry<Integer, NTreeSearch> entry : subtrees.entrySet()) {
			if (entry.getKey() > nextBit)
				continue;
			NTreeSearch subTree = entry.getValue();
			if (subTree != null && subTree.containsSuperSet(bitset, entry.getKey() + 1))
					return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.javasoft.bitset.search.ITreeSearch#forEach(java.util.function.
	 * Consumer)
	 */
	@Override
	public void forEach(Consumer<IBitSet> consumer) {
		if (bitset != null)
			consumer.accept(bitset);
		for (NTreeSearch subtree : subtrees.values()) {
			subtree.forEach(consumer);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.javasoft.bitset.search.ITreeSearch#remove(ch.javasoft.bitset.IBitSet)
	 */
	@Override
	public void remove(IBitSet remove) {
		remove(remove, 0);
	}

	private boolean remove(IBitSet remove, int next) {
		int nextBit = remove.nextSetBit(next);
		if (nextBit < 0) {
			if (bitset.equals(remove))
				bitset = null;
		} else {
			NTreeSearch subTree = subtrees.get(nextBit);
			if (subTree != null) {
				if (subTree.remove(remove, nextBit + 1))
					subtrees.remove(nextBit);
			}
		}
		return bitset == null && subtrees.size() == 0;
	}

	public void forEachSubSet(IBitSet bitset, Consumer<IBitSet> consumer) {
		forEachSubSet(bitset, consumer, 0);
	}

	private void forEachSubSet(IBitSet bitset, Consumer<IBitSet> consumer, int next) {
		if (this.bitset != null)
			consumer.accept(this.bitset);
		
		int nextBit = bitset.nextSetBit(next);
		while (nextBit >= 0) {
			NTreeSearch subTree = subtrees.get(nextBit);
			if (subTree != null) {
				subTree.forEachSubSet(bitset, consumer, nextBit + 1);
			}
			nextBit = bitset.nextSetBit(nextBit + 1);
		}
	}

	public boolean contains(IBitSet test) {
		return contains(test, 0);
	}
	
	private boolean contains(IBitSet test, int next) {
		int nextBit = test.nextSetBit(next);
		if (nextBit < 0) {
			return this.bitset.equals(test);
		} else {
			NTreeSearch subTree = subtrees.get(nextBit);
			if (subTree != null) {
				return subTree.contains(test, nextBit + 1);
			}
			return this.bitset != null && this.bitset.equals(test);
		}
	}
}
