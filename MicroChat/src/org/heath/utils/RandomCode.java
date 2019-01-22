package org.heath.utils;

public class RandomCode {
	
	public static String getRandomCode(){
		String[] code = {
				"0","1","2","3","4","5","6","7","8","9",
				"a","b","c","d","e","f","g","h","i","j",
				"k","l","m","n","o","p","q","r","s","t",
				"u","v","w","s","y","z"
		};
		String randomCode = "";
		for (int i = 0; i < 20; i++) {
			int random = (int) (Math.random()*36);
			randomCode+=code[random];
		}
		return randomCode;
	}

}
