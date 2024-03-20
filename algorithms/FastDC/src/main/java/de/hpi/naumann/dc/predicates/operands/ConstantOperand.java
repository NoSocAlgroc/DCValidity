package de.hpi.naumann.dc.predicates.operands;

import java.util.HashSet;
import java.util.Set;

public class ConstantOperand<T extends Comparable<T>> extends Operand<T> {
	private T value;
	
	public ConstantOperand(T value) {
		this.value = value;
	}

	@Override
	public T getValue(int line, int line2) {
		return value;
	}

	@Override
	public Set<Object> getAlphabet() {
		Set<Object> alphabet = new HashSet<>();
		alphabet.add(value);
		return alphabet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConstantOperand<?> other = (ConstantOperand<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "const:" + value;
	}
}
