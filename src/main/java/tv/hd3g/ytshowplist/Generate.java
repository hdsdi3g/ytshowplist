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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.html5.stylesandsemantics.Article;
import com.webfirmframework.wffweb.tag.html.html5.stylesandsemantics.Section;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
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
		List<YTPlistItem> playlist_items;
		
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
		
		List<Article> articles = new ArrayList<>();
		
		Section section = new Section(null);
		
		Html html = new Html(null) {
			{
				new Head(this);
				new Body(this) {
					{
						new NoTag(this, "Hello World");
						section.getChildren();
					}
				};
			}
		};
		
		File outfile = new File(p.getProperty("outfile", "index.html"));
		log.info("Save to " + outfile.getAbsolutePath());
		// <section> <article />
		
		FileOutputStream fos = new FileOutputStream(outfile);
		html.toOutputStream(fos, StandardCharsets.UTF_8);
		fos.close();
		
		// TODO Auto-generated method stub
		
	}
}
