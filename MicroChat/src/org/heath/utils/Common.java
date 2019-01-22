package org.heath.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JSONArray;

public class Common {
	
	public static JSONArray joinJSONArray(JSONArray mData, JSONArray array) {
		try {
			for (int i = 0; i < array.size(); i++) {
				mData.add(array.get(i));
			}
			return mData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
    }
	
	public static String getRandomCode(){
		String[] code = {
				"0","1","2","3","4","5","6","7","8","9"
		};
		String randomCode = "";
		for (int i = 0; i < 8; i++) {
			int random = (int) (Math.random()*10);
			randomCode+=code[random];
		}
		return randomCode;
	}
	
	public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

}
