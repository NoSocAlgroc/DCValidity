package de.metanome.algorithms.dcfinder.input;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

//import de.metanome.algorithm_integration.input.InputIterationException;
//import de.metanome.algorithm_integration.input.RelationalInput;
import com.csvreader.CsvReader;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.input.partitions.clusters.PLI;
import de.metanome.algorithms.dcfinder.input.partitions.clusters.TupleIDProvider;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.TIntIntMap;

public class Input {
    private final int lineCount;
    private final List<ParsedColumn<?>> parsedColumns;
    private final String name;

    public Input(RelationalInput relationalInput, int rowLimit) {
        final int columnCount = relationalInput.numberOfColumns();
        Column[] columns = readRelationalInputToColumns(relationalInput, rowLimit, true);
        this.lineCount = columns.length > 0 ? columns[0].values.size() : 0;

        parsedColumns = new ArrayList<>(columns.length);
        createParsedColumns(relationalInput, columns);

        name = relationalInput.relationName();
    }

    private Column[] readRelationalInputToColumns(RelationalInput relationalInput, int rowLimit, boolean csv) {
        final int columnCount = relationalInput.numberOfColumns();
        Column[] columns = new Column[columnCount];
        for (int i = 0; i < columnCount; ++i)
            columns[i] = new Column(relationalInput.relationName(), relationalInput.columnNames[i]);

        int nLine = 0;
        try {
            CsvReader csvReader = new CsvReader(relationalInput.filePath, ',', StandardCharsets.UTF_8);
            csvReader.readHeaders();    // skip the header
            while (csvReader.readRecord()) {
                String[] line = csvReader.getValues();
                for (int i = 0; i < columnCount; ++i)
                    columns[i].addLine(line[i]);

                ++nLine;
                if (rowLimit > 0 && nLine >= rowLimit)
                    break;
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return columns;
    }

    private void createParsedColumns(RelationalInput relationalInput, Column[] columns) {
        int i = 0;
        for (Column c : columns) {
            switch (c.getType()) {
                case LONG: {
                    ParsedColumn<Long> parsedColumn = new ParsedColumn<Long>(relationalInput.relationName(), c.getName(),
                            Long.class, i);

                    for (int l = 0; l < lineCount; ++l) {
                        parsedColumn.addLine(c.getLong(l));
                    }
                    parsedColumns.add(parsedColumn);
                }
                break;
                case NUMERIC: {
                    ParsedColumn<Double> parsedColumn = new ParsedColumn<Double>(relationalInput.relationName(),
                            c.getName(), Double.class, i);

                    for (int l = 0; l < lineCount; ++l) {
                        parsedColumn.addLine(c.getDouble(l));
                    }
                    parsedColumns.add(parsedColumn);
                }
                break;
                case STRING: {
                    ParsedColumn<String> parsedColumn = new ParsedColumn<String>(relationalInput.relationName(),
                            c.getName(), String.class, i);

                    for (int l = 0; l < lineCount; ++l) {
                        parsedColumn.addLine(c.getString(l));
                    }
                    parsedColumns.add(parsedColumn);
                }
                break;
                default:
                    break;
            }

            ++i;
        }
    }

    public int getLineCount() {
        return lineCount;
    }

    public ParsedColumn<?>[] getColumns() {
        return parsedColumns.toArray(new ParsedColumn[0]);
    }

    public String getName() {
        return name;
    }

    public Input(RelationalInput relationalInput) throws InputIterationException {
        this(relationalInput, -1);
    }

    public int[][] getInts() {
        final int COLUMN_COUNT = parsedColumns.size();
        final int ROW_COUNT = getLineCount();

        int[][] input2s = new int[ROW_COUNT][COLUMN_COUNT];
        IndexProvider<String> providerS = new IndexProvider<>();
        IndexProvider<Long> providerL = new IndexProvider<>();
        IndexProvider<Double> providerD = new IndexProvider<>();
        for (int col = 0; col < COLUMN_COUNT; ++col) {

            if (parsedColumns.get(col).getType() == String.class) {
                for (int line = 0; line < ROW_COUNT; ++line) {
                    input2s[line][col] = providerS.getIndex((String) parsedColumns.get(col).getValue(line)).intValue();
                }
            } else if (parsedColumns.get(col).getType() == Double.class) {
                for (int line = 0; line < ROW_COUNT; ++line) {
                    input2s[line][col] = providerD.getIndex((Double) parsedColumns.get(col).getValue(line)).intValue();

                }
            } else if (parsedColumns.get(col).getType() == Long.class) {
                for (int line = 0; line < ROW_COUNT; ++line) {
                    input2s[line][col] = providerL.getIndex((Long) parsedColumns.get(col).getValue(line)).intValue();
                }
            }
        }
        providerS = IndexProvider.getSorted(providerS);
        providerL = IndexProvider.getSorted(providerL);
        providerD = IndexProvider.getSorted(providerD);
        for (int col = 0; col < COLUMN_COUNT; ++col) {
            if (parsedColumns.get(col).getType() == String.class) {
                for (int line = 0; line < ROW_COUNT; ++line) {
                    input2s[line][col] = providerS.getIndex((String) parsedColumns.get(col).getValue(line)).intValue();
                }
            } else if (parsedColumns.get(col).getType() == Double.class) {
                for (int line = 0; line < ROW_COUNT; ++line) {
                    input2s[line][col] = providerD.getIndex((Double) parsedColumns.get(col).getValue(line)).intValue();

                }
            } else if (parsedColumns.get(col).getType() == Long.class) {
                for (int line = 0; line < ROW_COUNT; ++line) {
                    input2s[line][col] = providerL.getIndex((Long) parsedColumns.get(col).getValue(line)).intValue();
                }
            }
        }

        return input2s;
    }

    public void buildPLIs() {

        long time = System.currentTimeMillis();

        final int COLUMN_COUNT = parsedColumns.size();
        final int ROW_COUNT = getLineCount();

        List<Integer> tIDs = new TupleIDProvider(ROW_COUNT).gettIDs(); // to save integers storage

        int[][] inputs = getInts();

        for (int col = 0; col < COLUMN_COUNT; ++col) {

            TIntSet distincts = new TIntHashSet();
            for (int line = 0; line < ROW_COUNT; ++line) {
                distincts.add(inputs[line][col]);
            }

            int[] distinctsArray = distincts.toArray();

            // need to sort for doubles and integers
            if (!(parsedColumns.get(col).getType() == String.class)) {
                Arrays.sort(distinctsArray);// ascending is the default
            }

            TIntIntMap translator = new TIntIntHashMap();
            for (int position = 0; position < distinctsArray.length; position++) {
                translator.put(distinctsArray[position], position);
            }

            List<Set<Integer>> setPlis = new ArrayList<>();
            for (int i = 0; i < distinctsArray.length; i++) {
                setPlis.add(new TreeSet<Integer>());
            }

            for (int line = 0; line < ROW_COUNT; ++line) {
                Integer tid = tIDs.get(line);
                setPlis.get(translator.get(inputs[line][col])).add(tid);
            }

            int values[] = new int[ROW_COUNT];
            for (int line = 0; line < ROW_COUNT; ++line) {
                values[line] = inputs[line][col];
            }

            PLI pli;

            if (!(parsedColumns.get(col).getType() == String.class)) {
                pli = new PLI(setPlis, ROW_COUNT, true, values);
            } else {
                pli = new PLI(setPlis, ROW_COUNT, false, values);

            }

            parsedColumns.get(col).setPLI(pli);

        }

    }


}
