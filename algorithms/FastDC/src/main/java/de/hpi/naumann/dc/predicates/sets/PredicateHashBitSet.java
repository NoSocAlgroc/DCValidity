package de.hpi.naumann.dc.predicates.sets;

public class PredicateHashBitSet extends PredicateHashSet {
	
	private IPredicateSetBinary mc;
	
	public PredicateHashBitSet(IPredicateSet path, IPredicateSetBinary mc) {
		super(path);
		this.mc=mc;
	}

	@Override
	public IPredicateSetBinary getIPredicateSetBinary() {
		
		return mc;
	}

	

}
