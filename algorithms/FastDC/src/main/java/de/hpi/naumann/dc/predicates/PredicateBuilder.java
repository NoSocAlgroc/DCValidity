package de.hpi.naumann.dc.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.hpi.naumann.dc.input.ColumnPair;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.operands.Operand;

public class PredicateBuilder {

	public PredicateBuilder(Input input) {
		predicates = new HashSet<>();
		predicateGroups = new ArrayList<>();

		input.getColumnPairs().forEach(pair -> {
			Operand<?> o1 = new ColumnOperand<>(pair.getC1(), 0);
			addPredicates(o1, new ColumnOperand<>(pair.getC2(), 1), pair.isJoinable(), pair.isComparable());
			if (pair.getC1() != pair.getC2()) {
				addPredicates(o1, new ColumnOperand<>(pair.getC2(), 0), pair.isJoinable(), false);
			}
		});
	}

	public PredicateBuilder(Set<Predicate> predicates) {
		this.predicates = predicates;

		Function<Predicate, List<Operand<?>>> keyExtractor = predicate -> Arrays
				.<Operand<?>> asList(predicate.getOperand1(), predicate.getOperand2());

		this.predicateGroups = predicates.stream().collect(Collectors.groupingBy(keyExtractor)).values().stream()
				.map(list -> {
					return new HashSet<>(list);
				}).collect(Collectors.toList());
	}

	private Set<Predicate> predicates;

	private Collection<Collection<Predicate>> predicateGroups;

	public Set<Predicate> getPredicates() {
		return predicates;
	}

	public Collection<Collection<Predicate>> getPredicateGroups() {
		return predicateGroups;
	}

	private void addPredicates(Operand<?> o1, Operand<?> o2, boolean joinable, boolean comparable) {
		Set<Predicate> predicates = new HashSet<Predicate>();
		for (Operator op : Operator.values()) {
			// EQUAL and UNEQUAL must be joinable, all other comparable
			if (op == Operator.EQUAL || op == Operator.UNEQUAL) {
				if (joinable) {
					predicates.add(predicateProvider.getPredicate(op, o1, o2));
				}
			} else if (comparable) {
				predicates.add(predicateProvider.getPredicate(op, o1, o2));
			}
		}
		this.predicates.addAll(predicates);
		this.predicateGroups.add(predicates);
	}

	public Collection<ColumnPair> getColumnPairs() {
		Set<List<ParsedColumn>> joinable = new HashSet<>();
		Set<List<ParsedColumn>> comparable = new HashSet<>();
		Set<List<ParsedColumn>> all = new HashSet<>();
		for (Predicate p : predicates) {
			List<ParsedColumn> pair = new ArrayList<>();
			pair.add(((ColumnOperand) p.getOperand1()).getColumn());
			pair.add(((ColumnOperand) p.getOperand2()).getColumn());

			if (p.getOperator() == Operator.EQUAL)
				joinable.add(pair);

			if (p.getOperator() == Operator.LESS)
				comparable.add(pair);

			all.add(pair);
		}

		Set<ColumnPair> pairs = new HashSet<>();
		for (List<ParsedColumn> pair : all) {
			pairs.add(new ColumnPair(pair.get(0), pair.get(1), joinable.contains(pair), comparable.contains(pair)));
		}
		return pairs;
	}

	static final PredicateProvider predicateProvider = PredicateProvider.getInstance();
}
