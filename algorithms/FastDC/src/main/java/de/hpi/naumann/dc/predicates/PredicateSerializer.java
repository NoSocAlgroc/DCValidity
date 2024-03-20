package de.hpi.naumann.dc.predicates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;

public class PredicateSerializer {
	
	public static PredicateBuilder readPredicateSpace(String path, Input input) {
		ObjectInputStream objectinputstream = null;
		
		try {
			log.info("Start reading predicates from " + path);
			Set<Predicate> predicates = new HashSet<Predicate>();
			FileInputStream streamIn = new FileInputStream(path);
			objectinputstream = new ObjectInputStream(streamIn);
			int predicateCount = objectinputstream.readInt();
			for (int i = 0; i < predicateCount; ++i) {
				Operator op = (Operator) objectinputstream.readObject();
				int i1 = objectinputstream.readInt();
				int ci1 = objectinputstream.readInt();
				int i2 = objectinputstream.readInt();
				int ci2 = objectinputstream.readInt();
				Predicate p = predicateProvider.getPredicate(op, new ColumnOperand<>(input.getColumns()[ci1], i1), new ColumnOperand<>(input.getColumns()[ci2], i2));
				if (PredicateBitSet.indexProvider.getIndex(p) != i)
					log.error("WRONG INDEX!!");
				predicates.add(p);
			}
			log.info("Finished reading predicates from " + path);
			return new PredicateBuilder(predicates);
		} catch(FileNotFoundException e) {
			log.error("No predicates found " + path);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed reading predicates from " + path);
		} finally {
			if (objectinputstream != null) {
				try {
					objectinputstream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	
	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();

	public static void savePredicateSpace(String path, PredicateBuilder predicates) {
		
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		File yourFile = null;
		try {
			yourFile = new File(path);
			yourFile.createNewFile(); // if file already exists will do nothing 
			fout = new FileOutputStream(yourFile, false);
			oos = new ObjectOutputStream(fout);
			int predCount = predicates.getPredicates().size();
			oos.writeInt(predCount);
			for (Predicate p : predicates.getPredicates()) {
				oos.writeObject(p.getOperator());
				if(p.getOperand1() instanceof ColumnOperand) {
					ColumnOperand<?> o1 = (ColumnOperand<?>) p.getOperand1();
					oos.writeInt(o1.getIndex());
					oos.writeInt(o1.getColumn().getIndex());
				}
				if(p.getOperand2() instanceof ColumnOperand) {
					ColumnOperand<?> o2 = (ColumnOperand<?>) p.getOperand2();
					oos.writeInt(o2.getIndex());
					oos.writeInt(o2.getColumn().getIndex());
				}
			}
			log.info("Finished writing predicates to " + path);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("Failed writing predicates to " + path);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	private static Logger log = LoggerFactory.getLogger(PredicateSerializer.class);
}
