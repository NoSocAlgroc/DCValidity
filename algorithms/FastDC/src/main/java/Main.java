import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.hpi.naumann.dc.algorithms.fastdc.FastDC;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.input.DefaultFileInputGenerator;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.cover.MinimalCoverCandidate;

public class Main {


    public static void main(String[] args) throws Exception {
        String fp = args[0];
        double threshold = Double.parseDouble(args[1]);//Unused for FastDC
        int rowLimit = Integer.parseInt(args[2]);       
        
        FastDC fastADC = new FastDC();
        File f=new File(fp);
        DefaultFileInputGenerator fgen=new DefaultFileInputGenerator(f);
        Input input = new Input(fgen.generateNewCopy(), rowLimit);
        PredicateBuilder predicateBuilder = new PredicateBuilder(input);
        DenialConstraintSet dcs = fastADC.run(input, predicateBuilder);
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
