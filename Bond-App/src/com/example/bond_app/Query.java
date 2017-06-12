package com.example.bond_app;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Query extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query);
		Intent receive = getIntent();
		final String userId = receive.getExtras().getString(ClientUtils.USER_ID);
		final String action = receive.getExtras().getString("action");
		final EditText query = (EditText) findViewById(R.id.query);
		final EditText gLink = (EditText) findViewById(R.id.gLink);
		final EditText gName = (EditText) findViewById(R.id.gName);

		if (!action.equals("addGroup")) {
			//RelativeLayout rl = (RelativeLayout) findViewById(R.id.parentRelative);
			gLink.setVisibility(View.INVISIBLE);
			gName.setVisibility(View.INVISIBLE);
			//rl.removeView(gName);
			if (action.equals("search")) {
				query.setHint("ENTER SEARCH QUERY HERE");
			} else if (action.equals("registerToken")) {
				query.setHint("ENTER TOKENS HERE");
			}
		} else {
			query.setHint("ENTER GROUP DESCRIPTION HERE");
		}

		Button b = (Button) findViewById(R.id.enter);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String queryText = query.getText().toString();
				if (action.equals("search")) {
					handleSearch(queryText, userId);
				} else if (action.equals("registerToken")) {
					registerToken(queryText, userId);
				} else if (action.equals("addGroup")) {
					String gLinkText = gLink.getText().toString();
					String gNameText = gName.getText().toString();
					addGroup(gLinkText, gNameText, queryText, userId);
				}
				finish();

			}
		});

	}

	private void handleSearch(String text, String userId) {
		try {
			JSONObject json = new JSONObject();
			json.put(ClientUtils.USER_ID, userId);
			json.put(ClientUtils.PACKET_TYPE, ClientUtils.SEARCH);
			json.put(ClientUtils.SEARCH_QUERY, text);
			ClientUtils.sendmessage(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void registerToken(String text, String userId) {
		try {
			JSONObject json = null;
			json = new JSONObject();
			json.put(ClientUtils.USER_ID, userId);
			json.put(ClientUtils.PACKET_TYPE, ClientUtils.REGISTER_TOKEN);
			json.put(ClientUtils.TOKENS, text);
			ClientUtils.sendmessage(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void addGroup(String groupLink, String groupName, String groupDesc, String userId) {
		try {
			JSONObject json = null;
			json = new JSONObject();
			json.put(ClientUtils.USER_ID, userId);
			json.put(ClientUtils.PACKET_TYPE, ClientUtils.ADD_GROUP);
			json.put(ClientUtils.GROUP_LINK, groupLink);
			json.put(ClientUtils.GROUP_NAME, groupName);
			json.put(ClientUtils.GROUP_DESC, groupDesc);
			ClientUtils.sendmessage(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
