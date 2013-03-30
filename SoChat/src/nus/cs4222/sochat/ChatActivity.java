package nus.cs4222.sochat;

import java.util.Random;

import nus.cs4222.sochat.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class ChatActivity extends Activity {
	private DiscussArrayAdapter adapter;
	private ListView lv;
	private EditText editText1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		// Show the Up button in the action bar.
		setupActionBar();

		new Random();

		lv = (ListView) findViewById(R.id.chatList);
		adapter = new DiscussArrayAdapter(getApplicationContext(),
				R.layout.listitem_chat);
		lv.setAdapter(adapter);

		Intent intent = getIntent();
		String message = intent.getStringExtra(NearbyFragment.EXTRA_MESSAGE);

		editText1 = (EditText) findViewById(R.id.editText1);
		editText1.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					adapter.add(new OneComment(false, editText1.getText()
							.toString()));
					editText1.setText("");
					return true;
				}
				return false;
			}
		});
		// Randomly generate some text
		addItems();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addItems() {
		adapter.add(new OneComment(true, "Hello bubbles!"));
		adapter.add(new OneComment(false, "Hello bubbles!"));
	}

}
