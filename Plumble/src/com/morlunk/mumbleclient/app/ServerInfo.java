package com.morlunk.mumbleclient.app;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.techfair.tabletapp.R;

public class ServerInfo extends SherlockActivity {
	
	public void save() {
		final EditText nameEdit = (EditText) findViewById(R.id.serverNameEdit);
		final EditText hostEdit = (EditText) findViewById(R.id.serverHostEdit);
		final EditText portEdit = (EditText) findViewById(R.id.serverPortEdit);
		final EditText usernameEdit = (EditText) findViewById(R.id.serverUsernameEdit);
		final EditText passwordEdit = (EditText) findViewById(R.id.serverPasswordEdit);

		final String name = (nameEdit).getText().toString().trim();
		final String host = (hostEdit).getText().toString().trim();

		int port;
		try {
			port = Integer.parseInt((portEdit).getText().toString());
		} catch (final NumberFormatException ex) {
			port = 64738;
		}

		final String username = (usernameEdit).getText().toString().trim();
		final String password = (passwordEdit).getText().toString();

		final DbAdapter db = new DbAdapter(this);

		db.open();
		final long serverId = ServerInfo.this.getIntent().getLongExtra(
				"serverId", -1);
		if (serverId != -1) {
			db.updateServer(serverId, name, host, port, username, password);
		} else {
			db.createServer(name, host, port, username, password);
		}
		db.close();

		finish();
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_add);
		

		final long serverId = this.getIntent().getLongExtra("serverId", -1);
		if (serverId != -1) {
			final EditText nameEdit = (EditText) findViewById(R.id.serverNameEdit);
			final EditText hostEdit = (EditText) findViewById(R.id.serverHostEdit);
			final EditText portEdit = (EditText) findViewById(R.id.serverPortEdit);
			final EditText usernameEdit = (EditText) findViewById(R.id.serverUsernameEdit);
			final EditText passwordEdit = (EditText) findViewById(R.id.serverPasswordEdit);

			final DbAdapter db = new DbAdapter(this);
			db.open();
			final Cursor c = db.fetchServer(serverId);
			nameEdit.setText(c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_NAME)));
			hostEdit.setText(c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST)));
			portEdit.setText(Integer.toString(c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT))));
			usernameEdit.setText(c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_USERNAME)));
			passwordEdit.setText(c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PASSWORD)));
			c.close();
			db.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save_button:
			save();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_server_info, menu);
		return true;
	}
}
