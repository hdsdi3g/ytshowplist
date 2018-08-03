/*
 * This file is part of ytshowplist.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 * 
*/
package tv.hd3g.ytshowplist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;

public class Generate {
	
	private static final Logger log = LogManager.getLogger();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GoogleCredential credential = new GoogleCredential().setAccessToken(""); // XXX ?
		Plus plus = new Plus.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName("Google-PlusSample/1.0").build();
		
	}
	
}
