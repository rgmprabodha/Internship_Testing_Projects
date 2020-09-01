package JSONSchemaToVariables;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.io.parser.JSONP;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.PatternVars;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import fr.emse.ci.sparqlext.SPARQLExt;
import fr.emse.ci.sparqlext.SPARQLExtException;
import fr.emse.ci.sparqlext.lang.SPARQLExtParser;
import fr.emse.ci.sparqlext.normalizer.aggregates.ExprNormalizer;
import fr.emse.ci.sparqlext.normalizer.xexpr.NodeExprNormalizer;
import fr.emse.ci.sparqlext.normalizer.xexpr.QueryPatternNormalizer;
import fr.emse.ci.sparqlext.query.SPARQLExtQuery;
import fr.emse.ci.sparqlext.syntax.ElementGenerateTriplesBlock;
import fr.emse.ci.sparqlext.syntax.ElementIterator;
import fr.emse.ci.sparqlext.syntax.ElementSource;
import fr.emse.ci.sparqlext.syntax.ElementSubExtQuery;
import org.apache.jena.sparql.syntax.ElementGroup;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import fr.emse.ci.sparqlext.query.SPARQLExtQueryVisitor;
import fr.emse.ci.sparqlext.syntax.FromClause;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.ExprList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindVariables {
	/**
	 * @param args
	 */

	public static final ExprNormalizer enzer = new ExprNormalizer(null);

	public static List<String> sources = new ArrayList<String>();
	public static ArrayList<String> iterateVariables = new ArrayList<String>();
	public static ArrayList<String> bindVariables = new ArrayList<String>();
	public static LinkedHashMap<String, String> variableMap = new LinkedHashMap<String, String>();


	public static void main(String args[]) {

		Path path = Paths.get(".").toAbsolutePath().normalize();
		String queryPath = path.toFile().getAbsolutePath() + "/src/main/resources/combine/array/combine-query.rqg";

		try {
			String queryString = IOUtils.toString(new FileInputStream(queryPath), StandardCharsets.UTF_8);
			try {
				SPARQLExtQuery query = (SPARQLExtQuery) QueryFactory.create(queryString, SPARQLExt.SYNTAX);

				getSourceAndIterator(query);// source and iterator
				getBindVariables(query); // bind variables
				fillVariableMap();

			} catch (QueryParseException qe) {
				System.out.println("query error");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getSourceAndIterator(SPARQLExtQuery query) {
		final List<Element> bindingClauses = new ArrayList<>();
		
		if (query.getBindingClauses() != null) {
			for (final Element bindingClause : query.getBindingClauses()) {
				if (bindingClause instanceof ElementBind) {
					ElementBind el = (ElementBind) bindingClause;
					final Expr expr = enzer.normalize(el.getExpr());
					System.out.println("1: " + expr.toString());
					bindingClauses.add(new ElementBind(el.getVar(), expr));
				} else if (bindingClause instanceof ElementIterator) {

					ElementIterator el = (ElementIterator) bindingClause;
					final Expr expr = enzer.normalize(el.getExpr());

					ExprFunction function = expr.getFunction();
					String iri = function.getFunctionIRI();
					ExprList exprList = new ExprList(function.getArgs());
					String source = exprList.get(0).toString();
					String path = exprList.get(1).toString().replace("\"", "").replace("$.", "");
					String var = (el.getVars().get(0).toString());

					String tring = var + "," + source + "," + path;

					iterateVariables.add(tring); // add to array

				} else if (bindingClause instanceof ElementSource) {
					ElementSource el = (ElementSource) bindingClause;
					final NodeExprNormalizer nenzer = new NodeExprNormalizer(bindingClauses);
					el.getSource().visitWith(nenzer);
					final Node source = nenzer.getResult();
					final Node accept;
					if (el.getAccept() == null) {
						accept = null;
					} else {
						el.getAccept().visitWith(nenzer);
						accept = nenzer.getResult();
					}
					ElementSource es = new ElementSource(source, accept, el.getVar());
//					System.out.println("3: " + el.getVar().toString());
//					System.out.println("3: " + es.getSource());

					String s = el.getVar().toString();
					String d = es.getSource().toString();
					String sd = s + "," + d;

					sources.add(sd); // added to array
					bindingClauses.add(es);
				} else {
					throw new NullPointerException("Should not reach this point");
				}
			}
			query.setBindingClauses(bindingClauses);

		}
	}

	public static void getBindVariables(SPARQLExtQuery query) {
		Element bindingObject = query.getQueryPattern();

		LinkedHashSet<Var> queryVars = new LinkedHashSet<>();
		PatternVars.vars(queryVars, query.getQueryPattern());

		if (query.getQueryPattern() != null) {
			Element el = query.getQueryPattern();
			ElementGroup d = (ElementGroup) el;

			List<Element> ss = d.getElements();

			for (Element ele : ss) {
				ElementBind el1 = (ElementBind) ele;
				final Expr expr = enzer.normalize(el1.getExpr());

				String ds = expr.getFunction().getArgs().get(0).toString();
				ds = ds.replace("(", "").replace(")", "");
				String path = ds.split(" ")[2].toString().replace("\"", "").replace("$.", "");
				String var = enzer.normalize(el1.getVar()).toString();
				String source = expr.getVarsMentioned().toArray()[0].toString();
				String tring = var + "," + source + "," + path;

				bindVariables.add(tring); // add to array
			}
		}
		List<Binding> values = query.getValuesData();
	}

	public static void fillVariableMap() {
		// Assume I have only one source
		String sd = sources.get(0);
		String rootSource = sd.split(",")[0].toString();

		for (String string : iterateVariables) {
			String[] t = string.split(",");
			String var = t[0].toString();
			String source = t[1].toString();
			String path = t[2].toString();
			if (!variableMap.containsKey(var)) {
				if (source.equals(rootSource)) {
					variableMap.put(var, path);
				} else {
					String pathPrefix = variableMap.get(source).toString();
					String newPath = pathPrefix + "." + path;
					variableMap.put(var, newPath);
				}
			}
		}

		for (String string : bindVariables) {
			String[] t = string.split(",");
			String var = t[0].toString();
			String source = t[1].toString();
			String path = t[2].toString();

			if (!variableMap.containsKey(var)) {
				String pathPrefix = variableMap.get(source).toString();
				String newPath = pathPrefix + "." + path;
				variableMap.put(var, newPath);
			}
		}

		JSONObject finalMap = new JSONObject();
		for (Map.Entry entry : variableMap.entrySet()) {
			String var = (String) entry.getKey();
			String path = (String) entry.getValue();
			finalMap.put(var, path); 
		}
		
		try {

			Path path = Paths.get(".").toAbsolutePath().normalize();
			String variableMappingPath = path.toFile().getAbsolutePath()
					+ "/src/main/resources/combine/variableMapping.json";
			FileWriter fileWriter = new FileWriter(variableMappingPath);
			fileWriter.write(finalMap.toString(4));
			fileWriter.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
