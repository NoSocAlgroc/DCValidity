package de.hpi.naumann.dc.predicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.operands.Operand;

public class Predicate implements PartitionRefiner {

	private final Operator op;
	private final Operand<?> operand1;
	private final Operand<?> operand2;
	private double probability=0;

	public Predicate(Operator op, Operand<?> operand1, Operand<?> operand2) {
		if (op == null)
			throw new IllegalArgumentException("op must not be null.");

		if (operand1 == null)
			throw new IllegalArgumentException("operand1 must not be null.");

		if (operand2 == null)
			throw new IllegalArgumentException("operand2 must not be null.");

		this.op = op;
		this.operand1 = operand1;
		this.operand2 = operand2;
	}

	private Predicate symmetric;
	public Predicate getSymmetric() {
		if(symmetric != null)
			return symmetric;
		
		symmetric = predicateProvider.getPredicate(op.getSymmetric(), operand2, operand1);
		return symmetric;
	}

	private List<Predicate> implications = null;
	public Collection<Predicate> getImplications() {
		if(this.implications != null)
			return implications;
		Operator[] opImplications = op.getImplications();

		List<Predicate> implications = new ArrayList<>(opImplications.length);
		for (int i = 0; i < opImplications.length; ++i) {
			implications.add(predicateProvider.getPredicate(opImplications[i], operand1, operand2));
		}
		this.implications = Collections.unmodifiableList(implications);
		return implications;
	}

	public boolean implies(Predicate add) {
		if (add.operand1.equals(this.operand1) && add.operand2.equals(this.operand2))
			for (Operator i : op.getImplications())
				if (add.op == i)
					return true;
		return false;
	}

	public Operator getOperator() {
		return op;
	}

	public Operand<?> getOperand1() {
		return operand1;
	}

	public Operand<?> getOperand2() {
		return operand2;
	}

	public Predicate getInvT1T2() {
		if (operand1 instanceof ColumnOperand && operand2 instanceof ColumnOperand) {
			ColumnOperand<?> o1 = (ColumnOperand<?>) operand1;
			ColumnOperand<?> o2 = (ColumnOperand<?>) operand2;

			return predicateProvider.getPredicate(op, o1.getInvT1T2(), o2.getInvT1T2());
		}
		return null;
	}

	private Predicate inverse;
	public Predicate getInverse() {
		if(inverse != null)
			return inverse;
		
		inverse = predicateProvider.getPredicate(op.getInverse(), operand1, operand2);
		return inverse;
	}

	public boolean satisfies(int line1, int line2) {
		return op.eval(operand1.getValue(line1, line2), operand2.getValue(line1, line2));
	}

	@Override
	public String toString() {
		return operand1.toString() + " " + op.getShortString() + " " + operand2.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((operand1 == null) ? 0 : operand1.hashCode());
		result = prime * result + ((operand2 == null) ? 0 : operand2.hashCode());
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
		Predicate other = (Predicate) obj;
		if (op != other.op)
			return false;
		if (operand1 == null) {
			if (other.operand1 != null)
				return false;
		} else if (!operand1.equals(other.operand1))
			return false;
		if (operand2 == null) {
			if (other.operand2 != null)
				return false;
		} else if (!operand2.equals(other.operand2))
			return false;
		return true;
	}

	public Set<Object> getAlphabet() {
		Set<Object> alphabet = new HashSet<>();
		alphabet.add(op);
		alphabet.addAll(operand1.getAlphabet());
		alphabet.addAll(operand2.getAlphabet());
		return alphabet;
	}
	
	
	
	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	static final PredicateProvider predicateProvider = PredicateProvider.getInstance();  
}
