package com.rrockathon.artcollective;


import java.util.ArrayList;
import java.util.List;

import org.usergrid.android.client.entities.Entity;
import org.usergrid.android.client.response.ApiResponse;
import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.EditText;
import com.google.gson.Gson;


/**
 * List sites and allow selecting them to see details.
 * 
 */
public abstract class Lister extends ListActivity {
	private EditText editText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lister);
		fillData();
	}

	abstract String getMyType();

	private void fillData() {
		ApiResponse apiResponse = null;
		List<ArtEventData> eventDataList = new ArrayList<ArtEventData>();
		MessageController messageController = new MessageController();
		apiResponse = messageController.login("test", "test");
		editText = (EditText) findViewById(R.id.editText1);
		List<Entity> properties = messageController.getEvents();
		for (Entity entity : properties) {
			ArtEventData eventData = new Gson().fromJson(entity.getProperties()
					.toString(), ArtEventData.class);
			eventDataList.add(eventData);
		}
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.lister);
	}
}
