package generateShape;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.emse.ci.sparqlext.SPARQLExt;
import fr.emse.ci.sparqlext.query.SPARQLExtQuery;
import fr.emse.ci.sparqlext.syntax.ElementGenerateTriplesBlock;
import fr.emse.ci.sparqlext.utils.ContextUtils;

public class ExtractClassesFromQuery {
	public static void main(String args[]) {

		Path path = Paths.get(".").toAbsolutePath().normalize();

		Model model = ModelFactory.createDefaultModel();
		String rdfsNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		model.setNsPrefix("rdfs", rdfsNS);
		Property rdfType = model.createProperty(rdfsNS + "type");

		String jsonSchema;
		String schemaPath = path.toFile().getAbsolutePath() + "/src/main/resources/roomSchema.json";
		String queryPath = path.toFile().getAbsolutePath() + "/src/main/resources/query.rqg";

		try {
			jsonSchema = new String(Files.readAllBytes(Paths.get(schemaPath)));
			JSONObject jsonObjectSchema = new JSONObject(new JSONTokener(jsonSchema));
			String queryString = IOUtils.toString(new FileInputStream(queryPath), StandardCharsets.UTF_8);
			try {
				SPARQLExtQuery query = (SPARQLExtQuery) QueryFactory.create(queryString, SPARQLExt.SYNTAX);

				// taken from QueryBNodeNormalizer class 115 line,
				for (int i = 0; i < query.getGenerateClause().size(); i++) {
					Element elem = query.getGenerateClause().get(i);
					if (elem instanceof ElementGenerateTriplesBlock) {
						for (Iterator<Triple> it = ((ElementGenerateTriplesBlock) elem).patternElts(); it.hasNext();) {
							Triple t = it.next();
							if (t.getPredicate().toString().equals(rdfType.toString())) {
								System.out.println(t.getObject().toString());
							}

						}
					}
				}
				
				
				PrefixMapping amp = query.getPrefixMapping();
				System.out.println(amp);
				Map<String,String> map = amp.getNsPrefixMap();
				

			} catch (Exception e1) {
				System.out.println(e1.getMessage());
			}
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}
	}
}
