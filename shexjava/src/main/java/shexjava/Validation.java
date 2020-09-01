package shexjava;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.jena.util.FileManager;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import fr.inria.lille.shexjava.GlobalFactory;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import fr.inria.lille.shexjava.shapeMap.BaseShapeMap;
import fr.inria.lille.shexjava.shapeMap.ResultShapeMap;
import fr.inria.lille.shexjava.shapeMap.parsing.ShapeMapParsing;
import fr.inria.lille.shexjava.validation.RecursiveValidationWithMemorization;
import fr.inria.lille.shexjava.validation.RefineValidation;
import fr.inria.lille.shexjava.validation.ValidationAlgorithm;

public class Validation {

	public static void main(String[] args) throws Exception {

		Path path = Paths.get(".").toAbsolutePath().normalize();
		ShapeMapParsing parser = new ShapeMapParsing();

		// Data file
		String DATA = path.toFile().getAbsolutePath() + "/src/main/resources/temperature.ttl";
		
		// Shape file
		Path SHAPE = Paths.get("./src/main/resources/shape.json").toAbsolutePath().normalize();

		RDF4J factory = new RDF4J();
		GlobalFactory.RDFFactory = factory; // set the global factory used in shexjava

		// load and create the shex schema
		ShexSchema schema = GenParser.parseSchema(SHAPE);

		// load the data to model
		String baseIRI = "http://a.example.shex/";
		Model data = Rio.parse(FileManager.get().open(DATA), baseIRI, RDFFormat.TURTLE);
		
		// create the data graph
		Graph dataGraph = factory.asGraph(data);

		// focus node and shape label to be validated
//		IRI focusNode = factory.createIRI("http://example.com/ns#Sensor1");
//		Label shapeLabel = new Label(factory.createIRI("http://example.com/ns#TemperatureShape"));
		
		 // load the shape map
        String shMap = "{FOCUS a <http://example.com/ns#Sensor>}@<http://example.com/ns#TemperatureShape>";

        try {
            BaseShapeMap shapeMap = parser.parse(new ByteArrayInputStream(shMap.getBytes()));
//            System.out.println(shapeMap);
            ValidationAlgorithm vl = new RefineValidation(schema, dataGraph);			
			ResultShapeMap resultaa = vl.validate(shapeMap);
            System.out.println(resultaa);
            
            RecursiveValidationWithMemorization algo = new RecursiveValidationWithMemorization(schema, dataGraph);
            ResultShapeMap result = algo.validate(shapeMap);
            System.out.println(result.getAssociations().toString());
//            System.out.println(result);
            
            String reportFile = path.toFile().getAbsolutePath() + "/src/main/resources/temperatureReport.ttl";

    		FileWriter myWriter = new FileWriter(reportFile);
    		myWriter.write(result.toString());
    		myWriter.close();

        } catch ( Exception e) {
            e.printStackTrace();
        }

		// create the validation algorithm
//		ValidationAlgorithm validation = new RefineValidation(schema, dataGraph);		

		// validate
//		validation.validate(shapeMap);
//		System.out.println(validation.getTyping());

		
		System.out.println("Done");

		// check the result
//		boolean result = validation.getTyping().isConformant(focusNode, shapeLabel);
//		System.out.println("Does " + focusNode + " has shape " + shapeLabel + "? " + result);

	}
}
