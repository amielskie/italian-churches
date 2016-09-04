package sd.lab5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sd.lab5.others.PlaceSelected;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

@Path("/add")
public class AggiungiLuogoAiVisitati {

	// Modify to what name you want the file be and where you want to save it
	private static final String fileName = "visitedPage.txt"; // Visited Pages File Name
	private static final String fileSaveLocation = "C:\\Users\\AmielJairo\\Desktop\\"
			+ fileName; // Location of the Visited Pages Text File

	// Class variables
	private final static String DATE_FORMAT = "dd/MM/yyyy";
	private static int p = 0, c = 0, e = 0;
	private static String errorMessage = "";
	private static String resource = "";
	
	// HTML response when page successfully added a visited page
	private static String success = "<font color= \"#25FF5C\" > Success "
			+ "<br>"
			+ "Click the link below to view visted pages list </font> <br> <br>"
			+ "<a href = \"http://localhost:8080/LAB5/rest/visited\">Luoghi Visitati</a>";

	// Redirect to AddVisitedPage(itself)
	@GET
	public Response test() throws URISyntaxException {

		resource = PlaceSelected.getPlaceURI();

		if(PlaceSelected.getPlaceURI().equals("") || PlaceSelected.getPlaceURI() == null){
			errorMessage = "ERROR: Select a place to be saved.";
			System.out.println(resource);
			return Response.ok(errorMessage , MediaType.TEXT_PLAIN).build();
		}

		
		
		return Response.temporaryRedirect(
				new URI("http://localhost:8080/LAB5/AddVisitedPage.html"))
				.build();
	}


	// POST: Redirects to visited page and adds model to the text file
	@POST
	public Response saveRDF(@FormParam("date") String date,
			@FormParam("points") String points,
			@FormParam("crowd") String crowd, @FormParam("ease") String ease)
					throws URISyntaxException{
		
		errorMessage = "";

		// Check if forms are not null
		if (date.equals("") || points.equals("") || crowd.equals("")
				|| ease.equals("")) {
			errorMessage = "ERROR: Cannot leave empty fields.";
			System.out.println(errorMessage);
			return Response.ok(errorMessage, MediaType.TEXT_PLAIN).build();
		}

		// Check if date format is valid
		if (!isDateValid(date)) {
			errorMessage = "ERROR: Invalid date format.";
			System.out.println(errorMessage);
			return Response.ok(errorMessage, MediaType.TEXT_PLAIN).build();
		}

		// Parse the ratings to int
		try {
			p = Integer.parseInt(points);
			c = Integer.parseInt(crowd);
			e = Integer.parseInt(ease);
		} catch (NumberFormatException e) {
			errorMessage = "Invalid rating.";
			System.out.println(errorMessage);
			return Response.ok(errorMessage, MediaType.TEXT_PLAIN).build();
		}

		// Check if ratings are valid
		if (!rating(p) || !rating(c) || !rating(e)) {
			errorMessage = "ERROR: Rating must be between 1 and 10.";
			System.out.println(errorMessage);
			return Response.ok(errorMessage, MediaType.TEXT_PLAIN).build();
		}

		createModel(date, p, c, e);
		
		return Response.ok(success, MediaType.TEXT_HTML).build();

	}// Post

	// Creates a new Model from RDFModel text file
	public static void createModel(String date, int points, int crowd, int ease) {

		// Create a model from the rdfModel.txt
		Model model = ModelFactory.createDefaultModel();

		// Create resource and properties to be added
		Resource place = model.createResource(resource);
		Property visitato_il = model.createProperty("visitato_il");
		Property ha_punteggio = model.createProperty("ha_punteggio");
		Property affollato = model.createProperty("affollato");
		Property facilità_di_raggiungimento = model
				.createProperty("facilità_di_raggiungimento");

		// Convert ratings to string so I can easily add it as property
		// Cause i'm having trouble adding them as integers
		String p = Integer.toString(points);
		String c = Integer.toString(crowd);
		String e = Integer.toString(ease);

		// Add property to resource
		place.addProperty(visitato_il, date);
		place.addProperty(ha_punteggio, p);
		place.addProperty(affollato, c);
		place.addProperty(facilità_di_raggiungimento, e);

		// Print the model in the console
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		model.write(b, "Turtle");
		String newStatement = b.toString();

		
		// Check if a visitedPages file already exists
		File visitedPages = new File(fileSaveLocation);
		if (visitedPages.exists()) {
			updateFile(newStatement);
			System.out.println("UPDATED: Visited Pages File.");
			
		} 
		// Create a new visitedPages file and save the model data
		else {
			System.out.println("Created a new Visited Pages file.");
			FileOutputStream output = createFile();
			model.write(output, "Turtle");
			
			try {
				output.close();
			} catch (IOException e1) {
				System.out.println("Error closing the Visited Pages File.");
			}
			
		}

	}
	
	// Update an existing visitedPages file
	public static void updateFile(String newStatement){
		FileWriter fw;
		try {
			fw = new FileWriter(fileSaveLocation, true);
			fw.write("\n" + newStatement + "\n");
			fw.close();
		} catch (IOException e) {
			System.out.println("Error while updating the RDF Model file.");
		}
		

	}

	
	// Creates a new visitedPages file
	public static FileOutputStream createFile() {

		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(fileSaveLocation);
			System.out.println("CREATED: New Visited Pages File.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return outputStream;

	}

	// Check if rating is between 1 and 10
	public boolean rating(int rating) {

		if (rating <= 10 && rating > 0) {
			return true;
		}

		return false;

	}

	// Check date format
	public static boolean isDateValid(String date) {

		try {
			DateFormat df = new SimpleDateFormat(DATE_FORMAT);
			df.setLenient(false);
			df.parse(date);
			return true;
		} // End of try

		catch (ParseException e) {
			System.out.println("Invalid date format.");
			return false;
		} // End of Catch
	}

} // End of AggiungiLuogoAiVisitati
 