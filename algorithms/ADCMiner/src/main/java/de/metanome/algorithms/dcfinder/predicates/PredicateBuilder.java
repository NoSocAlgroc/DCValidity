package de.metanome.algorithms.dcfinder.predicates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.input.ColumnPair;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

import static de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet.indexProvider;

public class PredicateBuilder {

	private double COMPARE_AVG_RATIO = 0.10d;

	private double minimumSharedValue = 0.30d;

	private boolean noCrossColumn = true;

	private List<Predicate> predicates;

	private Collection<Collection<Predicate>> predicateGroups;

	//数值型单列 数值型跨列
	private Collection<Collection<Predicate>> predicateGroupsNumericalSingleColumn;
	private Collection<Collection<Predicate>> predicateGroupsNumericalCrossColumn;
	private Collection<Collection<Predicate>> predicateGroupsCategoricalSingleColumn;
	private Collection<Collection<Predicate>> predicateGroupsCategoricalCrossColumn;

	private LongBitSet[] mutexMap;   // i -> indices of predicates from the same column pair with predicate i
	private int[] inverseMap;        // i -> index of predicate having inverse op to predicate i

	public PredicateBuilder(Input input, boolean noCrossColumn) {
		predicates = new ArrayList<>();
		predicateGroups = new ArrayList<>();
		this.noCrossColumn = noCrossColumn;

		List<ColumnPair> columnPairs = constructColumnPairs(input);

		for (ColumnPair pair : columnPairs) {
			ColumnOperand<?> o1 = new ColumnOperand<>(pair.getC1(), 0);
			addPredicates(o1, new ColumnOperand<>(pair.getC2(), 1), pair.isJoinable(), pair.isComparable());
//			if (pair.getC1() != pair.getC2())
//				addPredicates(o1, new ColumnOperand<>(pair.getC2(), 0), pair.isJoinable(), false);
		}

		dividePredicateGroupsByType();

		buildMutexMap();
		buildInverseMap();
	}

	public void buildMutexMap() {
		IndexProvider<Predicate> predicateIdProvider = PredicateSet.indexProvider;
		mutexMap = new LongBitSet[predicates.size()];
		for (Predicate p1 : predicates) {
			LongBitSet mutex = new LongBitSet();
			for (Predicate p2 : predicates) {
				//if (!p1.equals(p2) && p1.getOperand1().equals(p2.getOperand1()) && p1.getOperand2().equals(p2.getOperand2()))
				if (p1.getOperand1().equals(p2.getOperand1()) && p1.getOperand2().equals(p2.getOperand2()))
					mutex.set(predicateIdProvider.getIndex(p2));
			}
			mutexMap[predicateIdProvider.getIndex(p1)] = mutex;
		}
	}

	public LongBitSet[] getMutexMap() {
		return mutexMap;
	}

	public void buildInverseMap() {
		IndexProvider<Predicate> predicateIdProvider = PredicateSet.indexProvider;
		inverseMap = new int[predicateIdProvider.size()];
		for (var r : predicateIdProvider.entrySet())
			inverseMap[r.getValue()] = predicateIdProvider.getIndex(r.getKey().getInverse());
	}
	public PredicateSet getInverse(LongBitSet predicateSet) {
		LongBitSet inverse = new LongBitSet();
		for (int l = predicateSet.nextSetBit(0); l >= 0; l = predicateSet.nextSetBit(l + 1))
			inverse.set(inverseMap[l]);
		return new PredicateSet(inverse);
	}


	public Operator getoperator(String s) {
		switch(s) {
			case ">=" : return Operator.GREATER_EQUAL;
			case ">":return Operator.GREATER;
			case "==":return Operator.EQUAL;
			case "<": return Operator.LESS;
			case "<=":return Operator.LESS_EQUAL;
			case "<>": return Operator.UNEQUAL;
		}
		return null;
	}


	private ArrayList<ColumnPair> constructColumnPairs(Input input) {
		ArrayList<ColumnPair> pairs = new ArrayList<ColumnPair>();
		for (int i = 0; i < input.getColumns().length; ++i) {
			ParsedColumn<?> c1 = input.getColumns()[i];
			for (int j = i; j < input.getColumns().length; ++j) {
				ParsedColumn<?> c2 = input.getColumns()[j];
				boolean joinable = isJoinable(c1, c2);
				boolean comparable = isComparable(c1, c2);
				if (joinable || comparable)
					/**
					 * different with hydra
					 * do not influence result
					 * */
//					 pairs.add(new ColumnPair(c1, c2, joinable, comparable));
					pairs.add(new ColumnPair(c1, c2, true, comparable));
			}
		}
		return pairs;
	}

	private boolean isJoinable(ParsedColumn<?> c1, ParsedColumn<?> c2) {
		if (noCrossColumn)
			return c1.equals(c2);

		if (!c1.getType().equals(c2.getType()))
			return false;

		return c1.getSharedPercentage(c2) > minimumSharedValue;
	}

	private boolean isComparable(ParsedColumn<?> c1, ParsedColumn<?> c2) {
		if (noCrossColumn)
			return c1.equals(c2) && (c1.getType().equals(Double.class) || c1.getType().equals(Long.class));

		if (!c1.getType().equals(c2.getType()))
			return false;

		if (c1.getType().equals(Double.class) || c1.getType().equals(Long.class)) {
			if (c1.equals(c2))
				return true;

			double avg1 = c1.getAverage();
			double avg2 = c2.getAverage();
			return Math.min(avg1, avg2) / Math.max(avg1, avg2) > COMPARE_AVG_RATIO;
		}
		return false;
	}

	public Collection<Predicate> getPredicates() {
		return predicates;
	}

	public Collection<Collection<Predicate>> getPredicateGroups() {
		return predicateGroups;
	}

	public Collection<Collection<Predicate>> getNumericalSingleColPredicates() {

		Collection<Collection<Predicate>> numericalPredicates = new ArrayList<>();

		for (Collection<Predicate> predicateGroup : getPredicateGroups()) {

			if (predicateGroup.size() == 6) {
				numericalPredicates.add(predicateGroup);
			}
		}

		return numericalPredicates;
	}

	public Collection<Collection<Predicate>> getCategoricalSingleColPredicates() {

		Collection<Collection<Predicate>> categoricalPredicates = new ArrayList<>();

		for (Collection<Predicate> predicateGroup : getPredicateGroups()) {

			if (predicateGroup.size() == 2) {
				categoricalPredicates.add(predicateGroup);
			}
		}

		return categoricalPredicates;
	}

	private void dividePredicateGroupsByType() {

		predicateGroupsNumericalSingleColumn = new ArrayList<>();
		predicateGroupsNumericalCrossColumn = new ArrayList<>();
		predicateGroupsCategoricalSingleColumn = new ArrayList<>();
		predicateGroupsCategoricalCrossColumn = new ArrayList<>();

		for (Collection<Predicate> predicateGroup : getPredicateGroups()) {

			if (predicateGroup.size() == 6) {// numeric
				if (predicateGroup.iterator().next().isCrossColumn()) {
					predicateGroupsNumericalCrossColumn.add(predicateGroup);
				} else {
					predicateGroupsNumericalSingleColumn.add(predicateGroup);
				}
			}

			if (predicateGroup.size() == 2) {// categorical
				if (predicateGroup.iterator().next().isCrossColumn()) {
					predicateGroupsCategoricalCrossColumn.add(predicateGroup);
				} else {
					predicateGroupsCategoricalSingleColumn.add(predicateGroup);
				}
			}
		}

	}

	public Collection<Collection<Predicate>> getPredicateGroupsNumericalSingleColumn() {
		return predicateGroupsNumericalSingleColumn;
	}

	public Collection<Collection<Predicate>> getPredicateGroupsNumericalCrossColumn() {
		return predicateGroupsNumericalCrossColumn;
	}

	public Collection<Collection<Predicate>> getPredicateGroupsCategoricalSingleColumn() {
		return predicateGroupsCategoricalSingleColumn;
	}

	public Collection<Collection<Predicate>> getPredicateGroupsCategoricalCrossColumn() {
		return predicateGroupsCategoricalCrossColumn;
	}

	public Predicate getPredicateByType(Collection<Predicate> predicateGroup, Operator type) {
		Predicate pwithtype = null;
		for (Predicate p : predicateGroup) {
			if (p.getOperator().equals(type)) {
				pwithtype = p;
				break;
			}
		}
		return pwithtype;
	}

	public Collection<ColumnPair> getColumnPairs() {
		Set<List<ParsedColumn<?>>> joinable = new HashSet<>();
		Set<List<ParsedColumn<?>>> comparable = new HashSet<>();
		Set<List<ParsedColumn<?>>> all = new HashSet<>();
		for (Predicate p : predicates) {
			List<ParsedColumn<?>> pair = new ArrayList<>();
			pair.add(p.getOperand1().getColumn());
			pair.add(p.getOperand2().getColumn());

			if (p.getOperator() == Operator.EQUAL)
				joinable.add(pair);

			if (p.getOperator() == Operator.LESS)
				comparable.add(pair);

			all.add(pair);
		}

		Set<ColumnPair> pairs = new HashSet<>();
		for (List<ParsedColumn<?>> pair : all) {
			pairs.add(new ColumnPair(pair.get(0), pair.get(1), joinable.contains(pair), comparable.contains(pair)));
		}
		return pairs;
	}

	private void addPredicates(ColumnOperand<?> o1, ColumnOperand<?> o2, boolean joinable, boolean comparable) {
		List<Predicate> predicates = new ArrayList<>();
		for (Operator op : Operator.values()) {
			if (op == Operator.EQUAL || op == Operator.UNEQUAL) {
				if (joinable && (o1.getIndex() != o2.getIndex()))
					predicates.add(predicateProvider.getPredicate(op, o1, o2));
			} else if (comparable) {
				predicates.add(predicateProvider.getPredicate(op, o1, o2));
			}
		}
		this.predicates.addAll(predicates);
		this.predicateGroups.add(predicates);
	}

	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();
}
