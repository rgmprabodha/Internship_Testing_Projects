package generateShape;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.emse.ci.sparqlext.SPARQLExt;
import fr.emse.ci.sparqlext.SPARQLExtException;
import fr.emse.ci.sparqlext.query.SPARQLExtQuery;
import fr.emse.ci.sparqlext.syntax.ElementGenerateTriplesBlock;
import fr.emse.ci.sparqlext.syntax.ElementSubExtQuery;

public class GenerateClassProfiles {

	static ArrayList<String> classesArray = new ArrayList<String>();
	static Property rdfType;
	static Map<String, String> class_subject = new HashMap<String, String>();
	static Map<String, String> class_map = new HashMap<String, String>();
	static JSONObject class_object = new JSONObject();
	static List<Triple> triplet_list;
	static ArrayList<ArrayList<String>> formattedTripleSet = new ArrayList<>();
	public static LinkedHashMap<String, String> variableMap = new LinkedHashMap<String, String>();
	public static LinkedHashMap<String, String> propertProfiles = new LinkedHashMap<String, String>();
	static JSONObject schemaClassProfilesFinal = new JSONObject();

	public static void main(String[] args) {
		variableMap.put("?room", "room[*]");
		variableMap.put("?sensor", "room[*].sensor[*]");
		variableMap.put("?plant", "room[*].plants[*]");
		variableMap.put("?roomId", "room[*].id");
		variableMap.put("?sensorId", "room[*].sensor[*].id");
		variableMap.put("?category", "room[*].sensor[*].category");
		variableMap.put("?temperature", "room[*].sensor[*].temperature");
		variableMap.put("?humidity", "room[*].sensor[*].humidity");
		variableMap.put("?expire", "room[*].sensor[*].expire");

		propertProfiles.put("room[*]", "profile_room[*]");
		propertProfiles.put("room[*].sensor[*]", "profile_room[*]_sensor[*]");
		propertProfiles.put("room[*].plants[*]", "profile_room[*]_plants[*]");
		propertProfiles.put("room[*].id", "profile_room[*]_id");		
		propertProfiles.put("room[*].sensor[*].id", "profile_room[*]_sensor[*]_id");
		propertProfiles.put("room[*].sensor[*].category", "profile_room[*]_sensor[*]_category");
		propertProfiles.put("room[*].sensor[*].temperature", "profile_room[*]_sensor[*]_temperature");
		propertProfiles.put("room[*].sensor[*].humidity", "profile_room[*]_sensor[*]_humidity");		
		propertProfiles.put("room[*].sensor[*].expire", "profile_room[*]_sensor[*]_expire");
		

		Path path = Paths.get(".").toAbsolutePath().normalize();

		Model model = ModelFactory.createDefaultModel();
		String rdfsNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		model.setNsPrefix("rdfs", rdfsNS);
		rdfType = model.createProperty(rdfsNS + "type");
		String queryPath = path.toFile().getAbsolutePath() + "/src/main/resources/query.rqg";
		String schemaClassProfilesPath = path.toFile().getAbsolutePath()
				+ "/src/main/resources/schemaClassProfiles.json";

		try {
			String queryString = IOUtils.toString(new FileInputStream(queryPath), StandardCharsets.UTF_8);
			try {
				SPARQLExtQuery query = (SPARQLExtQuery) QueryFactory.create(queryString, SPARQLExt.SYNTAX);

				getClasses(query);
				getStatements(query);
//				System.out.println(class_subject);
				fillClassObject();
//				schemaClassProfilesFinal = makeProfiles();
//				System.out.println(schemaClassProfilesFinal);

				try {
					FileWriter fileWriter = new FileWriter(schemaClassProfilesPath);
					fileWriter.write(class_object.toString(4));
					fileWriter.flush();

				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
			}
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}

	}

	public static void getClasses(SPARQLExtQuery query) {
		// taken from QueryBNodeNormalizer class 115 line,
		for (int i = 0; i < query.getGenerateClause().size(); i++) {
			Element elem = query.getGenerateClause().get(i);
			if (elem instanceof ElementGenerateTriplesBlock) {
				for (Iterator<Triple> it = ((ElementGenerateTriplesBlock) elem).patternElts(); it.hasNext();) {
					Triple t = it.next();
					if (t.getPredicate().toString().equals(rdfType.toString())) {
						classesArray.add(t.getObject().toString());
					}
				}
			}
		}
	}

	public static void getStatements(SPARQLExtQuery query) {

		final BasicPattern bgp = new BasicPattern();
		for (Element elem : query.getGenerateClause()) {
			if (elem instanceof ElementGenerateTriplesBlock) {
				ElementGenerateTriplesBlock sub = (ElementGenerateTriplesBlock) elem;

				List a = sub.getPattern().getList();
				bgp.addAll(sub.getPattern());
			} else if (elem instanceof ElementSubExtQuery) {
				ElementSubExtQuery sub = (ElementSubExtQuery) elem;
			} else {
				throw new SPARQLExtException("should not reach this point");
			}
		}

		triplet_list = bgp.getList();

		for (Triple triple : triplet_list) {
			String flag = "no";
			ArrayList<String> formattedTriple = new ArrayList<String>();
			String s = triple.getSubject().toString();
			String o = triple.getObject().toString();

			String processedElementTag = null;
			if (s.contains(":=")) {
				s = processElement(s);
			}

			if (o.contains(":=")) {
				o = processElement(o);
				flag = "yes";
			}

			formattedTriple.add(s);
			formattedTriple.add(triple.getPredicate().toString());
			formattedTriple.add(o);
			formattedTriple.add(flag);
			formattedTripleSet.add(formattedTriple);
		}
//		System.out.println(triplet_list);
		System.out.println("_: "+formattedTripleSet);

		// meka wenas wenna ona
//		for (Triple triple : triplet_list) {
//			if ((rdfType.toString()).equals(triple.getPredicate().toString())) {
//				class_subject.put(triple.getObject().toString(), triple.getSubject().toString());
//			}
//		}
//		
		for (ArrayList<String> quardaple : formattedTripleSet) {
			if (quardaple.get(1).equals(rdfType.toString())) {
				class_subject.put(quardaple.get(2), quardaple.get(0));
				class_map.put(quardaple.get(0), quardaple.get(2));
			}
		}
		System.out.println("-------------"+class_subject);
	}

	public static String processElement(String eachElement) {
		String processedElementTag = null;
		ArrayList<String> processedElements = new ArrayList<>();
		eachElement = eachElement.split("\\{")[1];
		eachElement = eachElement.replace("}>", "").trim();
		return eachElement;
	}

	public static void fillClassObject() {
		for (String sClass : class_subject.keySet()) {
			JSONObject classJSON = new JSONObject();
			String sSubject = class_subject.get(sClass);
//			for (Triple triple : triplet_list) {
//				if ((triple.getSubject().toString()).equals(sSubject)) {
//					classJSON.put(triple.getPredicate().toString(), triple.getObject().toString());
//				}
//			}
			JSONArray ja = new JSONArray();
			for (ArrayList<String> quardaple : formattedTripleSet) {
				System.out.println(quardaple);
				JSONObject line = new JSONObject();
				if(!quardaple.get(1).equals(rdfType.toString())) {
					if (quardaple.get(0).equals(sSubject)) {
						line.put("sh_path", quardaple.get(1));
						line.put("js_var", quardaple.get(2));
						if(quardaple.get(3).equals("no")) {
							line.put("nodeKind", "literal");
						}
						else if(quardaple.get(3).equals("yes")) {
							line.put("nodeKind", "nonLiteral");
							line.put("class", class_map.get(quardaple.get(2)));
						}
						line.put("js_path", variableMap.get(quardaple.get(2)));
						line.put("js_profile", propertProfiles.get(variableMap.get(quardaple.get(2))));
						classJSON.put(quardaple.get(1), line);
						ja.put(line);
					}
				}
			}
			System.out.println(ja);
			class_object.put(sClass, ja);
		}
		System.out.println(class_object.toString(4));
	}

	public static JSONObject makeProfiles() {
		JSONObject fin = new JSONObject();
		JSONObject pp = new JSONObject();
		for (String sClass : class_object.keySet()) {
			JSONObject prof = new JSONObject();
			ArrayList<String> gfg = new ArrayList<String>();

			JSONObject pop = new JSONObject();
			JSONObject obj = class_object.getJSONObject(sClass);
			for (String sPropertyIRI : obj.keySet()) {
				String path = "";
				String propertyPath = "";
				JSONObject ob = obj.getJSONObject(sPropertyIRI);
				String var = ob.get("js_var").toString();
				if (sPropertyIRI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					pop.put("target", var);
				} else {
//					if ((ob.get("class").toString()).equals("no")) {
//						path = variableMap.get(var);
//					} else if ((ob.get("class").toString()).equals("yes")) {
//						String start = variableMap.get(var); // remove the part after last dot
//						List<String> Arraysplit = Arrays.asList(start.split("\\."));
//											
//						path = path+Arraysplit.get(0).toString();
//						for(int i=1; i<Arraysplit.size()-1; i++) {
//							path = path+"."+Arraysplit.get(i).toString();
//						}		
//					}
//					propertyPath = propertProfiles.get(path);

					gfg.add(propertyPath);
				}
			}
			pop.put("properties", gfg);
			pp.put(sClass, pop);
			fin.put(sClass, pop);
		}
		return fin;
	}
}
