package com.rpa;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import com.rpa.constants.RPAConstants;
import com.rpa.util.UtilityFile;

public class TEST {

	public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
		// TODO Auto-generated method stub
		File files = new File("C:\\Users\\CSS\\Downloads\\ACNTDownloadReport.action");

		String fileExtension = getFileExtension(files);
		System.out.println(fileExtension);

	}
	private static String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}

}
