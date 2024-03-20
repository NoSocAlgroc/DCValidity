package de.hpi.naumann.dc.evidenceset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import de.hpi.naumann.dc.helpers.BitSetTranslator;
import de.hpi.naumann.dc.predicates.Operator;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class EvidenceSetSerializer {
	public static IEvidenceSet readEvidenceSet(String path, PredicateBuilder predicates) {
		ObjectInputStream objectinputstream = null;
		IEvidenceSet evidence = new TroveEvidenceSet();
		try {
			FileInputStream streamIn = new FileInputStream(path);
			objectinputstream = new ObjectInputStream(streamIn);
			int predicateCount = objectinputstream.readInt();
			Integer indexes[] = new Integer[predicateCount];
			for (int i = 0; i < predicateCount; ++i) {
				Operator op = (Operator) objectinputstream.readObject();
				String op1 = objectinputstream.readUTF();
				String op2 = objectinputstream.readUTF();
				Predicate p = null;
				for (Predicate pI : predicates.getPredicates()) {
					if (pI.getOperator().equals(op) && pI.getOperand1().toString().equals(op1.toString())
							&& pI.getOperand2().toString().equals(op2.toString())) {
						p = pI;
						break;
					}
				}
				indexes[i] = PredicateBitSet.indexProvider.getIndex(p);
			}
			BitSetTranslator translator = new BitSetTranslator(indexes);
			int evidenceSetSize = objectinputstream.readInt();
			for (int i = 0; i < evidenceSetSize; ++i) {
				IBitSet bitset = (IBitSet) objectinputstream.readObject();
				long count = objectinputstream.readLong();
				evidence.add(PredicateSetFactory.create(translator.bitsetRetransform(bitset)), count);
			}
			return evidence;
		} catch (FileNotFoundException e) {
			log.error("No evidence set found " + path);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed reading evidence from " + path);
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

	public static IEvidenceSet readOldEvidenceSet(String path, PredicateBuilder predicates) {
		ObjectInputStream objectinputstream = null;
		IEvidenceSet evidence = new TroveEvidenceSet();
		try {
			FileInputStream streamIn = new FileInputStream(path);
			objectinputstream = new ObjectInputStream(streamIn);
			int predicateCount = objectinputstream.readInt();
			Integer indexes[] = new Integer[predicateCount];
			for (int i = 0; i < predicateCount; ++i) {
				Operator op = (Operator) objectinputstream.readObject();
				String op1 = objectinputstream.readUTF();
				String op2 = objectinputstream.readUTF();
				Predicate p = null;
				for (Predicate pI : predicates.getPredicates()) {
					if (pI.getOperator().equals(op) && pI.getOperand1().toString().equals(op1.toString())
							&& pI.getOperand2().toString().equals(op2.toString())) {
						p = pI;
						break;
					}
				}
				indexes[i] = PredicateBitSet.indexProvider.getIndex(p);
			}
			BitSetTranslator translator = new BitSetTranslator(indexes);
			int evidenceSetSize = objectinputstream.readInt();
			for (int i = 0; i < evidenceSetSize; ++i) {
				IBitSet bitset = (IBitSet) objectinputstream.readObject();
				int count = objectinputstream.readInt();
				evidence.add(PredicateSetFactory.create(translator.bitsetRetransform(bitset)), count);
			}
			return evidence;
		} catch (FileNotFoundException e) {
			log.error("No evidence set found " + path);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed reading evidence from " + path);
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

	public static void saveEvidenceSet(String path, IEvidenceSet evidenceSet2, PredicateBuilder predicates) {
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
			for (int i = 0; i < predCount; ++i) {
				Predicate p = PredicateBitSet.indexProvider.getObject(i);
				oos.writeObject(p.getOperator());
				oos.writeUTF(p.getOperand1().toString());
				oos.writeUTF(p.getOperand2().toString());
			}
			oos.writeInt(evidenceSet2.size());
			for (IPredicateSetBinary pset : evidenceSet2) {
				oos.writeObject(pset.getBitset());
				oos.writeLong(evidenceSet2.getCount(pset));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed writing evidence to " + path);
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

	private static Logger log = LoggerFactory.getLogger(EvidenceSetSerializer.class);
}
