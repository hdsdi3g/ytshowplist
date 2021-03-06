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
import java.util.Arrays;
import java.util.Collections;
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
import com.webfirmframework.wffweb.tag.html.attribute.global.Title;
import com.webfirmframework.wffweb.tag.html.html5.stylesandsemantics.Section;
import com.webfirmframework.wffweb.tag.html.links.Link;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.metainfo.Meta;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

public class Generate {
	
	/*static {
		if (new File("log4j2.xml").exists()) {
			System.setProperty("log4j.configurationFile", "log4j2.xml");
		}
	}*/
	
	private static final Logger log = LogManager.getLogger();
	
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		Properties p = System.getProperties();
		
		File working_dir = Arrays.stream(((String) p.get("java.class.path")).split(File.pathSeparator)).map(cp -> {
			return new File(cp);
		}).filter(cp -> {
			return cp.isDirectory() && cp.exists() && cp.canRead() && cp.getName().equals("config");
		}).findFirst().orElseThrow(() -> new FileNotFoundException("config directory in classpath")).getCanonicalFile();
		
		log.debug("Use config directory: " + working_dir);
		
		p.load(new FileReader(new File(working_dir.getAbsolutePath() + File.separator + "setup.properties")));
		new Generate(p, working_dir).make();
	}
	
	private final Properties p;
	private final Gson gson;
	private final File working_dir;
	
	public Generate(Properties p, File working_dir) {
		this.p = p;
		this.working_dir = working_dir;
		GsonBuilder gb = new GsonBuilder();
		gb.serializeNulls();
		gson = gb.create();
	}
	
	public void make() throws IOException, GeneralSecurityException {
		final List<YTPlistItem> playlist_items;
		
		if (p.containsKey("offlinefile")) {
			File offlinefile = new File(working_dir.getAbsolutePath() + File.separator + p.getProperty("offlinefile"));
			
			log.debug("Switch to offline mode: {}", offlinefile.getAbsolutePath());
			
			if (offlinefile.exists() == false) {
				log.info("Create offline file: {}", offlinefile.getPath());
				
				String offline_content = gson.toJson(new YoutubePlaylist(p, working_dir).getLastPlaylistItems());
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
			playlist_items = new YoutubePlaylist(p, working_dir).getLastPlaylistItems();
		}
		
		String css_uri = "style.css";
		File css_file = new File(working_dir.getAbsolutePath() + File.separator + p.getProperty("outfile.css", "style.css")).getCanonicalFile();
		
		if (css_file.exists() == false) {
			throw new FileNotFoundException("Can't found css file: " + css_file.getAbsolutePath());
		}
		
		Html html = new Html(null, new Lang(Locale.getDefault())) {
			{
				new Head(this) {
					{
						new Title(p.getProperty("pagetitle", ""));
						new Meta(this, new Charset(StandardCharsets.UTF_8.toString().toLowerCase()));
						new Link(this, new Rel("stylesheet"), new Href(css_uri));
					}
				};
				new Body(this) {
					{
						new H1(this) {
							{
								new NoTag(this, p.getProperty("pagetitle", ""));
							}
						};
						
						new Section(this) {
							{
								playlist_items.stream().sorted(Collections.reverseOrder()).map(pi -> pi.getView(this)).collect(Collectors.toUnmodifiableList());
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
