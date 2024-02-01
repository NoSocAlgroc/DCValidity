import de.metanome.algorithms.dcfinder.DCFinder;

public class Main  {

    public static void main(String[] args) {
        String fp = "./dataset/airport.csv";
        double threshold = 0.01d;
        boolean singleColumn = false;
        int rowLimit = -1;

        DCFinder dcfinder = new DCFinder(threshold, singleColumn);
        dcfinder.run(fp, rowLimit);
        System.out.println();
    }

}
