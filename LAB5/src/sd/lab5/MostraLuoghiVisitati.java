package sd.lab5;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


@Path("/visited")
public class MostraLuoghiVisitati {
	
	// Modify to what the filename(RDFModel from moodle) is and where it is saved
	private static final String fileName = "visitedPage.txt"; // Visited Pages File Name
	private static final String fileSaveLocation = "C:\\Users\\AmielJairo\\Desktop\\"
			+ fileName; // Location of the Visited Pages Text File

	// Prints Visited Pages on the console and on the client side
	@GET
	public Response test() throws URISyntaxException {
		
		System.out.println("\n List of Saved Visited Places triples. \n");
		
		// Calls the getSavedPlaces method and store it's string in a variable
		String visitedPlaces = getSavedPlaces();
		
		// Sends out a message if the Visited Places RDFModel is empty
		if(visitedPlaces.equals("") || visitedPlaces == null){
			return Response.ok("No visited places saved.", MediaType.TEXT_PLAIN).build();
		}

		// Returns the tripples in the visitedPlaces
		return Response.ok(visitedPlaces, MediaType.TEXT_PLAIN).build();
	}
	
	
	// Gets the Visited Places RDFModel and  converts it to string
	public String getSavedPlaces(){
		Model model = createModel();
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		model.write(b, "Turtle");
		String newStatement = b.toString();
		
		return newStatement;
	}
	

	// Creates a new Model from RDFModel text file
	public static Model createModel() {

		// Create a model from the rdfModel.txt
		Model model = ModelFactory.createDefaultModel();
		FileInputStream inputStream = null;
		try {

			// Find the path of the RDFModel text file provided in moodle
			inputStream = new FileInputStream(fileSaveLocation);
		}

		catch (FileNotFoundException e) {
			System.out.println("Visited Page RDF Model NOT FOUND.");
		}

		RDFDataMgr.read(model, inputStream, Lang.TURTLE);
		
		// Prints the triples in the console
		RDFDataMgr.write(System.out, model, Lang.TURTLE);


		return model;
	}

	
}
