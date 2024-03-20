package de.hpi.naumann.dc.predicates.operands;

import java.util.Set;

public abstract class Operand<T extends Comparable<T>> {

	//public abstract Operand<T> getSymmetric();

	public abstract T getValue(int line, int line2);

	public abstract Set<Object> getAlphabet();

}
