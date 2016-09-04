package sd.lab5.others;

/*
 * Gets and sets the uri of the link that the user selected 
 * so the other servers can have access to it
 * */
public class PlaceSelected {
	
	private static String placeURI;

	public static String getPlaceURI() {
		return placeURI;
	}

	public static void setPlaceURI(String placeURI) {
		PlaceSelected.placeURI = placeURI;
	}
	

}
