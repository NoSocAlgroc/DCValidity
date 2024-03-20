package de.hpi.naumann.dc.experiments.reports;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.algorithms.FastDCMetanome;

public class Reporter {

	public static String reportName;
	public static String reportDataset;
	public static VelocityContext context;

	public static void initialize(String dataset) {
		if (context != null) {
			return;
		}
		Velocity.init();
		context = new VelocityContext();
		setReportInfo(dataset);
	}

	private static void setReportInfo(String dataset) {
		Reporter.reportDataset=dataset;
		String date = new SimpleDateFormat("dd_MM_yyyy'_'HH:mm:ss", Locale.ENGLISH).format(new Date()).toString();
		Reporter.reportName = dataset.concat("_" + date);
	}

	public static void generateReport() {

		Template t = Velocity.getTemplate("/src/main/resources/templates/features.vm");
		context.put("reportID", reportName);

		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		String reportFileName = "/home/eduardo/doutorado_code/hpi/datasets/" + reportDataset+"/" + reportName+".csv"  ;


		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(reportFileName);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.print(writer.toString());
			printWriter.close();
			
			log.info("Execution report:" + reportFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static Logger log = LoggerFactory.getLogger(FastDCMetanome.class);

}
