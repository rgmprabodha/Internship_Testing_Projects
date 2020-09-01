import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.fasterxml.jackson.databind.introspect.Annotated;

public class GenerateShape {

	@SuppressWarnings("unchecked")
	public static void main(String args[]) {

		// TODO Identify classes by reading graph, rdf:type 
		HashMap<String, String> modelClasses = new HashMap<String, String>();
		modelClasses.put("FeatureOfInterestShape", "http://www.w3.org/ns/sosa/FeatureOfInterest");
		modelClasses.put("AirQualityIndexObservationShape",
				"https://ci.mines-stetienne.fr/aqi/ontology#AirQualityIndexObservation");
		modelClasses.put("AirQualityIndexPropertyShape",
				"https://ci.mines-stetienne.fr/aqi/ontology#AirQualityIndexProperty");
		modelClasses.put("PM25ParticulatesPropertyShape", "https://ci.mines-stetienne.fr/aqi/ontology#PM25ParticulatesProperty");
		modelClasses.put("TemperatureObservationShape", "https://ci.mines-stetienne.fr/aqi/ontology#TemperatureObservation");
		modelClasses.put("sosaObservationShape","https://ci.mines-stetienne.fr/aqi/ontology#sosaObservation");
		modelClasses.put("NitrogenDioxideObservationShape", "https://ci.mines-stetienne.fr/aqi/ontology#NitrogenDioxideObservation");
		modelClasses.put("OzonePropertyShape", "https://ci.mines-stetienne.fr/aqi/ontology#OzoneProperty");
		modelClasses.put("SulfurDioxideObservationShape", "https://ci.mines-stetienne.fr/aqi/ontology#SulfurDioxideObservation");
		modelClasses.put("NitrogenDioxidePropertyShape", "https://ci.mines-stetienne.fr/aqi/ontology#NitrogenDioxideProperty");
		modelClasses.put("HumidityObservation", "https://ci.mines-stetienne.fr/aqi/ontology#HumidityObservation");

		Map<String, List<Resource>> subject_list = new TreeMap<String, List<Resource>>();
		Map<String, List<Statement>> statement_list = new TreeMap<String, List<Statement>>();

		Path path = Paths.get(".").toAbsolutePath().normalize();
		String dataPath = path.toFile().getAbsolutePath() + "/src/main/resources/data.ttl";
		Model model = ModelFactory.createDefaultModel();
		model.read(dataPath);

		Property rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

//		Map<String, ResIterator> sublist = new HashMap<String, ResIterator>();
//		for (Map.Entry me : modelClasses.entrySet()) {
//			Property currentProperty = model.createProperty((String) me.getValue());
//			ResIterator a = model.listSubjectsWithProperty(rdfType, currentProperty);
//			sublist.put((String) me.getKey(), a);
//		}
//		System.out.println(sublist);


		Map<Integer, Object> map = new HashMap<Integer, Object>();
		HashMap<Integer, String> hmap = new HashMap<Integer, String>();
		Statement statement;

		ArrayList<Statement> statements = new ArrayList<Statement>();
		StmtIterator iterator = model.listStatements();
		while (iterator.hasNext()) {
			statement = iterator.next();
			statements.add(statement);
		}

		for (Map.Entry me : modelClasses.entrySet()) {
			// TODO eka class ekakata statement eka wetuna nam, eka list eken remove kala
			// nam hari
			String currentClass = (String) me.getValue();

			List<Resource> subjects = new ArrayList<Resource>();
			String key = (String) me.getKey();
			for (Statement statement1 : statements) {
				Property p = statement1.getPredicate();
				Resource s = statement1.getSubject();
				RDFNode o = statement1.getObject();
				if (currentClass.toString().equals(o.toString()) && p.toString().equals(rdfType.toString())) {
					subjects.add(s);
				}
			}
			subject_list.put(key, subjects);
		}


		for (Map.Entry se : subject_list.entrySet()) {
			String shape = (String) se.getKey();
			List<Resource> subjects = new ArrayList<Resource>();
			String key = (String) se.getKey();
			subjects = (List<Resource>) se.getValue();

			List<Statement> statementsnew = new ArrayList<Statement>();
			for (Resource sub : subjects) {
				for (Statement statement1 : statements) {

					Property p = statement1.getPredicate();
					Resource s = statement1.getSubject();
					RDFNode o = statement1.getObject();
					if ((sub.toString().equals(s.toString()))) {
						// TODO do not take rdf:rype statement
						statementsnew.add(statement1);
						
					}
				}
			}
			statement_list.put(key, statementsnew);
		}
		
		System.out.println("fini");
		System.out.println(statement_list);
	}
}
