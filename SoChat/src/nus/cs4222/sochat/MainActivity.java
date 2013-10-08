package nus.cs4222.sochat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nus.cs4222.sochat.LocalService.LocalBinder;
import nus.dtn.api.fwdlayer.ForwardingLayerException;
import nus.dtn.api.fwdlayer.MessageListener;
import nus.dtn.util.DtnMessage;
import nus.dtn.util.DtnMessageException;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	NearbyListAdapter adapter;
	NearbyFragment nearby;
	BroadcastFragment broadcast;
	FriendFragment friend;
	Menu menu;
	boolean mBound = false;
	LocalService mService;
	// boolean ToastFlag=false;
	// Handler handler;
	Dialog dialog;
	User currentUser;

	PacketListener listener;
	private LocalService service;
	private LocalBinder binder;
	public TreeSet<User> neighbour;
	/** Defines callbacks for service binding, passed to bindService() */

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder b) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			binder = (LocalBinder) b;
			service = binder.getService();
			mBound = true;
			listener = new PacketListener();

			try {

				binder.addListener(listener);
				Log.d("sochat", "Main listener register");

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	public ArrayList<User> filterGender(boolean gender) {
		ArrayList<User> Result = new ArrayList<User>();
		// true for male false for female
		TreeSet<User> newNeighbour = new TreeSet<User>();

		while (!neighbour.isEmpty()) {
			User temp = neighbour.pollFirst();
			if (gender == temp.getGender())
				Result.add(temp);
			newNeighbour.add(temp);
		}

		neighbour = newNeighbour;

		return Result;
	}

	public ArrayList<User> filterOther(String target) {
		ArrayList<User> Result = new ArrayList<User>();
		// true for male false for female
		TreeSet<User> newNeighbour = new TreeSet<User>();
		target = target.toUpperCase();

		while (!neighbour.isEmpty()) {
			User temp = neighbour.pollFirst();
			String gender;
			if (temp.getGender())
				gender = "MALE";
			else
				gender = "FEMALE";
			if (temp.getName().toUpperCase().contains(target))
				Result.add(temp);
			else if (String.valueOf(temp.getAge()).toUpperCase()
					.contains(target))
				Result.add(temp);
			else if (temp.getGroup().toUpperCase().contains(target))
				Result.add(temp);
			else if (temp.getStatus().toUpperCase().contains(target))
				Result.add(temp);
			else if (temp.getBirthdayString().toUpperCase().contains(target))
				Result.add(temp);
			else if (gender.toUpperCase().contains(target))
				Result.add(temp);

			newNeighbour.add(temp);
		}

		neighbour = newNeighbour;

		return Result;
	}

	protected File saveFile(DtnMessage message, User user) {
		File copiedFile = null;
		try {
			Log.d("content", "swich to receive");
			String recName = message.readString();
			if (recName != null) {
				Log.d("content", recName);
				File recFile = message.getFile(0);
				Log.d("content", recFile.length() + "");
				Log.d("content", recFile.exists() ? "file exists"
						: "not exists");
				String recDirectory = ((SochatApplication) getApplication())
						.getAppFolderPath();
				Log.d("content", recDirectory);
				copiedFile = new File(recDirectory, recName);
				user.setProfileImagePath(copiedFile.getAbsolutePath());

				FileChannel fc1 = null, fc2 = null;
				try {
					fc1 = new FileInputStream(recFile).getChannel();
					fc2 = new FileOutputStream(copiedFile).getChannel();
					fc2.transferFrom(fc1, 0, fc1.size());
				} finally {
					// Must always close a file in the finally block
					try {
						if (fc1 != null)
							fc1.close();
						if (fc2 != null)
							fc2.close();
					} catch (Exception e) {
						// Nothing can be done about this
					}
				}
			}
		} catch (Exception e) {
			// Log the exception
			Log.e("SoChatApp", "Exception on message event", e);
			// Tell the user

		}
		return copiedFile;

	}

	public class PacketListener implements MessageListener {
		public void onMessageReceived(String source, String destination,
				final DtnMessage message) {

			try {
				Log.d("sochat", "Main listener called");

				message.switchToData();
				String ID;
				int type = -1;
				int code;

				ID = message.readString();

				type = message.readInt();
				code = message.readInt();
				
				String sender = message.readString();
				String IMEI = message.readString();
				Log.d("sochat", "received is" + type + " " + "code");
				
				
				
				if (type == 2) { // receive request
					Log.d("sochat", "2222222222222222222222");
					if (code == 1) {
						Log.d("sochat", "1111111111111111111");
						User discovere_user = User.UserWith(
								message.readString(), message.readString(),
								message.readString(), message.readBoolean(),
								message.readString(), message.readString());

						Log.d("sochat", discovere_user.toString());

						saveFile(message, discovere_user);
						neighbour.add(discovere_user);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Log.d("Sochat", "refresh neighbour list called");
								nearby.refreshNearByList(neighbour);

							}
						});
						// binder.getNeighbour
					}

				}

				if (type == 3) {
					Context context = getApplicationContext();
					
					String userTo = IMEI;
					String currentUserID = ((SochatApplication) getApplication())
							.getCurrentUser().getID();
					Conversation conversation = Conversation.fromStorage(
							currentUserID, userTo);
					// if first time
					if (conversation == null) {
						Log.d("Sochat", "Conversation null");
						conversation = new Conversation(currentUserID, userTo);
					} else {
						Log.d("Sochat", "Conversation NOT null");
						conversation.addComment(new OneComment(true, message
								.readString(),ID));
						conversation.toStorage();
					}
					CharSequence text = "You got a new message from " + sender
							+ "!";

					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					// if(!ToastFlag)
					toast.show();

				}
				if (type == 4) {
					Context context = getApplicationContext();
				
					String userTo = IMEI;
					String currentUserID = ((SochatApplication) getApplication())
							.getCurrentUser().getID();
					Conversation conversation = Conversation.fromStorage(
							currentUserID, userTo);
					OneComment comment = null;
					// if first time
					CharSequence text = "";
					if (code == 1) {
						text = "You got a new picture from " + sender + "!";
						comment = new OneComment(true, null, saveFile(message)
								.getAbsolutePath(), null,ID);
					} else if (code == 2) {
						text = "You got a new recording from " + sender + "!";
						comment = new OneComment(true, null, null, saveFile(
								message).getAbsolutePath(),ID);
					}
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					// if(!ToastFlag)
					toast.show();

					if (conversation == null) {
						Log.d("Sochat", "Conversation null");
						conversation = new Conversation(currentUserID, userTo);
					} else {
						Log.d("Sochat", "Conversation NOT null");
						conversation.addComment(comment);
						conversation.toStorage();
					}

				}

				if (type == 5) {

				
					String group = message.readString();
					String content = message.readString();
					if (code == 0) {
						// set broadcast GUI

						if (mViewPager.getCurrentItem() != 0) {
							Context context = getApplicationContext();
							String userTo = group;
							String currentUserID = ((SochatApplication) getApplication())
									.getCurrentUser().getID();
							Conversation conversation = Conversation
									.fromStorage(currentUserID, userTo);
							// if first time
							if (conversation == null) {
								Log.d("Sochat", "Conversation null");
								conversation = new Conversation(currentUserID,
										userTo);
							} else {
								Log.d("Sochat", "Conversation NOT null");
								conversation.addComment(new OneComment(true,
										message.readString(),ID));
								conversation.toStorage();
							}
							CharSequence text = "You got a new message from "
									+ sender + "!";

							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(context, text,
									duration);
							// if(!ToastFlag)
							toast.show();
						} else {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// Log.d("Sochat",
									// "refresh neighbour list called");

								}
							});

						}

					}

					if (code == 1) {
						if (group == ((SochatApplication) getApplication())
								.getCurrentUser().getGroup()) {

							if (mViewPager.getCurrentItem() != 0) {
								Context context = getApplicationContext();
								String userTo = group;
								String currentUserID = ((SochatApplication) getApplication())
										.getCurrentUser().getID();
								Conversation conversation = Conversation
										.fromStorage(currentUserID, userTo);
								// if first time
								if (conversation == null) {
									Log.d("Sochat", "Conversation null");
									conversation = new Conversation(
											currentUserID, userTo);
								} else {
									Log.d("Sochat", "Conversation NOT null");
									conversation.addComment(new OneComment(
											true, message.readString(),ID));
									conversation.toStorage();
								}
								CharSequence text = "You got a new message from "
										+ sender + "!";

								int duration = Toast.LENGTH_SHORT;

								Toast toast = Toast.makeText(context, text,
										duration);
								// if(!ToastFlag)
								toast.show();
							} else {

								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// Log.d("Sochat",
										// "refresh neighbour list called");

									}
								});

							}// end else
						}

					}

				}

			} catch (DtnMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	protected File saveFile(DtnMessage message) {
		File copiedFile = null;
		try {
			Log.d("content", "swich to receive");
			String recName = message.readString();
			Log.d("content", recName);
			File recFile = message.getFile(0);
			Log.d("content", recFile.length() + "");
			Log.d("content", recFile.exists() ? "file exists" : "not exists");
			String recDirectory = ((SochatApplication) getApplication())
					.getAppFolderPath();
			Log.d("content", recDirectory);
			copiedFile = new File(recDirectory, recName);
			FileChannel fc1 = null, fc2 = null;
			try {
				fc1 = new FileInputStream(recFile).getChannel();
				fc2 = new FileOutputStream(copiedFile).getChannel();
				fc2.transferFrom(fc1, 0, fc1.size());
			} finally {
				// Must always close a file in the finally block
				try {
					if (fc1 != null)
						fc1.close();
					if (fc2 != null)
						fc2.close();
				} catch (Exception e) {
					// Nothing can be done about this
				}
			}
		} catch (Exception e) {
			// Log the exception
			Log.e("SoChatApp", "Exception on message event", e);
			// Tell the user

		}
		return copiedFile;
	}

//	protected void onStop() {
//		super.onStop();
//		Log.d("Sochat", "Chat stopped");
//		// Save conversation
//
//		// Unbind from the service
//		if (mBound) {
//			try {
//				binder.removeListener(listener);
//				unbindService(mConnection);
//
//				mBound = false;
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Log.d("Sochat", "Chat service unbinded");
//			
//		}
//	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.activity_main);

		User curUser = ((SochatApplication) getApplication()).getCurrentUser();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		nearby = new NearbyFragment();
		broadcast = new BroadcastFragment();
		friend = new FriendFragment();

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Check if there is a user or not
		SochatApplication sochatApp = (SochatApplication) getApplication();
		currentUser = sochatApp.getCurrentUser();
		bindService(new Intent(this, LocalService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		if (currentUser == null) {
			// if no, then directed to login page
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		} else {
			setTitle(currentUser.getName());
		}
		if (mBound) {
			binder.refreshUser();
		}
	}

	public void settingClicked(MenuItem v) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		if (tab.getPosition() == 1) {
			neighbour = new TreeSet<User>();
			binder.neighbourDiscover();
			nearby.refreshNearByList(neighbour);
		} else if (tab.getPosition() == 2) {
			friend.refreshFriendList(neighbour);
		}
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = broadcast;
				break;
			case 1:
				fragment = nearby;
				adapter = nearby.adapter;
				break;
			case 2:
				fragment = friend;
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);

			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			Log.d("Load dummy", "Load dummy");
			return rootView;
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d("sochat", "Pause");
		if (mBound) {
			try {
				binder.removeListener(listener);
				unbindService(mConnection);

				mBound = false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}

	}

	public void onFriendItemClicked(View v) {
		FriendListAdapter friendAdapter = friend.getAdapter();
		User clickedUser = friendAdapter.getItem(Integer.valueOf((String) v
				.getTag()));
		if (User.isNoUser(clickedUser)) {
			return;
		}
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(EXTRA_MESSAGE, clickedUser.getID());
		intent.putExtra("name", clickedUser.getName());

		startActivity(intent);
	}

	public void onNearbyItemClicked(View v) {
		NearbyListAdapter nearbyAdapter = nearby.getAdapter();
		User clickedUser = nearbyAdapter.getNearbyUserList().get(
				Integer.valueOf((String) v.getTag()));
		if (User.isNoUser(clickedUser)) {
			return;
		}
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(EXTRA_MESSAGE, clickedUser.getID());
		intent.putExtra("name", clickedUser.getName());
		startActivity(intent);
	}

	public void onFollowClicked(View v) {
		Button btn = (Button) findViewById(R.id.nearby_follow_btn);
		Drawable image = getResources().getDrawable( R.drawable.favorite_after );
		image.setBounds( 0, 0, 60, 60 );
		btn.setCompoundDrawables( null, null, null, image );
		int position = Integer.valueOf((String) v.getTag());
		FriendList f = FriendList.fromStorage(currentUser.getID());
		if (f == null) {
			f = new FriendList(currentUser.getID());
		}
		User followUser = nearby.getAdapter().getItem(position);
		f.addFriend(followUser);
		btn.setText("Following");
		btn.setEnabled(false);
	}

	public void onUnfollowClicked(View v) {
		Button btn = (Button) v.findViewById(R.id.friend_unfollow_btn);
		Drawable image = getResources().getDrawable( R.drawable.favorite);
		image.setBounds( 0, 0, 60, 60 );
		btn.setCompoundDrawables( null, null, null, image );
		int position = Integer.valueOf((String) v.getTag());
		FriendList f = FriendList.fromStorage(currentUser.getID());
		if (f == null) {
			f = new FriendList(currentUser.getID());
		}
		User unfollowUser = friend.getAdapter().getItem(position);
		f.removeFriend(unfollowUser);
		btn.setText("Unfollowed");
		btn.setEnabled(false);
	}

	public void onConfigureGroupClicked(View v) {
		dialog = new Dialog(this);
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.group_configure_dialog);
		dialog.setTitle("Choose Your Channel Name");
		EditText textField = (EditText) dialog
				.findViewById(R.id.group_setting_text);

		Button confirmBtn = (Button) dialog
				.findViewById(R.id.group_btn_confirm);
		Button cancelBtn = (Button) dialog.findViewById(R.id.group_btn_cancel);

		confirmBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				EditText t = (EditText) dialog
						.findViewById(R.id.group_setting_text);
				String input = t.getText().toString().toLowerCase();
				Matcher ma = Pattern.compile("(?:[^a-z0-9 ]|(?<=['\"])s)")
						.matcher(input);
				if (input.trim().equalsIgnoreCase("")
						|| input.trim().length() > 15
						|| input.trim().contains(" ") || ma.find()) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Please check your input", 2);
					toast.show();
				} else {
					((SochatApplication) getApplication()).setGroup(input);
					broadcast.refreshGroupList();
					dialog.dismiss();
				}
			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});

		String group = currentUser.getGroup();
		if (group != null && !group.trim().equalsIgnoreCase("")) {
			textField.setText(group);
		}
		dialog.show();
	}
}
