package com.techfair.tabletapp.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.techfair.tabletapp.R;
import com.techfair.tabletapp.service.model.Message;

public class ChannelChatFragment extends SherlockFragment {

	private ChannelProvider channelProvider;
	private TextView chatText;
	private EditText chatTextEdit;

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
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Clear chat text. It'll reload from the data source.
		chatText.setText(null);
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
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append("[");
		sb.append(DateUtils.formatDateTime(
			getActivity(),
			msg.timestamp,
			DateUtils.FORMAT_SHOW_TIME));
		sb.append("] ");

		if (msg.direction == Message.DIRECTION_SENT) {
			sb.append("To ");
			sb.append(msg.channel.name);
			sb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.abs__holo_blue_light)), sb.length()-msg.channel.name.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			if (msg.channelIds > 0) {
				sb.append("(C) ");
			}
			if (msg.treeIds > 0) {
				sb.append("(T) ");
			}

			String actorName = msg.actor.name;
			
			if(actorName == null)
				actorName = "Server";
			
			sb.append(actorName);
			sb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.abs__holo_blue_light)), sb.length()-actorName.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		sb.append(": ");
		sb.append(Html.fromHtml(msg.message));
		sb.append(Html.fromHtml("<br>"));
		
		chatText.append(sb);
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
