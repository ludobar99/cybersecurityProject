package util;

import java.util.regex.Pattern;

public class Validator {
	
	public static boolean validateEmail(String email) {
		
		String pattern =  "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$";
		
		return patternMatches(email, pattern);

	}
	
	public static boolean validatePassword(String password) {
		
		String pattern = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}";
		
		return patternMatches(password, pattern);
		
	}
	
	public static boolean validateName(String name) {
		
		String pattern = "^[A-Za-z][A-Za-z\\'\\-]+([\\ A-Za-z][A-Za-z\\'\\-]+)*";

		
		return patternMatches(name, pattern);
	
	}
		
	
	private static boolean patternMatches(String string, String regexPattern) {
	    return Pattern.compile(regexPattern)
	      .matcher(string)
	      .matches();
	}
}
