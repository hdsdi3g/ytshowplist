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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

public class YoutubeAPI {
	
	static {
		// System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		Logger.getLogger(FileDataStoreFactory.class.getName()).setLevel(Level.SEVERE);
	}
	
	public final YouTube youtube;
	
	public YoutubeAPI(String application_name, File client_id, File datastore) throws FileNotFoundException, IOException, GeneralSecurityException {
		if (application_name == null) {
			throw new NullPointerException("\"application_name\" can't to be null");
		} else if (client_id == null) {
			throw new NullPointerException("\"client_id\" can't to be null");
		} else if (client_id.exists() == false) {
			throw new FileNotFoundException(client_id.getPath());
		} else if (datastore == null) {
			throw new NullPointerException("\"datastore\" can't to be null");
		}
		
		JsonFactory json_factory = JacksonFactory.getDefaultInstance();
		List<String> scopes = Arrays.asList(YouTubeScopes.YOUTUBE_READONLY);
		
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(json_factory, new FileReader(client_id));
		
		HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		FileDataStoreFactory DATA_STORE_FACTORY = new FileDataStoreFactory(datastore);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, json_factory, clientSecrets, scopes).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		
		LocalServerReceiver lsr = new LocalServerReceiver();
		AuthorizationCodeInstalledApp acia = new AuthorizationCodeInstalledApp(flow, lsr);
		Credential credential = acia.authorize("user");
		
		youtube = new YouTube.Builder(HTTP_TRANSPORT, json_factory, credential).setApplicationName(application_name).build();
	}
	
}
