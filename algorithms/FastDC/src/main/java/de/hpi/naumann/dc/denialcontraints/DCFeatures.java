package de.hpi.naumann.dc.denialcontraints;

import java.util.Locale;

public class DCFeatures {

	public static final double DEFAULT_ALPHA = 0.5D;
	public static final double TO_BE_ESTIMATED = -1.0D;
	private double coverage;
	private double succinctness;
	private double predicateMulti;
	
	double[] violationProbabilities; //
	long[] counters; //

	public DCFeatures() {
		super();
		this.coverage = TO_BE_ESTIMATED;
		this.succinctness = TO_BE_ESTIMATED;
	}

	public DCFeatures(double coverage, double succinctness) {
		this.coverage = coverage;
		this.succinctness = succinctness;
	}

	public double getCoverage() {
		if(coverage<0)
			System.out.println("Negativo");
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public double getSuccinctness() {
		return succinctness;
	}

	public void setSuccinctness(double succinctness) {
		this.succinctness = succinctness;
	}

	public double getInterestingness() {
		return DEFAULT_ALPHA * succinctness + (1.0 - DEFAULT_ALPHA) * coverage;
	}

	
	
	
	

	public double[] getViolationProbabilities() {
		return violationProbabilities;
	}
	
	public String getCounterStr() {
		String sb="";
		for(long d: counters)
			sb+=" "+d;
		return sb;
		
	}
	

	public long[] getCounters() {
		return counters;
	}

	


	public void setCounters(long[] counters) {
		this.counters = counters;
	}

	public void setViolationProbabilities(double[] violationProbabilities) {
		this.violationProbabilities = violationProbabilities;
	}

	public String getFeature(String identifier) {
		String str= "-1";
		double d=0d;
		if (identifier.equals("Inter")) {
			d = getInterestingness();
		}
		else if (identifier.equals("Succ")) {
			d = getSuccinctness();
		}
		else if (identifier.equals("Cover")) {
			d = getCoverage();
		}
		else if (identifier.equals("Level1")) {
			
			d = violationProbabilities[0];
		}
		else if (identifier.equals("Level2")) {
			if(violationProbabilities.length>2)
				d = violationProbabilities[1];
		}
		else if (identifier.equals("Level3")) {
			if(violationProbabilities.length>3)
			d = violationProbabilities[2];
		}
		else if (identifier.equals("Level4")) {
			if(violationProbabilities.length>4)
			d = violationProbabilities[3];
		}
		else if (identifier.equals("Predicate")) {
			d = getPredicateMulti();
		}
		
		
		str = String.format(Locale.US,"%f", d);
		return str;
	}

	

	public double getPredicateMulti() {
		return predicateMulti;
	}

	public void setPredicateMulti(double predicateMulti) {
		this.predicateMulti = predicateMulti;
	}

	public double getInterestingness(double alpha) {
		return alpha * succinctness + (1.0 - alpha) * coverage;
	}
	
	
	

}
