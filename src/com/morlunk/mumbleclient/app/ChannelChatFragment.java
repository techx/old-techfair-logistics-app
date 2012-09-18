package com.morlunk.mumbleclient.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.service.BaseServiceObserver;
import com.morlunk.mumbleclient.service.model.Message;

public class ChannelChatFragment extends Fragment {

	private class ChatServiceObserver extends BaseServiceObserver {
		@Override
		public void onMessageReceived(final Message msg) throws RemoteException {
			addMessage(msg);
		}

		@Override
		public void onMessageSent(final Message msg) throws RemoteException {
			addMessage(msg);
		}
	}

	private ChannelProvider channelProvider;
	private TextView chatText;
	private EditText chatTextEdit;

	private static final int MENU_CLEAR = Menu.FIRST;

	private final OnEditorActionListener chatTextEditActionEvent = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(
			final TextView v,
			final int actionId,
			final KeyEvent event) {
			if (event != null && !event.isShiftPressed() && v != null) {
				final View focus = v.focusSearch(View.FOCUS_RIGHT);
				if (focus != null) {
					focus.requestFocus();
					return true;
				}
				return false;
			}

			if (actionId == EditorInfo.IME_ACTION_SEND) {
				if (v != null) {
					sendMessage(v);
				}
				return true;
			}
			return true;
		}
	};

	private final OnClickListener sendOnClickEvent = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			sendMessage(chatTextEdit);
		}
	};
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chat_view, container, false);
		chatText = (TextView) view.findViewById(R.id.chatText);
		chatText.setMovementMethod(LinkMovementMethod.getInstance());
		chatTextEdit = (EditText) view.findViewById(R.id.chatTextEdit);
		chatTextEdit.setOnEditorActionListener(chatTextEditActionEvent);
		view.findViewById(R.id.send_button).setOnClickListener(sendOnClickEvent);
		updateText();
		return view;
	}

	void addMessage(final Message msg) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(DateUtils.formatDateTime(
			getActivity(),
			msg.timestamp,
			DateUtils.FORMAT_SHOW_TIME));
		sb.append("]");

		if (msg.direction == Message.DIRECTION_SENT) {
			sb.append("To ");
			sb.append(msg.channel.name);
		} else {
			if (msg.channelIds > 0) {
				sb.append("(C) ");
			}
			if (msg.treeIds > 0) {
				sb.append("(T) ");
			}

			if (msg.actor != null) {
				sb.append(msg.actor.name);
			} else {
				sb.append("Server");
			}
		}
		sb.append(": ");
		sb.append(msg.message);
		sb.append("<br>");
		
		Spanned htmlString = Html.fromHtml(sb.toString());
		chatText.append(htmlString);
	}

	void sendMessage(final TextView v) {
		String text = v.getText().toString();
		AsyncTask<String, Void, Void> messageTask = new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... params) {
				channelProvider.sendChannelMessage(params[0]);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				v.setText("");
			}
		};
		messageTask.execute(text);
	}

	void updateText() {
		chatText.beginBatchEdit();
		chatText.setText("");
//		for (final String s : ServerList.client.chatList) {
//			chatText.append(s);
//		}
//		chatText.endBatchEdit();
//		chatText.post(new Runnable() {
//			@Override
//			public void run() {
//				chatText.scrollTo(0, chatText.getHeight());
//			}
//		});
	}
	
	public void clear() {
		if(chatText != null) {
			updateText();
		}
	}
}
