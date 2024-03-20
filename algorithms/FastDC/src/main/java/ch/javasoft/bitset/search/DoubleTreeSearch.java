package ch.javasoft.bitset.search;

import java.util.Set;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;

public class DoubleTreeSearch implements ITreeSearch {

	private NTreeSearch ntree = new NTreeSearch();
	private TreeSearch tree = new TreeSearch();
	
	@Override
	public boolean add(IBitSet bs) {
		ntree.add(bs);
		return tree.add(bs);
	}

	@Override
	public void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer) {
		tree.forEachSuperSet(bitset, consumer);
	}

	@Override
	public void forEach(Consumer<IBitSet> consumer) {
		ntree.forEach(consumer);
	}

	@Override
	public void remove(IBitSet remove) {
		ntree.remove(remove);
		tree.remove(remove);
	}

	@Override
	public boolean containsSubset(IBitSet bitset) {
		return ntree.containsSubset(bitset);
	}

	@Override
	public Set<IBitSet> getAndRemoveGeneralizations(IBitSet bitset) {
		Set<IBitSet> res = ntree.getAndRemoveGeneralizations(bitset);
		res.stream().forEach(b -> tree.remove(b));
		return res;
	}

}
