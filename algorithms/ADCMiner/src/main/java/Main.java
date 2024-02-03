import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import de.metanome.algorithms.dcfinder.DCFinder;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;

public class Main  {

    public static void main(String[] args) {
        String fp = "./datasets/airport.csv";
        double threshold = 0.01d;
        boolean singleColumn = false;
        int rowLimit = 10000;

        DCFinder dcfinder = new DCFinder(threshold, singleColumn);
        DenialConstraintSet dcs=dcfinder.run(fp, rowLimit);
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
