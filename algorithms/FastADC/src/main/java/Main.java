import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import FastADC.FastADC;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;

public class Main {

    public static void main(String[] args) {
        String fp = "./dataset/hospital.csv";
        double threshold = 0.001d;
        int rowLimit = -1;              // limit the number of tuples in dataset, -1 means no limit
        int shardLength = 350;
        boolean linear = false;         // linear single-thread in EvidenceSetBuilder
        boolean singleColumn = true;   // only single-attribute predicates

        FastADC fastADC = new FastADC(singleColumn, threshold, shardLength, linear);
        DenialConstraintSet dcs = fastADC.buildApproxDCs(fp, rowLimit);
        System.out.println();

        

        // Specify the file path
        String filePath = "output.txt";

        // Sample lines to write to the file

        // Write lines to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (DenialConstraint dc : dcs) {
                writer.write(dc.toString());
                writer.newLine(); // Add a newline after each line
            }
            System.out.println("Lines written to file successfully!");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

}
