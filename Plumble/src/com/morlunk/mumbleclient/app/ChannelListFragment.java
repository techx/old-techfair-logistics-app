package com.morlunk.mumbleclient.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.service.model.User;

/**
 * The main connection view.
 *
 * The state of this activity depends closely on the state of the underlying
 * MumbleService. When the activity is started it can't really do anything else
 * than initialize its member variables until it has acquired a reference to the
 * MumbleService.
 *
 * Once the MumbleService reference has been acquired the activity is in one of
 * the three states:
 * <dl>
 * <dt>Connecting to server
 * <dd>MumbleService has just been started and ChannelList should wait until the
 * connection has been established. In this case the ChannelList should be very
 * careful as it doesn't have a visible channel and the Service doesn't have a
 * current channel.
 *
 * <dt>Connected to server
 * <dd>When the Activity is resumed during an established Mumble connection it
 * has connection immediately available and is free to act freely.
 *
 * <dt>Disconnecting or Disconnected
 * <dd>If the ChannelList is resumed after the Service has been disconnected the
 * List should exit immediately.
 * </dl>
 *
 * NOTE: Service enters 'Connected' state when it has received and processed
 * server sync message. This means that at this point the service should be
 * fully initialized.
 *
 * And just so the state wouldn't be too easy the connection can be cancelled.
 * Disconnecting the service is practically synchronous operation. Intents
 * broadcast by the Service aren't though. This means that after the ChannelList
 * disconnects the service it might still have some unprocessed intents queued
 * in a queue. For this reason all intents that require active connection must
 * take care to check that the connection is still alive.
 *
 * @author pcgod, Rantanen
 *
 */
public class ChannelListFragment extends SherlockFragment {	
	
	/**
	 * The parent activity MUST implement ChannelProvider. An exception will be thrown otherwise.
	 */
	private ChannelProvider channelProvider;
	
	private ListView channelUsersList;
	private UserListAdapter usersAdapter;
	private TextView noUsersText;

	/**
	 * Updates the users display with the data from the channelProvider.
	 */
	public void updateChannel() {
		// We need to make sure the fragment has been attached and is shown before updating the users.
		if(isVisible()) {
			usersAdapter.setVisibleChannel(channelProvider.getChannel().id);
			usersAdapter.setUsers(channelProvider.getChannelUsers());
			usersAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Updates the user specified in the users adapter.
	 * @param user
	 */
	public void updateUser(User user) {
		usersAdapter.refreshUser(user);
	}
	
	/**
	 * Removes the user from the channel list.
	 * @param user
	 */
	public void removeUser(User user) {
		usersAdapter.removeUser(user.session);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.channel_list, container, false);

		// Get the UI views
		channelUsersList = (ListView) view.findViewById(R.id.channelUsers);
		noUsersText = (TextView) view.findViewById(R.id.noUsersText);
		
		return view;
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			channelProvider = (ChannelProvider)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()+" must implement ChannelProvider!");
		}
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		usersAdapter = new UserListAdapter(getActivity(), channelUsersList, null);
		channelUsersList.setAdapter(usersAdapter);
		registerForContextMenu(channelUsersList);
		channelUsersList.setEmptyView(noUsersText);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.channel_list_context, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		User user = (User) usersAdapter.getItem(info.position);
		
		switch (item.getItemId()) {
		case R.id.menu_local_mute_item:
			user.localMuted = !user.localMuted;
			usersAdapter.notifyDataSetChanged();
			return true;
		}
		return false;
	};
}
