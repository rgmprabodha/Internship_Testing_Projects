import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Validation {

	public static void main(String args[]) throws FileNotFoundException, IOException {
		Path path = Paths.get(".").toAbsolutePath().normalize();

		String schemaPath = path.toFile().getAbsolutePath() + "/src/main/resources/user-schema.json";
		String dataPath = path.toFile().getAbsolutePath() + "/src/main/resources/user.json";

		try (InputStream inSchema = new FileInputStream(schemaPath)) {			
			InputStream inData = new FileInputStream(dataPath);
			JSONArray data = new JSONArray(new JSONTokener(inData));
			JSONObject rawSchema = new JSONObject(new JSONTokener(inSchema));
			Schema schema = SchemaLoader.load(rawSchema);
			schema.validate(data);
			System.out.println("validated successfuly");
		} catch (ValidationException e) {
			// throws a ValidationException if this object is invalid
			JSONObject error = e.toJSON();
			System.out.println(error);
			System.out.println("\n");
			System.out.println(e.getMessage());
			e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
		}
	}
}
