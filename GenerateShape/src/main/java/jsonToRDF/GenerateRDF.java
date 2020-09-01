package jsonToRDF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.util.Context;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.emse.ci.sparqlext.SPARQLExt;
import fr.emse.ci.sparqlext.engine.PlanFactory;
import fr.emse.ci.sparqlext.engine.RootPlan;
import fr.emse.ci.sparqlext.query.SPARQLExtQuery;
import fr.emse.ci.sparqlext.stream.LocationMapperAccept;
import fr.emse.ci.sparqlext.stream.LocatorFileAccept;
import fr.emse.ci.sparqlext.stream.SPARQLExtStreamManager;
import fr.emse.ci.sparqlext.utils.ContextUtils;


// TODO on web project, duplicate keys validate wenne ne json schema eken
public class GenerateRDF {
	public static void main(String args[]) throws FileNotFoundException, IOException {
		Path path = Paths.get(".").toAbsolutePath().normalize();

		String schemaPath = path.toFile().getAbsolutePath() + "/src/main/resources/combine-schema.json";
		String dataPath = path.toFile().getAbsolutePath() + "/src/main/resources/combine.json";
		String queryPath = path.toFile().getAbsolutePath() + "/src/main/resources/combine-query.rqg";
		String defaultGraphPath = path.toFile().getAbsolutePath() + "/src/main/resources/default-graph.ttl";

		InputStream inSchema = new FileInputStream(schemaPath);

		InputStream inData = new FileInputStream(dataPath);

		try {
//			JSONObject data = new JSONObject(new JSONTokener(inData));
			JSONObject rawSchema = new JSONObject(new JSONTokener(inSchema));
			Schema schema = SchemaLoader.load(rawSchema);
			try {
//				schema.validate(data);
				System.out.println("validated success");

				String queryString = IOUtils.toString(new FileInputStream(queryPath), StandardCharsets.UTF_8);
				SPARQLExtQuery query = (SPARQLExtQuery) QueryFactory.create(queryString, SPARQLExt.SYNTAX);

				// TODO validate sg query

				RootPlan plan = PlanFactory.create(query);
				Model model = ModelFactory.createDefaultModel();
				RDFDataMgr.read(model, new FileInputStream(defaultGraphPath), Lang.TURTLE);
				LocatorFileAccept locator = new LocatorFileAccept(new File("/src/main/resources").toURI().getPath());
				LocationMapperAccept mapper = new LocationMapperAccept();

				mapper.addAltEntry("https://saint-etienne-gbfs.klervi.net/gbfs/en/station_information.json",
						"combine.json");
				SPARQLExtStreamManager sm = SPARQLExtStreamManager.makeStreamManager(locator, mapper);

				Context context = ContextUtils.build().setInputModel(model).setStreamManager(sm).build();

				// execute the plan
				Model output = plan.execGenerate(context);

				// output model
				StringWriter sboutput = new StringWriter();
//	        RDFDataMgr.write(sboutput, output, Lang.TURTLE);

				String url = "E:\\CPS2\\Year_2\\Internship\\malshani_internship\\GenerateShape\\src\\main\\resources\\combine.ttl";
				output.write(new FileOutputStream(new File(url)), "TURTLE");

			} catch (ValidationException e) {
				System.out.println("validated fail");
				JSONObject error = e.toJSON();
				System.out.println(error);
				System.out.println(e.getMessage());
				e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
//			e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
			}
		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}

	}
}
