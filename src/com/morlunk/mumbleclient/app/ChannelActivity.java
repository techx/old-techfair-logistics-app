package com.morlunk.mumbleclient.app;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.Settings;
import com.morlunk.mumbleclient.service.BaseServiceObserver;
import com.morlunk.mumbleclient.service.IServiceObserver;
import com.morlunk.mumbleclient.service.model.Channel;
import com.morlunk.mumbleclient.service.model.Message;
import com.morlunk.mumbleclient.service.model.User;


/**
 * An interface for the activity that manages the channel selection.
 * @author andrew
 *
 */
interface ChannelProvider {
	public Channel getChannel();
	public List<User> getChannelUsers();
	public void sendChannelMessage(String message);
}

public class ChannelActivity extends ConnectedActivity implements ChannelProvider {

	public static final String JOIN_CHANNEL = "join_channel";
	public static final String SAVED_STATE_VISIBLE_CHANNEL = "visible_channel";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

	private Channel visibleChannel;
	private ChannelSpinnerAdapter channelAdapter;

	private ProgressDialog mProgressDialog;
	
	// Fragments
	private ChannelListFragment listFragment;
	private ChannelChatFragment chatFragment;

	// Menu items
	private MenuItem recordItem;
	
	private Settings settings;
	
	public final DialogInterface.OnClickListener onDisconnectConfirm = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					mService.disconnect();
				}
			}).start();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		settings = new Settings(this);
		
		// Use theme from settings
		int theme = 0;
		if(settings.getTheme().equals(Settings.ARRAY_THEME_LIGHTDARK)) {
			theme = R.style.Theme_Sherlock_Light_DarkActionBar;
		} else if(settings.getTheme().equals(Settings.ARRAY_THEME_DARK)) {
			theme = R.style.Theme_Sherlock;
		}
		setTheme(theme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
		
		setVolumeControlStream(settings.getAudioStream());
		
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        if(savedInstanceState != null) {
        	final Channel channel = (Channel) savedInstanceState.getParcelable(SAVED_STATE_VISIBLE_CHANNEL);

			// Channel might be null if we for example caused screen rotation
			// while still connecting.
			if (channel != null) {
				this.visibleChannel = channel;
			}
			
			if(savedInstanceState.containsKey(ChannelListFragment.class.getName()) &&
					savedInstanceState.containsKey(ChannelChatFragment.class.getName())) {
				// Load existing fragments
				listFragment = (ChannelListFragment) getSupportFragmentManager().getFragment(savedInstanceState, ChannelListFragment.class.getName());
				chatFragment = (ChannelChatFragment) getSupportFragmentManager().getFragment(savedInstanceState, ChannelChatFragment.class.getName());
			} else {
		        // Create fragments
		        listFragment = new ChannelListFragment();
		        chatFragment = new ChannelChatFragment();
			}
			
        } else {
	        // Create fragments
	        listFragment = new ChannelListFragment();
	        chatFragment = new ChannelChatFragment();
		}
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        /*
         * Removed tab code as you are unable to have both tabs and list navigation modes. Use pager only for now.
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        
        */
    }
    
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	getSupportFragmentManager().putFragment(outState, ChannelListFragment.class.getName(), listFragment);
    	getSupportFragmentManager().putFragment(outState, ChannelChatFragment.class.getName(), chatFragment);
		outState.putParcelable(SAVED_STATE_VISIBLE_CHANNEL, visibleChannel);
    }
    
    /* (non-Javadoc)
     * @see com.morlunk.mumbleclient.app.ConnectedActivity#onResume()
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if(recordItem != null && mService != null) {
    		recordItem.setIcon(mService.isRecording() ? R.drawable.microphone : R.drawable.microphone_muted);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_channel, menu);
        
        // Store recording icon to adjust for recording changes
        recordItem = menu.findItem(R.id.menu_talk_item);
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
		case R.id.menu_talk_item:
			mService.setRecording(!mService.isRecording());
			item.setIcon(mService.isRecording() ? R.drawable.microphone : R.drawable.microphone_muted);
			return true;
		case R.id.menu_disconnect_item:
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					mService.disconnect();
					
				}
			}).start();
			return true;
		}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			final AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("Disconnect");
			b.setMessage("Are you sure you want to disconnect from Mumble?");
			b.setPositiveButton(android.R.string.yes, onDisconnectConfirm);
			b.setNegativeButton(android.R.string.no, null);
			b.show();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
    
    /**
	 * Handles activity initialization when the Service has connected.
	 *
	 * Should be called when there is a reason to believe that the connection
	 * might have became valid. The connection MUST be established but other
	 * validity criteria may still be unfilled such as server synchronization
	 * being complete.
	 *
	 * The method implements the logic required for making sure that the
	 * Connected service is in such a state that it fills all the connection
	 * criteria for ChannelList.
	 *
	 * The method also takes care of making sure that its initialization code
	 * is executed only once so calling it several times doesn't cause problems.
	 */
	@Override
	protected void onConnected() {
		// We are now connected! \o/
		
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		List<Channel> channelList = mService.getChannelList();
		channelAdapter = new ChannelSpinnerAdapter(channelList);
		getSupportActionBar().setListNavigationCallbacks(channelAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(final int itemPosition, long itemId) {
				new AsyncTask<Channel, Void, Void>() {

					@Override
					protected Void doInBackground(Channel... params) {
						mService.joinChannel(params[0].id);
						return null;
					}
					
					/* (non-Javadoc)
					 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
					 */
					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						setChannel(channelAdapter.getItem(itemPosition));
					}
				}.execute(channelAdapter.getItem(itemPosition));
				return true;
			}
		});
		
		// If we don't have visible channel selected, default to the current channel.
		// Setting channel also synchronizes the UI so we don't need to do it manually.
		//
		// TODO: Resync channel if current channel was visible on pause.
		// Currently if the user is moved to another channel while this activity
		// is paused the channel isn't updated when the activity resumes.
		if (visibleChannel == null) {
			setChannel(mService.getCurrentChannel());
		} else {
			// Re-select visible channel. Necessary after a rotation is
			// performed or the app is suspended.
			if (channelList.contains(visibleChannel)) {
				getSupportActionBar().setSelectedNavigationItem(
						channelList.indexOf(visibleChannel));
			}
		}
		
		final List<Message> messages = mService.getMessageList();
		for (final Message m : messages) {
			chatFragment.addMessage(m);
		}
	}

	/**
	 * Handles activity initialization when the Service is connecting.
	 */
	@Override
	protected void onConnecting() {
		showProgressDialog(R.string.connectionProgressConnectingMessage);
	}

	@Override
	protected void onSynchronizing() {
		showProgressDialog(R.string.connectionProgressSynchronizingMessage);
	}
	
	/* (non-Javadoc)
	 * @see com.morlunk.mumbleclient.app.ConnectedActivity#createServiceObserver()
	 */
	@Override
	protected IServiceObserver createServiceObserver() {
		return new ChannelServiceObserver();
	}
	
	private void showProgressDialog(final int message) {
		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialog.show(
				ChannelActivity.this,
				getString(R.string.connectionProgressTitle),
				getString(message),
				true,
				false,
				new OnCancelListener() {
					@Override
					public void onCancel(final DialogInterface dialog) {
						mService.disconnect();
						mProgressDialog.setMessage(getString(R.string.connectionProgressDisconnectingMessage));
					}
				});
		} else {
			mProgressDialog.setMessage(getString(message));
		}
	}
	
	public void setChannel(Channel channel) {
		this.visibleChannel = channel;
		listFragment.updateChannel();
	}
	
	/* (non-Javadoc)
	 * @see com.morlunk.mumbleclient.app.ChannelProvider#getChannel()
	 */
	@Override
	public Channel getChannel() {
		return visibleChannel;
	}
	
	/* (non-Javadoc)
	 * @see com.morlunk.mumbleclient.app.ChannelProvider#getChannelUsers()
	 */
	@Override
	public List<User> getChannelUsers() {
		return mService.getUserList();
	}
	
	/* (non-Javadoc)
	 * @see com.morlunk.mumbleclient.app.ChannelProvider#sendChannelMessage(java.lang.String)
	 */
	@Override
	public void sendChannelMessage(String message) {
		mService.sendChannelTextMessage(
				message,
				visibleChannel);
	}
	
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
    	
        public SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public SherlockFragment getItem(int i) {
        	switch (i) {
			case 0:
				return listFragment;
			case 1:
				return chatFragment;
			default:
				return null;
			}
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.title_section1).toUpperCase();
                case 1: return getString(R.string.title_section2).toUpperCase();
            }
            return null;
        }
    }

    class ChannelServiceObserver extends BaseServiceObserver {
		@Override
		public void onMessageReceived(final Message msg) throws RemoteException {
			chatFragment.addMessage(msg);
		}

		@Override
		public void onMessageSent(final Message msg) throws RemoteException {
			chatFragment.addMessage(msg);
		}
		
		@Override
		public void onCurrentChannelChanged() throws RemoteException {
		}

		@Override
		public void onCurrentUserUpdated() throws RemoteException {
		}

		@Override
		public void onUserAdded(final User user) throws RemoteException {
			refreshUser(user);
		}

		@Override
		public void onUserRemoved(final User user) throws RemoteException {
			listFragment.removeUser(user);
		}

		@Override
		public void onUserUpdated(final User user) throws RemoteException {
			refreshUser(user);
		}

		private void refreshUser(final User user) {
			listFragment.updateUser(user);
		}
	};
    
class ChannelSpinnerAdapter implements SpinnerAdapter {
		
		List<Channel> availableChannels;
		
		public ChannelSpinnerAdapter(List<Channel> availableChannels) {
			this.availableChannels = availableChannels;
		}
		
		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return availableChannels.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Channel getItem(int arg0) {
			return availableChannels.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemViewType(int)
		 */
		@Override
		public int getItemViewType(int arg0) {
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view = arg1;
			if(arg1 == null) {
				view = getLayoutInflater().inflate(R.layout.sherlock_spinner_dropdown_item, arg2, false);
			}
			
			TextView spinnerTitle = (TextView) view.findViewById(android.R.id.text1);
			spinnerTitle.setText(getItem(arg0).name+" ("+getItem(arg0).userCount+")");
			spinnerTitle.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
			
			return view;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getViewTypeCount()
		 */
		@Override
		public int getViewTypeCount() {
			return 1;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			return false;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return false;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
		 */
		@Override
		public void registerDataSetObserver(DataSetObserver arg0) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
		 */
		@Override
		public void unregisterDataSetObserver(DataSetObserver arg0) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see android.widget.SpinnerAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getView(position, convertView, parent);
		}
		
	}
}
