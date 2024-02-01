package de.metanome.algorithms.dcfinder;


import approxcover.ApproxDCBuilder;
import de.metanome.algorithms.dcfinder.evidenceset.TroveEvidenceSet;
import de.metanome.algorithms.dcfinder.evidenceset.builders.SplitReconstructEvidenceSetBuilder;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;

import java.io.File;
import java.text.SimpleDateFormat;

public class DCFinder {

    protected long chunkLength = 10000 * 5000;
    protected int bufferLength = 5000;

    private String dataFp;

    protected boolean singleColumn;
    protected double errorThreshold;
    protected long violationsThreshold = 0L;
    protected long satisfactionThreshold;
    protected long rsize = 0;

    public DCFinder(double threshold, boolean _singleColumn) {
        errorThreshold = threshold;
        singleColumn = _singleColumn;
    }

    public void run(String fp, int rowLimit) {
        dataFp = fp;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("INPUT FILE: " + fp);

        long t00 = System.currentTimeMillis();
        RelationalInput data = new RelationalInput(new File(fp));
        Input input = new Input(data, rowLimit);

        PredicateBuilder pBuilder = new PredicateBuilder(input, singleColumn);
        System.out.println("predicate space size: " + pBuilder.getPredicates().size());

        //PLI
        input.buildPLIs();
        rsize = input.getLineCount();
        long t_pre = System.currentTimeMillis() - t00;
        System.out.println("[Common] Pre-process time: " + t_pre + "ms");

        // build evidence set
        System.out.println(" [Evi t0] " + sdf.format(System.currentTimeMillis()));
        long t10 = System.currentTimeMillis();
        var evidenceSetBuilder = new SplitReconstructEvidenceSetBuilder(input, pBuilder, chunkLength, bufferLength);
        TroveEvidenceSet evidenceSet =  evidenceSetBuilder.buildEvidenceSet();
        setViolationsThreshold(evidenceSet.getTotalCount());
        long t_evi = System.currentTimeMillis() - t10;
        System.out.println(" [Evi Builder] evidence set size: " + evidenceSet.size());
        System.out.println(" [Evi Builder] evidence count: " + evidenceSet.getTotalCount());
        System.out.println("[Evi Builder] evidence time: " + t_evi + "ms");

        // [ADC] Evidence inversion
        System.out.println(" [ADC t0] " + sdf.format(System.currentTimeMillis()));
        long t30 = System.currentTimeMillis();
        ApproxDCBuilder coverSearcher = new ApproxDCBuilder(satisfactionThreshold, pBuilder);
        coverSearcher.buildApproxDCs(evidenceSet);
        long t_adc_ivs = System.currentTimeMillis() - t30;
        System.out.println("[ADC] inversion time: " + t_adc_ivs + "ms\n");

        System.out.println("[ADC] Total computing time: " + (t_pre + t_evi + t_adc_ivs) + " ms");
    }

    private void setViolationsThreshold(long totalCount) {
        violationsThreshold = (long) Math.floor(((double) totalCount * errorThreshold));
        satisfactionThreshold = totalCount - violationsThreshold;
        System.out.println("Error threshold: " + errorThreshold);
        System.out.println("Discovering DCs with at most " + violationsThreshold + " violating tuple pairs");
    }

}
