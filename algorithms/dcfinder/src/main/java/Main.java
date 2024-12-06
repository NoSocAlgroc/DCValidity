import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.DCFinder;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.input.DefaultFileInputGenerator;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;

import de.metanome.algorithms.dcfinder.setcover.partial.MinimalCoverCandidate;
public class Main {

    public static void main(String[] args) throws Exception {
        String fp = args[0];
        double threshold = Double.parseDouble(args[1]);
        int rowLimit = Integer.parseInt(args[2]);              // limit the number of tuples in dataset, -1 means no limit

        
        DCFinder dcFinder = new DCFinder();
        File f=new File(fp);
        DefaultFileInputGenerator fgen=new DefaultFileInputGenerator(f);
        Input input = new Input(fgen.generateNewCopy(), rowLimit);
        PredicateBuilder predicateBuilder = new PredicateBuilder(input,true, 0.3d);
        DenialConstraintSet dcs = dcFinder.run(input, predicateBuilder,threshold);
        System.out.println();

        fgen.close();

        // Specify the file path
        String filePath = "output.txt";

        // Sample lines to write to the file

        // Write lines to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (DenialConstraint dc : dcs) {
                writer.write(dc.toResult().toString());
                writer.newLine(); // Add a newline after each line
            }
            System.out.println("Lines written to file successfully!");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

}
