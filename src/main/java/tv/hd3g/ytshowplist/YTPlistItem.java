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

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.common.reflect.TypeToken;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.H3;
import com.webfirmframework.wffweb.tag.html.attribute.Href;
import com.webfirmframework.wffweb.tag.html.attribute.Src;
import com.webfirmframework.wffweb.tag.html.attribute.Target;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.html5.stylesandsemantics.Article;
import com.webfirmframework.wffweb.tag.html.images.Img;
import com.webfirmframework.wffweb.tag.html.links.A;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

public class YTPlistItem implements Comparable<YTPlistItem> {
	
	static final Type type_al_YTPlistItem = new TypeToken<ArrayList<YTPlistItem>>() {}.getType();
	
	private int absolute_pos;
	private String videoid;
	private String title;
	// private String descr;
	
	protected YTPlistItem() {
		
	}
	
	public YTPlistItem(int absolute_pos, String videoid, String title, String descr) {
		this.absolute_pos = absolute_pos;
		this.videoid = videoid;
		this.title = title;
		// this.descr = descr;
	}
	
	public AbstractHtml getView(AbstractHtml parent) {
		return new Article(parent) {
			{
				new A(this, new ClassAttribute("thum"), new Target("_blank"), new Href("https://www.youtube.com/watch?v=" + videoid)) {
					{
						new Img(this, new Src("https://i.ytimg.com/vi/" + videoid + "/mqdefault.jpg"));
					}
				};
				
				new H3(this) {
					{
						new A(this, new Target("_blank"), new Href("https://www.youtube.com/watch?v=" + videoid)) {
							{
								new NoTag(this, title);
							}
						};
					}
				};
			}
		};
	}
	
	public int compareTo(YTPlistItem o) {
		return absolute_pos - o.absolute_pos;
	}
	
	/*
	"default" : {
	  "height" : 90,
	  "url" : "https://i.ytimg.com/vi/eXR1olg_I0w/default.jpg",
	  "width" : 120
	},
	"high" : {
	  "height" : 360,
	  "url" : "https://i.ytimg.com/vi/eXR1olg_I0w/hqdefault.jpg",
	  "width" : 480
	},
	"maxres" : {
	  "height" : 720,
	  "url" : "https://i.ytimg.com/vi/eXR1olg_I0w/maxresdefault.jpg",
	  "width" : 1280
	},
	"medium" : {
	  "height" : 180,
	  "url" : "https://i.ytimg.com/vi/eXR1olg_I0w/mqdefault.jpg",
	  "width" : 320
	},
	"standard" : {
	  "height" : 480,
	  "url" : "https://i.ytimg.com/vi/eXR1olg_I0w/sddefault.jpg",
	  "width" : 640
	}
	* */
	
}
