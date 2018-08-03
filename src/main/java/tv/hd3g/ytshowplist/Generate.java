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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.H1;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.attribute.Charset;
import com.webfirmframework.wffweb.tag.html.attribute.Href;
import com.webfirmframework.wffweb.tag.html.attribute.Rel;
import com.webfirmframework.wffweb.tag.html.attribute.global.Lang;
import com.webfirmframework.wffweb.tag.html.html5.stylesandsemantics.Section;
import com.webfirmframework.wffweb.tag.html.links.Link;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.metainfo.Meta;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

public class Generate {
	private static final Logger log = LogManager.getLogger();
	
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		Properties p = System.getProperties();
		p.load(new FileReader(new File("setup.properties")));
		new Generate(p).make();
	}
	
	private final Properties p;
	private final Gson gson;
	
	public Generate(Properties p) {
		this.p = p;
		GsonBuilder gb = new GsonBuilder();
		gb.serializeNulls();
		gson = gb.create();
	}
	
	public void make() throws IOException, GeneralSecurityException {
		final List<YTPlistItem> playlist_items;
		
		if (p.containsKey("offlinefile")) {
			File offlinefile = new File(p.getProperty("offlinefile"));
			
			log.debug("Switch to offline mode: {}", offlinefile.getAbsolutePath());
			
			if (offlinefile.exists() == false) {
				log.info("Create offline file: {}", offlinefile.getPath());
				
				String offline_content = gson.toJson(new YoutubePlaylist(p).getLastPlaylistItems());
				BufferedWriter writer = new BufferedWriter(new FileWriter(offlinefile));
				writer.write(offline_content);
				writer.close();
			}
			
			log.info("Load offline file: {}", offlinefile.getPath());
			
			BufferedReader reader = new BufferedReader(new FileReader(offlinefile));
			String offline_content = reader.lines().collect(Collectors.joining(" "));
			reader.close();
			
			playlist_items = gson.fromJson(offline_content, YTPlistItem.type_al_YTPlistItem);
		} else {
			playlist_items = new YoutubePlaylist(p).getLastPlaylistItems();
		}
		
		String css_uri = "style.css";
		File css_file = new File(p.getProperty("outfile.css", "style.css")).getCanonicalFile();
		
		if (css_file.exists() == false) {
			throw new FileNotFoundException("Can't found css file: " + css_file.getAbsolutePath());
		}
		
		/*<!doctype html>
		<html lang="fr">
		<head>
		<meta charset="utf-8">
		<title>Titre de la page</title>
		<link rel="stylesheet" href="style.css">
		<script src="script.js"></script>
		</head>
		<body>
		*/
		
		Html html = new Html(null, new Lang(Locale.getDefault())) {
			{
				new Head(this) {
					{
						new Meta(this, new Charset(StandardCharsets.UTF_8.toString().toLowerCase()));
						new Link(this, new Rel("stylesheet"), new Href(css_uri));
					}
				};
				new Body(this) {
					{
						new H1(this) {
							{
								new NoTag(this, "Derniers favoris Youtube");
							}
						};
						
						new Section(this) {
							{
								playlist_items.stream().map(pi -> pi.getView(this)).collect(Collectors.toUnmodifiableList());
							}
						};
					}
				};
			}
		};
		html.setCharset(StandardCharsets.UTF_8);
		html.setDocTypeTag("html");
		
		File outfile = new File(p.getProperty("outfile", "index.html")).getCanonicalFile();
		log.info("Save to " + outfile.getAbsolutePath());
		
		FileOutputStream fos = new FileOutputStream(outfile);
		html.toOutputStream(fos, StandardCharsets.UTF_8);
		fos.close();
		
		if (outfile.getParentFile().equals(css_file.getParentFile()) == false) {
			/**
			 * Copy css file side to index file.
			 */
			File css_dest = new File(outfile.getParentFile() + File.separator + css_uri);
			log.info("Deploy css file to " + css_dest.getParent());
			Files.copy(css_file.toPath(), css_dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		
	}
	
}
