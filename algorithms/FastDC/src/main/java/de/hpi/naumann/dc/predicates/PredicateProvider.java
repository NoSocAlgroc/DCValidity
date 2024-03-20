package de.hpi.naumann.dc.predicates;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.hpi.naumann.dc.predicates.operands.Operand;

public class PredicateProvider {
	private static PredicateProvider instance;

	private Map<Operator, Map<Operand<?>, Map<Operand<?>, Predicate>>> predicates;

	private PredicateProvider() {
		predicates = new HashMap<>();
	}

	private static final Function<? super Operator, ? extends Map<Operand<?>, Map<Operand<?>, Predicate>>> createHashMap = op -> new HashMap<>(); 
	private static final Function<? super Operand<?>, ? extends Map<Operand<?>, Predicate>> createHashMap2 = op -> new HashMap<>(); 
	
	public Predicate getPredicate(Operator op, Operand<?> op1, Operand<?> op2) {
		Map<Operand<?>, Predicate> map = predicates.computeIfAbsent(op, createHashMap).computeIfAbsent(op1, createHashMap2);
		Predicate p = map.get(op2);
		if(p == null) {
			p = new Predicate(op, op1, op2);
			map.put(op2, p);
		}
		return p;
	}

	static {
        instance = new PredicateProvider();
	}

    public static PredicateProvider getInstance() {
        return instance;
    }
}
