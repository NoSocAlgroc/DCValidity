package de.hpi.naumann.dc.input;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultiset;

import de.metanome.algorithm_integration.ColumnIdentifier;

public class ParsedColumn<T extends Comparable<T>> {
	private final String tableName;
	private final String name;
	private final HashMultiset<T> valueSet = HashMultiset.create();
	private final List<T> values = new ArrayList<>();
	private final Class<T> type;
	private final int index;

	public ParsedColumn(String tableName, String name, Class<T> type, int index) {
		this.tableName = tableName;
		this.name = name;
		this.type = type;
		this.index = index;
	}

	public void addLine(T value) {
		valueSet.add(value);
		values.add(value);
	}

	public T getValue(int line) {
		return values.get(line);
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public Class<T> getType() {
		return type;
	}

	public void getKFrequentPredicates(double frequency) {
		// valueSet.stream().filter(value -> {
		// return valueSet.count(value) > frequency;
		// }).map(value -> {
		// return new ConstantOperand(value);
		// }).collect(Collectors.toList());
		// }
	}

	@Override
	public String toString() {
		return /* tableName + "." +*/ name;
	}

	private double getAverage() {
		double avg = 0.0d;
		int size = values.size();
		if (type.equals(Double.class)) {
			for (int i = 0; i < size; i++) {
				Double l = (Double) values.get(i);
				double tmp = l / (double) size;
				avg += tmp;
			}
		} else if (type.equals(Long.class)) {
			for (int i = 0; i < size; i++) {
				Long l = (Long) values.get(i);
				double tmp = l / (double) size;
				avg += tmp;
			}
		}

		return avg;
	}

	private boolean noCrossColumn = true;

	public boolean isComparable(ParsedColumn<?> c2) {
		if (noCrossColumn)
			return this.equals(c2) && (type.equals(Double.class) || type.equals(Long.class));

		if (!type.equals(c2.type))
			return false;

		if (type.equals(Double.class) || type.equals(Long.class)) {
			if (this.equals(c2))
				return true;

			double avg1 = this.getAverage();
			double avg2 = c2.getAverage();
			return Math.min(avg1, avg2) / Math.max(avg1, avg2) > 0.1d;
		}
		return false;
	}

	public boolean isJoinable(ParsedColumn<?> c2) {
		if (noCrossColumn)
			return this.equals(c2);

		if (!type.equals(c2.type))
			return false;

		int totalCount = 0;
		int sharedCount = 0;
		for (T s : valueSet.elementSet()) {
			int thisCount = valueSet.count(s);
			int otherCount = c2.valueSet.count(s);
			sharedCount += Math.min(thisCount, otherCount);
			totalCount += thisCount;
		}
		// if (sharedCount > 0)
		// System.out.println(sharedCount + " " + totalCount + " " + 1.0f *
		// sharedCount / totalCount);
		return sharedCount > totalCount * 0.3f;
	}
	public ColumnIdentifier getColumnIdentifier() {
		return new ColumnIdentifier(tableName, name);
	}
}
