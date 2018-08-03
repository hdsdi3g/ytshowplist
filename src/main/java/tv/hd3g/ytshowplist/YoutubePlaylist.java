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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;

public class YoutubePlaylist {
	
	private static final Logger log = LogManager.getLogger();
	
	private final YouTube youtube;
	private final String channelid;
	private final String playlistid;
	private final Properties p;
	
	public YoutubePlaylist(Properties p) throws IOException, GeneralSecurityException {
		this.p = p;
		
		youtube = new YoutubeAPI(p.getProperty("youtube.appname"), new File(p.getProperty("youtube.clientid")), new File(p.getProperty("youtube.datastore"))).youtube;
		
		if (p.containsKey("youtube.channelid") == false) {
			if (p.containsKey("youtube.channeltitle") == false) {
				throw new IOException("Missing youtube.channelid or youtube.channeltitle in configuration");
			}
			
			YouTube.Channels.List channelsListByUsernameRequest = youtube.channels().list("snippet,contentDetails");
			channelsListByUsernameRequest.setForUsername(p.getProperty("youtube.channeltitle"));
			
			ChannelListResponse response = channelsListByUsernameRequest.execute();
			String _channelid = response.getItems().stream().findFirst().get().getId();
			p.put("youtube.channelid", _channelid);
			
			log.warn("Please add to configuration: \"youtube.channelid={}\"", _channelid);
			System.exit(1);
		}
		
		channelid = p.getProperty("youtube.channelid");
		
		log.info("Works on channel id {}", channelid);
		
		if (p.containsKey("youtube.playlistid") == false) {
			YouTube.Playlists.List playlistsListByChannelIdRequest = youtube.playlists().list("snippet,contentDetails");
			playlistsListByChannelIdRequest.setChannelId(channelid).setMaxResults(50l);
			
			PlaylistListResponse response = playlistsListByChannelIdRequest.execute();
			
			log.warn("Please set playlist Id on configuration, as \"youtube.playlistid=XXXXXXX-xxxxxxxxxxx\"");
			response.getItems().forEach(pl -> {
				System.out.println("Playlist name:\t\"" + Stream.of(pl.getSnippet().getTitle(), pl.getSnippet().getLocalized().getTitle()).filter(t -> t.isEmpty() == false).distinct().findFirst().get() + "\"");
				System.out.println("Id:\t" + pl.getId());
				
				if (pl.getSnippet().getLocalized().getDescription().isEmpty() != false) {
					System.out.println("Description:\t" + pl.getSnippet().getLocalized().getDescription());
				}
				System.out.println();
			});
			System.exit(1);
		}
		playlistid = p.getProperty("youtube.playlistid");
		
	}
	
	public List<YTPlistItem> getLastPlaylistItems() throws IOException {
		long limit = Long.valueOf((String) p.getOrDefault("youtube.lastitemcount", "15"));
		
		YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = youtube.playlistItems().list("snippet,contentDetails");
		playlistItemsListByPlaylistIdRequest.setMaxResults(limit).setPlaylistId(playlistid);
		
		PlaylistItemListResponse response = playlistItemsListByPlaylistIdRequest.execute();
		
		log.trace("Raw response: {}", () -> {
			try {
				return response.toPrettyString();
			} catch (IOException e) {
				throw new RuntimeException("Can't get json", e);
			}
		});
		
		return response.getItems().stream().map(pl_item -> {
			return new YTPlistItem(Long.valueOf(pl_item.getSnippet().getPosition()).intValue(), pl_item.getContentDetails().getVideoId(), pl_item.getSnippet().getTitle(), pl_item.getSnippet().getDescription());
		}).collect(Collectors.toList());
	}
	
}
