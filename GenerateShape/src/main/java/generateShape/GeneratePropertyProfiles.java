package generateShape;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.beust.jcommander.defaultprovider.PropertyFileDefaultProvider;
import com.jayway.jsonpath.JsonPath;

public class GeneratePropertyProfiles {

	// For properties cardinality is always 1, json document can not have same key
	// what about array of strings
	static JSONObject schemaClassPropertyProfiles = new JSONObject();
	static JSONObject schemaPropertyProfilesFinal = new JSONObject();
	static JSONObject schemaClassPropertyProfilesFinal = new JSONObject();
	static Map<String, String> constraints_map = new HashMap<String, String>();

	static ArrayList<String> jsonPathsFromQuery = new ArrayList<>(

			Arrays.asList("children[*].otherProperty", "children[*].name", "children[*]", "children[*].otherProperty2",
					"children[*].otherProperty3"));

	static ArrayList<String> PROPERTY_PROPERTIES = new ArrayList<>(Arrays.asList("type", "minimum", "maximum",
			"exclusiveMinimum", "exclusiveMaximum", "minItems", "maxItems", "format"));

	public static void main(String args[]) {

//		constraints_map.put("type", "literal_data_type");
		constraints_map.put("minimum", "numeric_minimum");
		constraints_map.put("maximum", "numeric_maximum");
		constraints_map.put("exclusiveMinimum", "numeric_exclusive_minimum");
		constraints_map.put("exclusiveMaximum", "numeric_exclusive_maximum");
		constraints_map.put("minItems", "array_min_value_nodes");
		constraints_map.put("maxItems", "array_max_value_nodes");
		constraints_map.put("format", "string_format");
		constraints_map.put("pattern", "string_min_length");
		constraints_map.put("minLength", "string_max_length");
		constraints_map.put("maxLength", "string_pattern");
		constraints_map.put("enum", "literal_enum");
		constraints_map.put("const", "literal_const");

		JSONObject schemaPropertyProfiles = new JSONObject();
		Path path = Paths.get(".").toAbsolutePath().normalize();

		String jsonSchema;
		String schemaPath = path.toFile().getAbsolutePath() + "/src/main/resources/combine/array/combine-schema.json";
		String schemaPropertyProfilesPath = path.toFile().getAbsolutePath()
				+ "/src/main/resources/combine/array/schemaPropertyProfiles.json";
		try {
			jsonSchema = new String(Files.readAllBytes(Paths.get(schemaPath)));
			JSONObject jsonObjectSchema = new JSONObject(new JSONTokener(jsonSchema));

			String type = JsonPath.read(jsonSchema, "$.type");
			if (type.equals("object")) {
				generateShapesForProperties(jsonObjectSchema, "", schemaPropertyProfiles);
			}

			if (schemaPropertyProfiles.length() > 0) {
				Iterator<String> keys = schemaPropertyProfiles.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (schemaPropertyProfiles.get(key) instanceof JSONObject) {
						JSONObject xx = new JSONObject(schemaPropertyProfiles.get(key).toString());
						processToPropertyProfile(key, xx);
					}
				}
			}

			try {
				FileWriter fileWriter = new FileWriter(schemaPropertyProfilesPath);
				fileWriter.write(schemaPropertyProfilesFinal.toString(4));
				fileWriter.flush();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateShapesForProperties(JSONObject jsonSchema, String jPath,
			JSONObject schemaPropertyProfiles) {

		JSONObject properties = (JSONObject) jsonSchema.get("properties");
		Iterator<String> propertyKeys = properties.keys();

		while (propertyKeys.hasNext()) {
			String key = propertyKeys.next().toString();
			JSONObject obj = (JSONObject) properties.get(key);

			String type = (String) obj.get("type");
			if (type.equals("object")) {
				String path = jPath + key + ".";

//				schemaPropertyProfiles.put(jPath, jsonSchema);////////// TODO
				generateShapesForProperties(obj, path, schemaPropertyProfiles);
			} else if (type.equals("array")) {

				String path = jPath + key + "[*].";

				schemaPropertyProfiles.put(path, obj);//////////

				JSONObject itemsObject = (JSONObject) obj.get("items");

				if (itemsObject.has("anyOf")) {
					int j = 0;
					JSONArray anyOfArray = (JSONArray) itemsObject.get("anyOf");
					if (anyOfArray.length() > 0) {
						for (Object object : anyOfArray) {
							j++;
							String newPath = path + "_anyOf_" + j + "_.";
							generateShapesForProperties((JSONObject) object, newPath, schemaPropertyProfiles);
						}
					}
				}
//				if(itemsObject.has("allOf")) {
//					JSONArray allOfArray = (JSONArray) itemsObject.get("allOf");
//				}
//				if(itemsObject.has("oneOf")) {
//					JSONArray oneOfArray = (JSONArray) itemsObject.get("OneOf");
//				}
				if (itemsObject.has("type")) {
					String type2 = (String) itemsObject.get("type");
					if (type2.equals("object")) {
						generateShapesForProperties(itemsObject, path, schemaPropertyProfiles);
					}
				}

			} else {
				String path = jPath + key;
				System.out.println("path: " + path);
//				if (jsonPathsFromQuery.contains(path)) {
					schemaPropertyProfiles.put(path, obj);
//				}
			}
		}
	}

	public static void processToPropertyProfile(String path, JSONObject obj) {

		System.out.println("--------------");
		System.out.println(path + " : " + obj);
		System.out.println("--------------");
		
		JSONObject prop = new JSONObject();
		
		String[] pathParts = path.split("\\.");
		String profName = "profile_" + pathParts[0];
		String anyOfStr;
		int anyOfInt = 0;
		for (int i = 1; i < pathParts.length; i++) {
			profName = profName + "_" + pathParts[i];
			if(pathParts[i].contains("anyOf")) {
				anyOfStr = pathParts[i];
				anyOfInt = i;
				prop.put("any_of", anyOfStr);
			}
		}
		
		
		prop.put("js_path", path);

		Iterator<String> keys = obj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (!(obj.get("type").equals("array") || obj.get("type").equals("object"))) {
				// TODO ei me if ekata object ekath check karanne? array eka witarak madida?
//				prop.put("array_min_value_nodes", 1);
//				prop.put("array_max_value_nodes", 1);
			}

			if (constraints_map.containsKey(key)) {
				prop.put(constraints_map.get(key), obj.get(key));
			}

			if (obj.get("type").equals("array")) {
				JSONObject items = obj.getJSONObject("items");
				if(items.has("type")) {
					if (!(items.get("type").equals("array") || items.get("type").equals("object"))) {
						prop.put("value_node_kind", "literal");
						prop.put("literal_data_type", items.get("type"));
					} else {
						prop.put("value_node_kind", "non_literal");
					}					
				}else if(items.has("anyOf")) {
					prop.put("value_node_kind", "non_literal"); // TODO 
				}
			} else if (key.equals("type")) {
				prop.put("value_node_kind", "literal");
				prop.put("literal_data_type", obj.get("type"));
			}
		}
		schemaPropertyProfilesFinal.put(profName, prop);
	}

}
