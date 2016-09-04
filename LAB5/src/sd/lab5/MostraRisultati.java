package sd.lab5;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import sd.lab5.others.PlaceSelected;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

@Path("/luoghi")
public class MostraRisultati {

	private static List<String> stringToBeReturned = new ArrayList<>(); // List of strings that matches the userInput
	private static String searchPlace = ""; // Variable to take the userInput
	private static boolean matchFound = false;
	private static final String RDFModelFile = "C:/Users/AmielJairo/Desktop/RdfModel.txt";

	// POST: Gets the user input and makes a list of all matches found
	@POST
	public Response search(@FormParam("userInput") String userInput)
			throws URISyntaxException {

		// Do nothing if user input is empty or lessa than 3 characters long
		if (userInput.equals("") || userInput.length() < 4) {
			return Response.status(Status.NOT_FOUND).build();
		}

		// Reset matchFound to false
		matchFound = false;

		// Replace spaces with _(underscore)
		searchPlace = toURI(userInput);

		// Clear out the list first
		clearList();

		// Make a new list of the string matches
		scanModel(userInput.toString());

		// Print the list of results
		// printResult();

		// Return a ResponseBuilder
		return Response
				.status(Status.CREATED)
				.location(
						new URI("http://localhost:8080/LAB5/rest/luoghi/"
								+ searchPlace)).build();

	}
	
	// GET(PARAM): Get the place that the user clicks
	@GET
	@Path("{uri}")
	@Produces(MediaType.TEXT_HTML)
	public String displaySearch(@PathParam("uri") String uri) {

		// Get place info
		String placeInfo = getPlace(uri);

		return placeInfo;
	}

	// GET:  Get all the matches of the user's input
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getResults() {

		StringBuffer links = new StringBuffer();

		// Check if the list is not empty
		if (stringToBeReturned != null) {

			links.append("<font size = \"5\" color = \"#AFD0D7\" >");
			links.append("Search results for : <b>" + cleanLink(searchPlace) + "</b> </font> " + "<br> <br>");

			for (String s : stringToBeReturned) {
				// Replace spaces with "_" (underscore)
				s = toURI(s);

				// Add an html tag link to the resource subject
				links.append("<li> <a href=\"" + s + "\" class=\"getPlace\"> "
						+ cleanLink(s) + "</a> </li>");
			}
			return links.toString();

		}

		else
			return "<center><h1> No Match Found </h1> </center>";
	}

	// Creates a list of possible places the user is searching
	public static void scanModel(String text) {

		// Converting variables to be usable
		final String place = text.toLowerCase();
		String subjectTemp = "";
		String[] placeToBeSearched = place.split(" ");

		// Create a Model
		Model model = createModel();

		// Scan the whole userInput so it will be the first
		// on the list
		scanUserInput(place, model);

		// End the search if an exact userInput match is found
		if (matchFound) {
			return;
		}

		else {

			// Check the words if they have a match on the RDFModel text file
			for (String s : removeUselessWords(placeToBeSearched)) {

				// Create StmtIterator after each word checked all the
				// statements
				StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null));

				subjectTemp = "";

				// Compare strings to the list of places on the RDFModel text
				// file.
				while (iter.hasNext()) {
					Statement stmt = iter.nextStatement();
					Resource subject = stmt.getSubject();

					// Check if a uri matches a string that the user is
					// searching
					if (subject.toString().toLowerCase().contains(s)) {

						// Skips if the subject has already been checked
						if (subjectTemp.equals(subject.toString())) {
							// Do nothing
						}

						else if (alreadyExists(subject.toString()) == true) {
							// Do nothing if a result already exists in the list
						}

						// Add subject to the stringToBeReturned list
						else {
							stringToBeReturned.add(subject.toString());
						}

					}

					subjectTemp = subject.toString();
				}
			}
		}

	}

	// Returns an HTML code containing the place's properties
	public String getPlace(String uri) {

		String pageTitle = uri.replaceAll("_", " ");

		// Set uri to the PlaceSelected class

		PlaceSelected.setPlaceURI("http://localhost:8080/LAB5/rest/luoghi/" + pageTitle);
		
		// Predicate values
		String latitudine = "latitudine";
		String longitudine = "longitudine";
		String descrizione = "descrizione";
		String immagine = "immagine";

		// Object values
		String latitude = null;
		String longitude = null;
		String description = null;
		String image = null;

		StringBuffer pageContent = new StringBuffer();

		// Create a Model
		Model model = createModel();

		// Create StmtIterator after each word checked all the statements
		StmtIterator iter = model.listStatements(new SimpleSelector(null, null,
				(RDFNode) null));

		while (iter.hasNext()) {

			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			// Get the subject's predicate's object
			if (subject.toString().contains(pageTitle)) {

				if (predicate.toString().contains(immagine)) {
					image = removeTags(object.toString());
				}

				else if (predicate.toString().contains(descrizione)) {
					description = removeTags(object.toString());
				}

				else if (predicate.toString().contains(longitudine)) {
					longitude = removeTags(object.toString());
				}

				else if (predicate.toString().contains(latitudine)) {
					latitude = removeTags(object.toString());
				}
			}
		} // WHILE

		// Sorry for the messy code
		// Create the html tags
		pageContent.append("<center>");
		pageContent.append("<font size = \" 6 \" color = \" #CBBEF9 \" >"
				+ pageTitle + "</font>");
		pageContent.append("<br>");
		pageContent.append("<img src=\"" + image + "\" "
				+ "style=\"width:500px;height:300px\">");
		pageContent
				.append("<p> <font size=\"5\"> <b>Description: </b></font></p> ");
		pageContent.append("<p> <font size=\"4\"><i>" + description
				+ "</i></font></p>");
		pageContent.append("<p> <font size=\"4\"> <b>Longitude :</b> "
				+ longitude + "</font></p>");
		pageContent.append("<p> <font size=\"4\"> <b>Latitude :</b> "
				+ latitude + "</font></p>");
		pageContent.append("<font size =  \"4\" face = \"verdana\" >");
		pageContent.append("<a href= \"http://localhost:8080/LAB5/rest/add\" class=\"addVisited\"> Aggiungi ai visitati </a>");
		pageContent.append("</font>");
		pageContent.append("</center>");

		return pageContent.toString();
	}

	// Creates a new Model from RDFModel text file
	public static Model createModel() {

		// Create a model from the rdfModel.txt
		Model model = ModelFactory.createDefaultModel();
		FileInputStream inputStream = null;
		try {

			// Find the path of the RDFModel text file provided in moodle
			inputStream = new FileInputStream(RDFModelFile);
		}

		catch (FileNotFoundException e) {
			System.out.println("RDF file not found.");
		}

		RDFDataMgr.read(model, inputStream, Lang.TURTLE);
		

		return model;
	}

	// Search for the whole user input
	public static void scanUserInput(String userInput, Model model) {
		// Create StmtIterator after each word checked all the statements
		StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null));

		String subjectTemp = "";

		// Compare strings to the list of places on the RDFModel text file.
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();

			// Check if a uri matches a string that the user is searching
			if (subject.toString().toLowerCase().contains(userInput)) {

				// Skips if the subject has already been checked
				if (subjectTemp.equals(subject.toString())) {
					// Do nothing
				}

				else if (alreadyExists(subject.toString()) == true) {
					// Do nothing if a result already exists in the list
				}

				// Add subject to the stringToBeReturned list
				else {
					stringToBeReturned.add(subject.toString());
					matchFound = true;
				}

			}
			subjectTemp = subject.toString();
		}
	}

	// String parsing methods below

	// Convert the searched place to a uri compatible format
	public static String toURI(String place) {

		String newPlaceURI = place.replaceAll(" ", "_");

		return newPlaceURI;
	}

	// Removes articles in the userInput
	public static List<String> removeUselessWords(String[] s) {
		List<String> result = new ArrayList<>();

		for (int x = 0; x < s.length; x++) {

			if (s[x].length() < 3 || s[x].equals("dalle") || s[x].equals("dal")
					|| s[x].equals("alla") || s[x].equals("dalla")
					|| s[x].equals("nel") || s[x].equals("nella")
					|| s[x].equals("san") || s[x].equals("dei")) {
			}

			// Add searched words to a list
			else {
				result.add(s[x]);
			}
		}
		return result;
	}

	// Check if the a queryResult is already in the list
	public static boolean alreadyExists(String result) {

		for (String s : stringToBeReturned) {

			if (s.equals(result)) {
				return true;
			}
		}
		return false;
	}

	// Print the list in the console (optional)
	public void printResult() {
		for (String s : stringToBeReturned) {
			System.out.println(s);
		}
	}

	// Clear list results
	public static void clearList() {
		stringToBeReturned.clear();
	}

	// Remove ^^http://www.w3.org/2001/XMLSchema#string
	public String removeTags(String s) {

		s = s.replace("^^http://www.w3.org/2001/XMLSchema#string", "");

		return s;
	}

	// Returns the name of the place
	public String cleanLink(String s) {

		s = s.replaceAll("http://localhost:8080/LAB5/rest/luoghi/", "");
		s = s.replaceAll("_", " ");

		return s;
	}
}
