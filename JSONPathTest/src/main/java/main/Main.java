package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class Main {
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String args[]) {

		String json;

		Path path = Paths.get(".").toAbsolutePath().normalize();
		String jsonDataFile = path.toFile().getAbsolutePath() + "\\src\\main\\java\\main\\floor-data.json";		
		
		try {
			json = new String(Files.readAllBytes(Paths.get(jsonDataFile)));
			
			// Static read API, have to read each time
			
			// Get Ids of all the sensors in the room
//			List<String> sensors = JsonPath.read(json, "$.room.sensor[*].id");
//			System.out.println(sensors);
//
//			// Get all the room objects
//			List<String> items = JsonPath.read(json, "$.room.*");
//			String roomId = JsonPath.read(json, "$.room.id");
//			System.out.println("roomId: "+roomId);
//
//			// Get the 3rd sensor
//			Object sensor3 = JsonPath.read(json, "$..sensor[3]");
//			System.out.println(sensor3);
//
//			// Get the sensors between 1 and 3
//			List<String> sensors1and2 = JsonPath.read(json, "$..sensor[1:3]");
//			System.out.println(sensors1and2);
//
//			// Get the sensors which has expire
//			List<String> sensorsexpire = JsonPath.read(json, "$..sensor[?(@.expire)]");
//			System.out.println(sensorsexpire);
//
//			// Get the number of sensors in the room 
//			List<String> numOfSensors = JsonPath.read(json, "$..sensor.length()");
//			System.out.println(numOfSensors);
//						
//			// Parse to object
//			Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
//			
//			// Get the Id of first sensor
//			String sensor1Id = JsonPath.read(document, "room.sensor[0].id");
//			System.out.println(sensor1Id);
//			
//			// Get the last two sensors
//			List<String> lastSensors  = JsonPath.read(document, "$..sensor[:2]");
//			System.out.println(lastSensors);
//			
//			
//			List<Integer> hum = JsonPath.read(document,"room.sensor[*].humadity");
//			
//			
//			// Fluent API
//			ReadContext ctx = JsonPath.parse(json);
//			
//			// Get the id of 2nd sensor
//			String sensor2Id  = ctx.read("room.sensor[1].id");
//			System.out.println(sensor2Id);
//
//			// Get all the lights
//			List<Object> lights  = ctx.read("room.light[*]");
//			System.out.println(lights);
			
			JSONArray users = JsonPath.read(json, "$.[*]");
			System.out.println(users);


			List<String> aa = JsonPath.read(json, "$.[*].sensorbases[*].id");
			System.out.println(aa);
//			JSONObject user0 = JsonPath.read(json, "$.[0]");
//			System.out.println(user0);
			
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
