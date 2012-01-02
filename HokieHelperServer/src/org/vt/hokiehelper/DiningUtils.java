package org.vt.hokiehelper;

import java.util.Arrays;
import java.util.HashSet;

public class DiningUtils {
	public static String[] halls = {"D2", "DXpress", "Shultz Dining Center", "Shultz Express", "Owens Food Court",
        "Deet's Place", "Hokie Grill & Co.", "Carvel", "Cinnabon",
        "West End Market", "Vet Med Cafe", "Sbarro", "Au Bon Pain - Squires Cafe",
        "Au Bon Pain - GLC", "Au Bon Pain - Squires Kiosk"};
	public static HashSet<String> hallsHash = null;
	
	static {
		// Initialize hallsHash
		hallsHash = new HashSet<String>(Arrays.asList(halls));
	}
}
