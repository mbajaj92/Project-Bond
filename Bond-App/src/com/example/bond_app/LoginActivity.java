package com.example.bond_app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("NewApi")
public class LoginActivity extends Activity {
	private Socket test_socket;
	private DataOutputStream out;
	private DataInputStream in;
	private EditText userId;
	private EditText password;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		Button b = (Button) findViewById(R.id.login);
		userId = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String servername = ClientUtils.SERVER_NAME;
					int port = ClientUtils.SERVER_PORT_NUMBER;
					JSONObject packet = new JSONObject();
					packet.put(ClientUtils.PACKET_TYPE, ClientUtils.LOGIN);
					String user_ID = userId.getText().toString();
					packet.put(ClientUtils.USER_ID, user_ID);
					packet.put(ClientUtils.PASSWORD, password.getText().toString());

					test_socket = new Socket(servername, port);
					out = new DataOutputStream(test_socket.getOutputStream());
					in = new DataInputStream(test_socket.getInputStream());
					out.writeUTF(packet.toString());
					out.flush();
					packet = new JSONObject(in.readUTF());
					if (packet.getString(ClientUtils.AUTHENTICATION).equals(ClientUtils.WELCOME)) {
						Intent intent = new Intent(getBaseContext(), AfterLogin.class);
						intent.putExtra(ClientUtils.USER_ID, user_ID);
						startActivity(intent);
						finish();
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
