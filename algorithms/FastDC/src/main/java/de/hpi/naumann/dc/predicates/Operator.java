package de.hpi.naumann.dc.predicates;

/**
 * See Table 2 page 1500
 * 
 * @author tbleifuss
 *
 */
public enum Operator {
	EQUAL, UNEQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL;

	private Operator inverse;
	private Operator symmetric;
	private Operator[] implications;
	private Operator[] transitives;
	private String shortString;
	private boolean needsDouble;

	public Operator getInverse() {
		return inverse;
	}

	public Operator getSymmetric() {
		return symmetric;
	}

	public Operator[] getImplications() {
		return implications;
	}

	public String getShortString() {
		return shortString;
	}
	
	public Operator[] getTransitives() {
		return transitives;
	}

	public boolean isTransitiveWith(Operator op) {
		for (Operator i : transitives) {
			if (i == op)
				return true;
		}
		return false;
	}

	public boolean needsDouble() {
		return needsDouble;
	}

	static {
		EQUAL.inverse = UNEQUAL;
		UNEQUAL.inverse = EQUAL;
		GREATER.inverse = LESS_EQUAL;
		LESS.inverse = GREATER_EQUAL;
		GREATER_EQUAL.inverse = LESS;
		LESS_EQUAL.inverse = GREATER;
	}

	static {
		EQUAL.symmetric = EQUAL;
		UNEQUAL.symmetric = UNEQUAL;
		GREATER.symmetric = LESS;
		LESS.symmetric = GREATER;
		GREATER_EQUAL.symmetric = LESS_EQUAL;
		LESS_EQUAL.symmetric = GREATER_EQUAL;
	}

	static {
		EQUAL.implications = new Operator[] { EQUAL, GREATER_EQUAL, LESS_EQUAL };
		UNEQUAL.implications = new Operator[] { UNEQUAL };
		GREATER.implications = new Operator[] { GREATER, GREATER_EQUAL, UNEQUAL };
		LESS.implications = new Operator[] { LESS, LESS_EQUAL, UNEQUAL };
		GREATER_EQUAL.implications = new Operator[] { GREATER_EQUAL };
		LESS_EQUAL.implications = new Operator[] { LESS_EQUAL };
	}

	static {
		EQUAL.transitives = new Operator[] { EQUAL };
		UNEQUAL.transitives = new Operator[] {EQUAL };
		GREATER.transitives = new Operator[] { GREATER, GREATER_EQUAL, EQUAL };
		LESS.transitives = new Operator[] { LESS, LESS_EQUAL, EQUAL };
		GREATER_EQUAL.transitives = new Operator[] { GREATER, GREATER_EQUAL, EQUAL };
		LESS_EQUAL.transitives = new Operator[] { LESS, LESS_EQUAL, EQUAL };
	}

	static {
		EQUAL.shortString = "==";
		UNEQUAL.shortString = "<>";
		GREATER.shortString = ">";
		LESS.shortString = "<";
		GREATER_EQUAL.shortString = ">=";
		LESS_EQUAL.shortString = "<=";
	}

	static {
		EQUAL.needsDouble = false;
		UNEQUAL.needsDouble = false;
		GREATER.needsDouble = true;
		LESS.needsDouble = true;
		GREATER_EQUAL.needsDouble = true;
		LESS_EQUAL.needsDouble = true;
	}

	public boolean eval(Comparable value1, Object value2) {
		if (this == EQUAL) {
			return value1.equals(value2);
		} else if (this == UNEQUAL) {
			return !value1.equals(value2);
		} else {
			int c = value1.compareTo(value2);
			switch (this) {
			case GREATER_EQUAL:
				return c >= 0;
			case LESS:
				return c < 0;
			case LESS_EQUAL:
				return c <= 0;
			case GREATER:
				return c > 0;
			}
		}

		return false;
	}

	public boolean eval(int value1, int value2) {
		switch (this) {
		case EQUAL:
			return value1 == value2;
		case GREATER:
			return value1 > value2;
		case GREATER_EQUAL:
			return value1 >= value2;
		case LESS:
			return value1 < value2;
		case LESS_EQUAL:
			return value1 <= value2;
		case UNEQUAL:
			return value1 != value2;
		}
		return false;
	}

	private static final double EPSILON = 0.00001d;

	public boolean eval(double value1, double value2) {
		switch (this) {
		case EQUAL:
			return Math.abs(value1 - value2) < EPSILON;
		case UNEQUAL:
			return Math.abs(value1 - value2) >= EPSILON;
		case GREATER:
			return value1 > value2;
		case GREATER_EQUAL:
			return value1 >= value2;
		case LESS:
			return value1 < value2;
		case LESS_EQUAL:
			return value1 <= value2;
		}
		return false;
	}
}
