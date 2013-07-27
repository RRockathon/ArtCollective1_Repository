package com.rrockathon.artcollective;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.usergrid.android.client.entities.Entity;
import org.usergrid.android.client.response.ApiResponse;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;

import com.google.gson.Gson;

public class ArtEventLoader {
	private transient ArtEventData currentEvent;
	public List<ArtEventData> getArtEventsData() throws IOException{
		ApiResponse apiResponse = null;
		List<ArtEventData> eventDataList = new ArrayList<ArtEventData>();
		List<ArtEventData> eventDataListFeed = null;
		MessageController messageController = new MessageController();
		apiResponse = messageController.login("test", "test");
		List<Entity> properties = messageController.getEvents();
		for (Entity entity : properties) {
			ArtEventData eventData = new Gson().fromJson(entity.getProperties()
					.toString(), ArtEventData.class);
			eventDataList.add(eventData);
		}
		eventDataListFeed = getArtEventsDataFromFeeds();
		for(ArtEventData eventData : eventDataListFeed){
			eventDataList.add(eventData);
		}
		return eventDataList;
	}
		
	public List<ArtEventData> getArtEventsDataFromFeeds() throws IOException{

		final long methodStartTime = System.currentTimeMillis();

		final List<ArtEventData> l = new ArrayList<ArtEventData>();
		URL url;
		url = new URL("http://www.austinchronicle.com/gyrobase/rss/arts.xml");
		final long gotISTime;

		final long parsedTime;

		try {
			InputStream is = url.openConnection().getInputStream();

			gotISTime = System.currentTimeMillis();

			RootElement root = new RootElement("rss");
			Element channel = root.getChild("channel");
			Element item = channel.getChild("item");
			Element title = item.getChild("title");
			Element description = item.getChild("description");
			Element link = item.getChild("link");

			item.setElementListener(new ElementListener() {
				@Override
				public void start(Attributes arg0) {
					currentEvent = new ArtEventData();
					currentEvent.setLocation("Austin");
				}

				@Override
				public void end() {

					l.add(currentEvent);
				}
			});

			title.setEndTextElementListener(new EndTextElementListener() {
				@Override
				public void end(String aBody) {
					currentEvent.setTitle(aBody);
				}
			});

			description.setEndTextElementListener(new EndTextElementListener() {
				@Override
				public void end(String aBody) {
					currentEvent.setDescription(aBody);
				}
			});

			link.setEndTextElementListener(new EndTextElementListener() {
				@Override
				public void end(String aBody) {
					currentEvent.setWebsite(aBody);
				}
			});

			XMLReader reader = XMLReaderFactory
					.createXMLReader(org.xmlpull.v1.sax2.Driver.class.getName());
			reader.setContentHandler(root.getContentHandler());
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
			line = sb.toString();
			in = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(line.getBytes("UTF-8"))));
			reader.parse(new InputSource(in));
			in.close();
			parsedTime = System.currentTimeMillis();

		} catch (SAXException e) {
			e.printStackTrace();
			return l;
		}

		final long endTime = System.currentTimeMillis();

		Log.i(ArtEventLoader.class.getName(),
				String.format(
						"getting input stream took %d ms; parsing took %d ms; building took %d ms",
						(gotISTime - methodStartTime),
						(parsedTime - gotISTime), (endTime - parsedTime)));

		return l;

	}
}
