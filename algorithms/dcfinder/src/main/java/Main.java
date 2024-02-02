import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.metanome.algorithms.dcfinder.DCFinder;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.input.DefaultFileInputGenerator;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;

public class Main {

    public static void main(String[] args) throws Exception {
        String fp = "./datasets/Hospital.csv";
        double threshold = 0.01d;
        int rowLimit = 20000;              // limit the number of tuples in dataset, -1 means no limit
        int shardLength = 350;
        boolean linear = false;         // linear single-thread in EvidenceSetBuilder
        boolean singleColumn = true;   // only single-attribute predicates
        
        DCFinder fastADC = new DCFinder();
        File f=new File(fp);
        DefaultFileInputGenerator fgen=new DefaultFileInputGenerator(f);
        Input input = new Input(fgen.generateNewCopy(), rowLimit);
        PredicateBuilder predicateBuilder = new PredicateBuilder(input,true, 0.3d);
        DenialConstraintSet dcs = fastADC.run(input, predicateBuilder);
        System.out.println();
        fgen.close();

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
