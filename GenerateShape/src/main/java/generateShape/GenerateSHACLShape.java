package generateShape;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GenerateSHACLShape {

	static Map<String, Resource> class_shape_map = new HashMap<String, Resource>();
	static JSONObject jsonPropertyProfiles = new JSONObject();
	static JSONObject jsonClassProfiles = new JSONObject();
	static Model shapesModel;

	public static void main(String[] args) {
		Path path = Paths.get(".").toAbsolutePath().normalize();
		String classProfilesPath = path.toFile().getAbsolutePath() + "/src/main/resources/schemaClassProfiles.json";
		String propertyProfilesPath = path.toFile().getAbsolutePath()
				+ "/src/main/resources/schemaPropertyProfilesUpdated.json";

		shapesModel = ModelFactory.createDefaultModel();
		String rdfsNS = "http://www.w3.org/2000/01/rdf-schema/";
		String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		String shNS = "http://www.w3.org/ns/shacl#";
		String xsdNS = "http://www.w3.org/2001/XMLSchema#";
		String shgenNS = "http://generated-shacl.io/";

		shapesModel.setNsPrefix("rdfs", rdfsNS);
		shapesModel.setNsPrefix("rdf", rdfNS);
		shapesModel.setNsPrefix("sh", shNS);
		shapesModel.setNsPrefix("xsd", xsdNS);
		shapesModel.setNsPrefix("shgen", shgenNS);


		try {
			String cProfilesPath = new String(Files.readAllBytes(Paths.get(classProfilesPath)));
			jsonClassProfiles = new JSONObject(new JSONTokener(cProfilesPath));
			String pProfilesPath = new String(Files.readAllBytes(Paths.get(propertyProfilesPath)));
			jsonPropertyProfiles = new JSONObject(new JSONTokener(pProfilesPath));

			for (Iterator iterator = jsonClassProfiles.keySet().iterator(); iterator.hasNext();) {
				String classIRI = (String) iterator.next();
				JSONArray classPoperties = (JSONArray) jsonClassProfiles.get(classIRI);
				
				Resource s = processShape(classIRI, classPoperties);
				
			}
			shapesModel.write(System.out, "turtle");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// model eka static thiyagena
	public static Resource processShape(String classIRI, JSONArray classPoperties) {
		
		Model shapeModel = ModelFactory.createDefaultModel();
		String rdfsNS = "http://www.w3.org/2000/01/rdf-schema/";
		String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		String shNS = "http://www.w3.org/ns/shacl#";
		String xsdNS = "http://www.w3.org/2001/XMLSchema#";
		String shgenNS = "http://generated-shacl.io/";
		String exNS = "http://example.com/";

		shapeModel.setNsPrefix("rdfs", rdfsNS);
		shapeModel.setNsPrefix("rdf", rdfNS);
		shapeModel.setNsPrefix("sh", shNS);
		shapeModel.setNsPrefix("xsd", xsdNS);
		shapeModel.setNsPrefix("shgen", shgenNS);
		shapeModel.setNsPrefix("ex", exNS);

		Property pTargetClass = shapeModel.createProperty(shNS + "targetClass");
		Property pPath = shapeModel.createProperty(shNS + "path");
		Property pProperty = shapeModel.createProperty(shNS + "property");
		Property pMinCount = shapeModel.createProperty(shNS + "minCount");
		Property pmaxCount = shapeModel.createProperty(shNS + "maxCount");
		Property pminInclusive = shapeModel.createProperty(shNS + "minInclusive");
		Property pminExclusive = shapeModel.createProperty(shNS + "minExclusive");
		Property pmaxInclusive = shapeModel.createProperty(shNS + "maxInclusive");
		Property pmaxExclusive = shapeModel.createProperty(shNS + "maxExclusive");
		Property pDataType = shapeModel.createProperty(shNS + "dataType");
		Property pNode = shapeModel.createProperty(shNS + "node");
		Property pNodeKind = shapeModel.createProperty(shNS + "nodeKind");

		Map<String, Property> property_map = new HashMap<String, Property>();
		property_map.put("minCount", pMinCount);
		property_map.put("maxCount", pmaxCount);
		property_map.put("minInclusive", pminInclusive);
		property_map.put("minExclusive", pminExclusive);
		property_map.put("maxInclusive", pmaxInclusive);
		property_map.put("maxExclusive", pmaxExclusive);
		property_map.put("dataType", pDataType);
		property_map.put("node", pNode);
		property_map.put("nodeKind", pNodeKind);
		
		// check this classIRI is already processed and have a shape
		if (class_shape_map.containsKey(classIRI)) {
			return class_shape_map.get(classIRI);
		}
		String[] paths = classIRI.split("/");
		String shapeName = paths[paths.length-1];
		Resource itemShape = shapeModel.createResource(shgenNS + shapeName + "Shape");
		Resource nodeShape = shapeModel.createResource(shNS + "Nodeshape");
		Resource targetClass = shapeModel.createResource(classIRI);
		itemShape.addProperty(RDF.type, nodeShape);
		itemShape.addProperty(pTargetClass, targetClass);
		
		for (int i = 0; i < classPoperties.length(); i++) {
			JSONObject obj = classPoperties.getJSONObject(i);
			Resource bProperty = shapeModel.createResource();
			Resource shPath = shapeModel.createResource(obj.getString("sh_path"));
			
			bProperty.addProperty(pPath, shPath);
			itemShape.addProperty(pProperty, bProperty);
			
			JSONObject objj2 = jsonPropertyProfiles.getJSONObject(obj.get("js_profile").toString());
			JSONObject cons = objj2.getJSONObject("constraints");
			Iterator<String> keys = cons.keys();

			// property array ekak thiyagena eken check karanna 
			while(keys.hasNext()) {
			    String key = keys.next();
			    String val = cons.get(key).toString();
			    if(property_map.containsKey(key)) {
			    	Property p = property_map.get(key);
			    	bProperty.addProperty(p, val);
			    }
			}

			if((obj.get("nodeKind").toString()).equals("nonLiteral")) {
				String nextClass = obj.getString("class");
				Resource rs = processShape(nextClass, jsonClassProfiles.getJSONArray(nextClass));

				bProperty.addProperty(pNode, rs);
				bProperty.addProperty(pNodeKind, "IRI");
			}
		}
		

		shapesModel.add(shapeModel);
//		shapesModel = shapesModel.union(shapeModel);
		
//		and also shape ekath return karanna onada?
		class_shape_map.put(classIRI, itemShape);
		return itemShape; //return karanna ona generate wena shape eke iri eka
	}
}
