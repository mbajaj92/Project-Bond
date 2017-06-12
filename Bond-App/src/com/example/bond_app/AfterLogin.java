package com.example.bond_app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import ServerSideCode.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AfterLogin extends Activity {

	private String userId;
	private Socket test_socket;
	private DataOutputStream out;
	private DataInputStream in;
	private NotificationManager nManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_after_login);

		Intent receive = getIntent();
		userId = receive.getExtras().getString(ClientUtils.USER_ID_CLIENT);
		final TextView result = (TextView) findViewById(R.id.result);
		Handler handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				String displayString = result.getText().toString();
				if (msg.what == 1) {
					displayString = "\n Search Reply : \n" + msg.obj.toString();
				} else if (msg.what == 2) {
					displayString = "\n Spy Reply : \n" + msg.obj.toString();
				}
				result.setText(displayString);
			}
		};
		nManager = new NotificationManager(userId, handler);
		nManager.start();

		Button search = (Button) findViewById(R.id.search);
		Button registertoken = (Button) findViewById(R.id.registertoken);
		Button addgroup = (Button) findViewById(R.id.addgroup);
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i1 = new Intent(getBaseContext(), Query.class);
				i1.putExtra(ClientUtils.USER_ID_CLIENT, userId);
				i1.putExtra("action", "search");
				startActivity(i1);
			}
		});
		registertoken.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i2 = new Intent(getBaseContext(), Query.class);
				i2.putExtra(ClientUtils.USER_ID_CLIENT, userId);
				i2.putExtra("action", "registerToken");
				startActivity(i2);
			}
		});
		addgroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i3 = new Intent(getBaseContext(), Query.class);
				i3.putExtra(ClientUtils.USER_ID_CLIENT, userId);
				i3.putExtra("action", "addGroup");
				startActivity(i3);
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.optionsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.logoff) {
			JSONObject json = new JSONObject();
			try {
				json.put(Utils.USER_ID, userId);
				json.put(Utils.PACKET_TYPE, Utils.LOGOFF);
				test_socket = new Socket(ClientUtils.SERVER_NAME, ClientUtils.SERVER_PORT_NUMBER);
				out = new DataOutputStream(test_socket.getOutputStream());
				in = new DataInputStream(test_socket.getInputStream());
				out.writeUTF(json.toString());
				out.flush();
				json = new JSONObject(in.readUTF());
				nManager.stopListening();
				finish();
				return true;
			} catch (JSONException j) {
				j.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return super.onOptionsItemSelected(item);
	}

}
