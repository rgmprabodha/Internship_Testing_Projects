package shacl2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.ShaclSimpleValidator;

public class Validation {
	public static void main(String... args) throws FileNotFoundException {

		Path path = Paths.get(".").toAbsolutePath().normalize();

		// shape graph and data graph
		String DATA = path.toFile().getAbsolutePath() + "\\src\\main\\resources\\temperature.ttl";
		String SHAPES = path.toFile().getAbsolutePath() + "\\src\\main\\resources\\temperatureShape.ttl";

		// initiate data graph, shapes graph as Graphs
		Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
		Graph dataGraph = RDFDataMgr.loadGraph(DATA);

		// initiate shape graph as a shape
		Shapes shapes = Shapes.parse(shapesGraph);

		// validate
		ShaclSimpleValidator ssv = new ShaclSimpleValidator();
		ValidationReport report = ssv.validate(shapes, dataGraph);

//		ShLib.printReport(report);
//		RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);

		// write to the file
		String reportFile = path.toFile().getAbsolutePath() + "/src/main/resources/temperatureReport.ttl";
		report.getModel().write(new FileOutputStream(new File(reportFile)), "TURTLE");
		System.out.println("Done");
	}
}
