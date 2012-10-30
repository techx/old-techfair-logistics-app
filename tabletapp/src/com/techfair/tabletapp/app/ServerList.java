package com.techfair.tabletapp.app;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.techfair.tabletapp.Globals;
import com.techfair.tabletapp.R;
import com.techfair.tabletapp.service.BaseServiceObserver;
import com.techfair.tabletapp.service.MumbleService;

/**
 * The main server list activity.
 *
 * Shows a list of servers and allows connecting to these. Also provides
 * ways to start creating and editing servers.
 *
 * @author pcgod
 *
 */
public class ServerList extends ConnectedListActivity {
	private class ServerAdapter extends BaseAdapter {
		private final Context context;
		private final Cursor cursor;

		public ServerAdapter(final Context context_, final DbAdapter dbAdapter_) {
			context = context_;
			cursor = dbAdapter_.fetchAllServers();
			startManagingCursor(cursor);
		}

		@Override
		public final int getCount() {
			return cursor.getCount();
		}

		@Override
		public final Object getItem(final int position) {
			return getItemId(position);
		}

		@Override
		public final long getItemId(final int position) {
			cursor.moveToPosition(position);
			return cursor.getLong(cursor.getColumnIndexOrThrow(DbAdapter.SERVER_COL_ID));
		}

		@Override
		public final View getView(
			final int position,
			final View v,
			final ViewGroup parent) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View row = inflater.inflate(
				android.R.layout.simple_list_item_2,
				null);

			final TextView nameText = (TextView) row.findViewById(android.R.id.text1);
			final TextView userText = (TextView) row.findViewById(android.R.id.text2);

			cursor.moveToPosition(position);

			final String serverName = cursor.getString(cursor.getColumnIndexOrThrow(DbAdapter.SERVER_COL_NAME));
			final String serverHost = cursor.getString(cursor.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST));
			final int serverPort = cursor.getInt(cursor.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT));
			final String serverUsername = cursor.getString(cursor.getColumnIndexOrThrow(DbAdapter.SERVER_COL_USERNAME));

			if ("".equals(serverName)) {
				nameText.setText(serverHost + ":" + serverPort);
				userText.setText(serverUsername);
			} else {
				nameText.setText(serverName);
				userText.setText(serverUsername + "@" + serverHost + ":" +
								 serverPort);
			}

			return row;
		}
	}

	private class ServerServiceObserver extends BaseServiceObserver {
		@Override
		public void onConnectionStateChanged(final int state)
			throws RemoteException {
			checkConnectionState();
		}
	}

	long serverToDeleteId = -1;
	DbAdapter dbAdapter;

	private static final int ACTIVITY_ADD_SERVER = 0;
	private static final int ACTIVITY_CHANNEL_LIST = 1;
	private static final int DIALOG_DELETE_SERVER = 0;

	private static final int MENU_ADD_SERVER = Menu.FIRST;
	private static final int MENU_EDIT_SERVER = Menu.FIRST + 1;
	private static final int MENU_DELETE_SERVER = Menu.FIRST + 2;
	private static final int MENU_EXIT = Menu.FIRST + 3;
	private static final int MENU_CONNECT_SERVER = Menu.FIRST + 4;
	private static final int MENU_PREFERENCES = Menu.FIRST + 5;

	private static final String STATE_WAIT_CONNECTION = "org.pcgod.mumbleclient.ServerList.WAIT_CONNECTION";

	private ServerServiceObserver mServiceObserver;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case MENU_CONNECT_SERVER:
			onListItemClick(
				getListView(),
				getCurrentFocus(),
				info.position,
				getListAdapter().getItemId(info.position));
			return true;
		case MENU_EDIT_SERVER:
			editServer(getListAdapter().getItemId(info.position));
			return true;
		case MENU_DELETE_SERVER:
			serverToDeleteId = info.id;
			showDialog(DIALOG_DELETE_SERVER);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public final void onCreateContextMenu(
		final ContextMenu menu,
		final View v,
		final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final int menuPosition = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
		final int serverId = (int) getListView().getItemIdAtPosition(
			menuPosition);
		final Cursor c = dbAdapter.fetchServer(serverId);
		final String name = getServerName(c);
		c.close();
		menu.setHeaderTitle(name);

		menu.add(0, MENU_CONNECT_SERVER, 1, "Connect").setIcon(
			android.R.drawable.ic_menu_view);
		menu.add(0, MENU_EDIT_SERVER, 1, "Edit").setIcon(
			android.R.drawable.ic_menu_edit);
		menu.add(0, MENU_DELETE_SERVER, 1, "Delete").setIcon(
			android.R.drawable.ic_menu_delete);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.activity_server_list, menu);
		return true;
	}
	
	@Override
	public final boolean onMenuItemSelected(
		final int featureId,
		final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_server_item:
			addServer();
			return true;
		case R.id.menu_preferences:
			final Intent prefs = new Intent(this, Preferences.class);
			startActivity(prefs);
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void addServer() {
		final Intent i = new Intent(this, ServerInfo.class);
		startActivityForResult(i, ACTIVITY_ADD_SERVER);
	}

	/**
	 * Monitors the connection state after clicking a server entry.
	 */
	private final boolean checkConnectionState() {
		switch (mService.getConnectionState()) {
		case MumbleService.CONNECTION_STATE_CONNECTING:
		case MumbleService.CONNECTION_STATE_SYNCHRONIZING:
		case MumbleService.CONNECTION_STATE_CONNECTED:
			unregisterConnectionReceiver();
			final Intent i = new Intent(this, ChannelActivity.class);
			startActivityForResult(i, ACTIVITY_CHANNEL_LIST);
			return true;
		case MumbleService.CONNECTION_STATE_DISCONNECTED:
			// TODO: Error message checks.
			// This can be reached if the user leaves ServerList after clicking
			// server but before the connection intent reaches the service.
			// In this case the service connects and can be disconnected before
			// the connection state is checked again.
			Log.i(Globals.LOG_TAG, "ServerList: Disconnected");
			break;
		default:
			Assert.fail("Unknown connection state");
		}

		return false;
	}

	private Dialog createDeleteServerDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sureDeleteServer).setCancelable(
			false).setPositiveButton(
			"Yes",
			new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					if (serverToDeleteId > 0) {
						dbAdapter.deleteServer(serverToDeleteId);
						serverToDeleteId = -1;
						fillList();
						Toast.makeText(
							ServerList.this,
							R.string.server_deleted,
							Toast.LENGTH_SHORT).show();
					}
				}
			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});

		return builder.create();
	}

	private void editServer(final long id) {
		final Intent i = new Intent(this, ServerInfo.class);
		i.putExtra("serverId", id);
		startActivityForResult(i, ACTIVITY_ADD_SERVER);
	}

	private String getServerName(final Cursor c) {
		String name = c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_NAME));
		if ("".equals(name)) {
			final String host = c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST));
			final int port = c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT));
			name = host + ":" + port;
		}
		return name;
	}

	private void registerConnectionReceiver() {
		if (mServiceObserver != null) {
			return;
		}

		mServiceObserver = new ServerServiceObserver();

		if (mService != null) {
			mService.registerObserver(mServiceObserver);
		}
	}

	private void unregisterConnectionReceiver() {
		if (mServiceObserver == null) {
			return;
		}

		if (mService != null) {
			mService.unregisterObserver(mServiceObserver);
		}

		mServiceObserver = null;
	}

	/**
	 * Starts connecting to a server.
	 *
	 * @param id
	 */
	protected final void connectServer(final long id) {
		final Cursor c = dbAdapter.fetchServer(id);
		final int serverId = c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_ID));
		final String host = c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_HOST));
		final int port = c.getInt(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PORT));
		final String username = c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_USERNAME));
		final String password = c.getString(c.getColumnIndexOrThrow(DbAdapter.SERVER_COL_PASSWORD));
		c.close();

		registerConnectionReceiver();

		final Intent connectionIntent = new Intent(this, MumbleService.class);
		connectionIntent.setAction(MumbleService.ACTION_CONNECT);
		connectionIntent.putExtra(MumbleService.EXTRA_SERVER_ID, serverId);
		connectionIntent.putExtra(MumbleService.EXTRA_HOST, host);
		connectionIntent.putExtra(MumbleService.EXTRA_PORT, port);
		connectionIntent.putExtra(MumbleService.EXTRA_USERNAME, username);
		connectionIntent.putExtra(MumbleService.EXTRA_PASSWORD, password);
		startService(connectionIntent);
	}

	@Override
	protected final void onActivityResult(
		final int requestCode,
		final int resultCode,
		final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillList();
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.main);
		
		registerForContextMenu(getListView());

		// Create the service observer. If such exists, onServiceBound will
		// register it.
		if (savedInstanceState != null) {
			mServiceObserver = new ServerServiceObserver();
		}

		dbAdapter = new DbAdapter(this);
		dbAdapter.open();

		fillList();
	}

	@Override
	protected final Dialog onCreateDialog(final int id) {
		Dialog d;
		switch (id) {
		case DIALOG_DELETE_SERVER:
			d = createDeleteServerDialog();
			break;
		default:
			d = null;
		}
		return d;
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();

		dbAdapter.close();
	}

	@Override
	protected void onDisconnected() {
		// Suppress the default disconnect behavior.
	}

	@Override
	protected final void onListItemClick(
		final ListView l,
		final View v,
		final int position,
		final long id) {
		super.onListItemClick(l, v, position, id);

		connectServer(id);
	}

	@Override
	protected void onPause() {
		unregisterConnectionReceiver();
		super.onPause();
	}
	
	/* (non-Javadoc)
	 * @see com.techfair.tabletapp.app.ConnectedListActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mService != null && mService.getConnectionState() == MumbleService.CONNECTION_STATE_CONNECTED) {
			// If already connected, just jump to channel list.
			startActivity(new Intent(this, ChannelActivity.class));
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mServiceObserver != null) {
			outState.putBoolean(STATE_WAIT_CONNECTION, true);
		}
	}

	@Override
	protected void onServiceBound() {
		if (mServiceObserver != null) {
			if (!checkConnectionState()) {
				mService.registerObserver(mServiceObserver);
			}
		}
	}

	void fillList() {
		setListAdapter(new ServerAdapter(this, dbAdapter));
	}
}
