package com.rrockathon.artcollective;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.usergrid.android.client.Client;
import org.usergrid.android.client.entities.Entity;
import org.usergrid.android.client.response.ApiResponse;
import android.util.Log;

public class MessageController {

	private String USERGRID_API_URL = "https://api.usergrid.com";
	private String USERGRID_APP = "rrcokathon/artcollective";
	private Client client = null;

	public MessageController() {

		client = new Client(USERGRID_APP).withApiUrl(USERGRID_API_URL);

	}

	public ApiResponse login(String usernameArg, String passwordArg) {
		ApiResponse response = null;
		try {
			response = client.authorizeAppUser(usernameArg, passwordArg);
			Log.i("login success", "login success");
		} catch (Exception e) {
			e.printStackTrace();
			response = null;
		}
		return response;
	}

	public List<Entity> getEvents() {
		ApiResponse resp = null;
		try {
			resp = client.apiRequest(HttpMethod.GET, null, null, USERGRID_APP,
					"testevents");
		} catch (Exception e) {
			resp = null;
		}
		return resp.getEntities();
	}
}
