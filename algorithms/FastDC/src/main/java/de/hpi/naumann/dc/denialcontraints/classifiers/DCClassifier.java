package de.hpi.naumann.dc.denialcontraints.classifiers;

import java.util.ArrayList;
import java.util.Collection;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.Operator;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;

public class DCClassifier {

	public enum DCType {
		FD, UCC, BUSINESSRULE, NONE
	};

	private DenialConstraintSet fds = new DenialConstraintSet();
	private DenialConstraintSet uccs = new DenialConstraintSet();
	private DenialConstraintSet brules = new DenialConstraintSet();

	DenialConstraintSet dcs;

	public DCClassifier(DenialConstraintSet dcs) {
		this.dcs = dcs;
		// classifyDCs();
	}

	public void classifyDCs() {
		for (DenialConstraint dc : dcs) {

			DCType type = classify(dc);
			if (type == DCType.FD) {
				fds.add(dc);
			} else if (type == DCType.UCC) {
				uccs.add(dc);
			} else if (type == DCType.BUSINESSRULE) {
				brules.add(dc);
			} else {
				System.out.println("None:" + dc);
			}

		}

	}

	public DCType classify(DenialConstraint dc) {

		boolean hasUnequal = false;

		for (Predicate predicate : dc.getPredicateSet()) {
			if (predicate.getOperator() != Operator.EQUAL && predicate.getOperator() != Operator.UNEQUAL)
				return DCType.BUSINESSRULE;

			if (predicate.getOperator() == Operator.UNEQUAL)
				hasUnequal = true;

			if (!(predicate.getOperand1() instanceof ColumnOperand)
					|| !(predicate.getOperand2() instanceof ColumnOperand))
				return DCType.NONE;

			ColumnOperand<?> o1 = (ColumnOperand<?>) predicate.getOperand1();
			ColumnOperand<?> o2 = (ColumnOperand<?>) predicate.getOperand2();

			if (o1.getColumn() != o2.getColumn() || o1.getIndex() == o2.getIndex())
				return DCType.NONE;
		}

		if (hasUnequal) {
			return DCType.FD;
		}

		return DCType.UCC;

	}

	public static void identify(DenialConstraint dc) {
		isFD(dc);
	}

	public static boolean isFD(DenialConstraint dc) {
		ParsedColumn<?> rhs = null;
		Collection<ParsedColumn<?>> lhs = new ArrayList<>();

		for (Predicate predicate : dc.getPredicateSet()) {
			if (predicate.getOperator() != Operator.EQUAL && predicate.getOperator() != Operator.UNEQUAL)
				return false;

			if (!(predicate.getOperand1() instanceof ColumnOperand)
					|| !(predicate.getOperand2() instanceof ColumnOperand))
				return false;

			ColumnOperand<?> o1 = (ColumnOperand<?>) predicate.getOperand1();
			ColumnOperand<?> o2 = (ColumnOperand<?>) predicate.getOperand2();

			if (o1.getColumn() != o2.getColumn() || o1.getIndex() == o2.getIndex())
				return false;

			if (predicate.getOperator() == Operator.EQUAL)
				lhs.add(o1.getColumn());
			else {
				if (rhs != null)
					return false;
				rhs = o1.getColumn();
			}
		}

		for (ParsedColumn<?> c : lhs)
			System.out.print(c.getName() + "\t");
		System.out.print("->");
		if (rhs != null)
			System.out.println(rhs.getName());
		else
			System.out.println("ALL");

		return true;
	}

	public DenialConstraintSet getFds() {
		return fds;
	}

	public void setFds(DenialConstraintSet fds) {
		this.fds = fds;
	}

	public DenialConstraintSet getUccs() {
		return uccs;
	}

	public void setUccs(DenialConstraintSet uccs) {
		this.uccs = uccs;
	}

	public DenialConstraintSet getBrules() {
		return brules;
	}

	public void setBrules(DenialConstraintSet brules) {
		this.brules = brules;
	}

}
