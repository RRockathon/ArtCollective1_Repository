package com.rrockathon.artcollective;

import java.util.ArrayList;
import java.util.List;

import org.usergrid.android.client.entities.Entity;
import org.usergrid.android.client.response.ApiResponse;

import com.google.gson.Gson;

public class ArtEventLoader {

	public List<ArtEventData> getArtEventsData(){
		ApiResponse apiResponse = null;
		List<ArtEventData> eventDataList = new ArrayList<ArtEventData>();
		MessageController messageController = new MessageController();
		apiResponse = messageController.login("test", "test");
		List<Entity> properties = messageController.getEvents();
		for (Entity entity : properties) {
			ArtEventData eventData = new Gson().fromJson(entity.getProperties()
					.toString(), ArtEventData.class);
			eventDataList.add(eventData);
		}
		return eventDataList;
	}
		
}
