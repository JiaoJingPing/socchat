package nus.cs4222.sochat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import nus.cs4222.sochat.BaseAlbumDirFactory;
import nus.cs4222.sochat.FroyoAlbumDirFactory;

import nus.cs4222.sochat.AlbumStorageDirFactory;

import nus.cs4222.sochat.R;
import nus.cs4222.sochat.LocalService.LocalBinder;
import nus.dtn.api.fwdlayer.ForwardingLayerException;
import nus.dtn.api.fwdlayer.MessageListener;
import nus.dtn.util.DtnMessage;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class BroadcastActivity extends Activity {

	// IMPORTANT
	private String groupTo;
	private Conversation conversation;

	private DiscussArrayAdapter adapter;
	private ListView lv;
	private EditText editText1;
	private ImageButton sendVoice;
	private ImageButton sendImage;

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	private final int CameraActionCode = 999;
	private final int GalleryActionCode = 998;
	private String mCurrentPhotoPath;
	private String mCurrentVoicePath;
	Dialog dialog;
	private MediaRecorder voiceRecorder;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
	private static final String AUDIO_RECORDER_FOLDER = "Sochat";
	private int currentFormat = 0;
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
			MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4,
			AUDIO_RECORDER_FILE_EXT_3GP };

	private static boolean isClicked = false;

	// Service
	boolean mBound = false;
	PacketListener listener;
	private LocalService service;
	private LocalBinder binder;

	/** Defines callbacks for service binding, passed to bindService() */

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d("Sochat", "Chat created");
		setContentView(R.layout.activity_chat);

		// Show the Up button in the action bar.
		setupActionBar();

		bindService(new Intent(this, LocalService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		adapter = new DiscussArrayAdapter(getApplicationContext(),
				R.layout.listitem_text_left);
		lv = (ListView) findViewById(R.id.chatList);

		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				OneComment clickedComment = adapter.getItem(position);
				// If contains picture, enlarge the picture
				if (clickedComment.getPicture_dir() != null
						&& !clickedComment.getPicture_dir()
								.equalsIgnoreCase("")) {
					if (dialog == null) {
						dialog = new Dialog(BroadcastActivity.this);
						dialog.setCancelable(true);
						dialog.setContentView(R.layout.photo_dialog);
						dialog.setTitle("Photo");
					}
					ImageView image = (ImageView) dialog
							.findViewById(R.id.photo_dialog_image);
					setPicToView(image, clickedComment.getPicture_dir());
					dialog.show();
				}
				// If contains voice play the voice
				if (clickedComment.getVoice_dir() != null
						&& !clickedComment.getVoice_dir().equalsIgnoreCase("")) {
					AudioManager audioManager = (AudioManager) getSystemService(ChatActivity.AUDIO_SERVICE);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 50,
							0);
					final ImageView voicePic = (ImageView) view
							.findViewById(R.id.chat_voice_image);
					voicePic.setImageResource(R.drawable.voice_on);
					MediaPlayer mp = new MediaPlayer();
					try {
						mp.setDataSource(clickedComment.getVoice_dir());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						mp.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mp.setOnCompletionListener(new OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							voicePic.setImageResource(R.drawable.voice);
						}

					});
					mp.start();
				} // End voice

			}
		});

		// Setup voice button
		sendVoice = (ImageButton) findViewById(R.id.btn_chat_sendVoice);
		sendVoice.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					stopRecording();
					// Add voice
					String ID = "abc";
					OneComment comment = new OneComment(false, editText1
							.getText().toString(), ID);
					comment.setVoice_dir(mCurrentVoicePath);
					adapter.add(comment);

					if (groupTo.compareTo("all") == 0) {
						binder.sendAllRecord(mCurrentVoicePath);
					} else {
						binder.sendGroupRecord(mCurrentVoicePath);
					}

					break;
				case MotionEvent.ACTION_DOWN:
					startRecording();
					break;
				default:
					break;
				}
				return false;
			}
		});

		// Setup image button
		// Add listener for sendImage button
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		sendImage = (ImageButton) findViewById(R.id.btn_chat_sendImage);
		sendImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isClicked) {
					createPopUpWindow();
					isClicked = true;
				}
			}
		});

		editText1 = (EditText) findViewById(R.id.chat_EditText);
		editText1.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					String content = editText1.getText().toString();

					if (content != null && !content.trim().equalsIgnoreCase("")) {
						if (groupTo.compareTo("all") == 0) {
							binder.broadcast(content);
							Log.d("SOCHat", "broad sent");
						} else {
							binder.groupBroadcast(content);
							Log.d("SOCHat", "group sent");
						}
						String ID = "ABC";
						adapter.add(new OneComment(false, editText1.getText()
								.toString(), ID));
						editText1.setText("");

					}

					return true;
				}
				return false;
			}
		});

		// Get from the list of user around
		Intent intent = getIntent();
		groupTo = intent.getStringExtra(NearbyFragment.EXTRA_MESSAGE);
		String groupToName = intent.getStringExtra("name");
		this.setTitle(groupToName);
		String currentUserID = ((SochatApplication) getApplication())
				.getCurrentUser().getID();
		conversation = Conversation.fromStorage(currentUserID, groupTo);
		// if first time

		if (conversation == null) {
			conversation = new Conversation(currentUserID, groupTo);
		} else {
			for (OneComment c : conversation.getComments()) {
				adapter.add(c);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("Sochat", "Chat stopped");
		// Save conversation
		conversation.setComments(adapter.getComments());
		conversation.toStorage();

		// Unbind from the service
		if (mBound) {
			try {
				binder.removeListener(listener);

			} catch (ForwardingLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d("Sochat", "Chat service unbinded");
			unbindService(mConnection);

			mBound = false;
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d("Sochat", "Chat destoryed");
		if (mBound) {
			try {
				binder.removeListener(listener);
				unbindService(mConnection);

				mBound = false;
			} catch (ForwardingLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d("Sochat", "Chat service unbinded");

		}
	}

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

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			Log.d("sochat", "broadcast onServiceDisconnected");
		}
	};

	public class PacketListener implements MessageListener {
		public void onMessageReceived(String source, String destination,
				final DtnMessage message) {
			try {
				message.switchToData();
				final String ID;
				int type = -1;
				int code;
				final String content;

				ID = message.readString();
				type = message.readInt();
				code = message.readInt();
				Log.d("mmmm", "" + code + " " + type);
				final String sender = message.readString();
				String IMEI = message.readString();
				boolean duplicate = false;
				OneComment temp = new OneComment(true, "", ID + sender);
				// if(adapter.getComments().contains(temp)){
				// duplicate = true;
				//
				// }

				if (adapter.getComments() != null
						&& adapter.getComments().size() > 0)
					for (OneComment one : adapter.getComments()) {
						if (one.equals(temp)) {
							duplicate = true;
							Log.d("soc", temp.getID() + " " + one.getID());
						}

					}

				if (!duplicate) {
					if (type == 1) { // receive request

					}
					if (type == 2) { // receive Announce

					}
					if (type == 3) { // chat

						content = message.readString();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								adapter.add(new OneComment(true, content, ID
										+ sender));
							}
						});

					}
					if (type == 4) { // receive files
						if (code == 1) {// send picture
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									OneComment comment = new OneComment(true,
											editText1.getText().toString(), ID
													+ sender);
									comment.setPicture_dir(saveFile(message)
											.getAbsolutePath());
									adapter.add(comment);

								}
							});

						}
						if (code == 2) // receive recorders
						{
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									OneComment comment = new OneComment(true,
											editText1.getText().toString(), ID
													+ sender);
									comment.setVoice_dir(saveFile(message)
											.getAbsolutePath());
									adapter.add(comment);
									;// will return a saved receive file

								}
							});

						}
						if (code == 3) // receive other files
						{
							saveFile(message);// will return a saved receive
												// file
						}
					}

					if (type == 5) {

						String group = message.readString();
						final String contentBoardcast = message.readString();
						String myGroup = ((SochatApplication) getApplication())
								.getCurrentUser().getGroup();

						Log.d("kkkk", "" + code + " my  " + myGroup + "  "
								+ group + "  " + groupTo);

						if (code == 0 && groupTo.compareTo("all") == 0) { // broadcast
																			// +
																			// broadcast
																			// page

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									adapter.add(new OneComment(true, sender
											+ "says: " + contentBoardcast, ID
											+ sender));
								}
							});
						}

						else if (code == 0 && groupTo.compareTo("all") != 0) { // broadcast
																				// +
																				// not
																				// in
																				// broadcast
																				// page
							// save to file
							Context context = getApplicationContext();
							String userTo = groupTo;
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
										contentBoardcast, ID + sender));
								conversation.toStorage();
							}
							CharSequence text = "You got a new broadcast message from "
									+ sender + "!";

							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(context, text,
									duration);
							// if(!ToastFlag)
							toast.show();

						}

						if (code == 1 && myGroup.compareTo(group) == 0
								&& groupTo.compareTo("all") == 0) {

							// save to file
							Context context = getApplicationContext();
							String userTo = groupTo;
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
										contentBoardcast, ID + sender));
								conversation.toStorage();
							}
							CharSequence text = "You got a new group message from "
									+ sender + "!";

							int duration = Toast.LENGTH_SHORT;
							Log.d("soccccccc", "toast");
							Toast toast = Toast.makeText(context, text,
									duration);
							// if(!ToastFlag)
							toast.show();

						}

						else if (code == 1 && myGroup.compareTo(group) == 0
								&& groupTo.compareTo("all") != 0) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									adapter.add(new OneComment(true, sender
											+ "says: " + contentBoardcast, ID
											+ sender));
								}
							});

						}

					}// end type

					if (type == 6) {

						String group = message.readString();
						String myGroup = ((SochatApplication) getApplication())
								.getCurrentUser().getGroup();

						Log.d("llll", "" + code + " my  " + myGroup + "  "
								+ group + "  " + groupTo);

						if (code == 1 && groupTo.compareTo("all") == 0) {

							// save to file
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									OneComment comment = new OneComment(true,
											editText1.getText().toString(), ID
													+ sender);
									comment.setPicture_dir(saveGroupFile(
											message).getAbsolutePath());
									adapter.add(comment);

								}
							});

						}// if all

						else if (code == 1 && groupTo.compareTo("all") != 0) {
							String userTo = groupTo;
							String currentUserID = ((SochatApplication) getApplication())
									.getCurrentUser().getID();
							Conversation conversation = Conversation
									.fromStorage(currentUserID, userTo);
							OneComment comment = null;
							// if first time
							CharSequence text = "";
							text = "You got a new picture from " + sender + "!";
							comment = new OneComment(true, null, saveGroupFile(
									message).getAbsolutePath(), null, ID
									+ sender);
							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(
									getApplicationContext(), text, duration);
							// if(!ToastFlag)
							toast.show();

							if (conversation == null) {
								Log.d("Sochat", "Conversation null");
								conversation = new Conversation(currentUserID,
										userTo);
							} else {
								Log.d("Sochat", "Conversation NOT null");
								conversation.addComment(comment);
								conversation.toStorage();
							}

						}

						if (code == 2 && groupTo.compareTo("all") == 0) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									OneComment comment = new OneComment(true,
											editText1.getText().toString(), ID
													+ sender);
									comment.setVoice_dir(saveGroupFile(message)
											.getAbsolutePath());
									adapter.add(comment);
									;// will return a saved receive file

								}
							});
						}

						else if (code == 2 && groupTo.compareTo("all") != 0) {
							String userTo = groupTo;
							String currentUserID = ((SochatApplication) getApplication())
									.getCurrentUser().getID();
							Conversation conversation = Conversation
									.fromStorage(currentUserID, userTo);
							OneComment comment = null;
							// if first time
							CharSequence text = "";
							text = "You got a new recording from " + sender
									+ "!";
							comment = new OneComment(true, null, null,
									saveGroupFile(message).getAbsolutePath(),
									ID + sender);

							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(
									getApplicationContext(), text, duration);
							// if(!ToastFlag)
							toast.show();

							if (conversation == null) {
								Log.d("Sochat", "Conversation null");
								conversation = new Conversation(currentUserID,
										userTo);
							} else {
								Log.d("Sochat", "Conversation NOT null");
								conversation.addComment(comment);
								conversation.toStorage();
							}

						}

						if (code == 3 && myGroup.compareTo(group) == 0
								&& groupTo.compareTo("all") == 0) {

							// save to file
							String userTo = groupTo;
							String currentUserID = ((SochatApplication) getApplication())
									.getCurrentUser().getID();
							Conversation conversation = Conversation
									.fromStorage(currentUserID, userTo);
							OneComment comment = null;
							// if first time
							CharSequence text = "";
							text = "You got a new picture from " + sender + "!";
							comment = new OneComment(true, null, saveGroupFile(
									message).getAbsolutePath(), null, ID
									+ sender);
							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(
									getApplicationContext(), text, duration);
							// if(!ToastFlag)
							toast.show();

							if (conversation == null) {
								Log.d("Sochat", "Conversation null");
								conversation = new Conversation(currentUserID,
										userTo);
							} else {
								Log.d("Sochat", "Conversation NOT null");
								conversation.addComment(comment);
								conversation.toStorage();
							}

						}

						else if (code == 3 && myGroup.compareTo(group) == 0
								&& groupTo.compareTo("all") != 0) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									OneComment comment = new OneComment(true,
											editText1.getText().toString(), ID
													+ sender);
									comment.setPicture_dir(saveGroupFile(
											message).getAbsolutePath());
									adapter.add(comment);

								}
							});

						}

						if (code == 4 && myGroup.compareTo(group) == 0
								&& groupTo.compareTo("all") == 0) {

							// save to file
							String userTo = groupTo;
							String currentUserID = ((SochatApplication) getApplication())
									.getCurrentUser().getID();
							Conversation conversation = Conversation
									.fromStorage(currentUserID, userTo);
							OneComment comment = null;
							// if first time
							CharSequence text = "";
							text = "You got a new recording from " + sender
									+ "!";
							comment = new OneComment(true, null, null,
									saveGroupFile(message).getAbsolutePath(),
									ID + sender);

							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(
									getApplicationContext(), text, duration);
							// if(!ToastFlag)
							toast.show();

							if (conversation == null) {
								Log.d("Sochat", "Conversation null");
								conversation = new Conversation(currentUserID,
										userTo);
							} else {
								Log.d("Sochat", "Conversation NOT null");
								conversation.addComment(comment);
								conversation.toStorage();
							}

						}// if all

						else if (code == 4 && myGroup.compareTo(group) == 0
								&& groupTo.compareTo("all") != 0) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									OneComment comment = new OneComment(true,
											editText1.getText().toString(), ID
													+ sender);
									comment.setVoice_dir(saveGroupFile(message)
											.getAbsolutePath());
									adapter.add(comment);
									;// will return a saved receive file

								}
							});

						}

					}

				}
			} catch (Exception e) {

				Log.e("sochat", "Exception on message event", e);

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

	protected File saveGroupFile(DtnMessage message) {
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

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}

	private String getVoiceFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
	}

	private void startRecording() {
		voiceRecorder = new MediaRecorder();
		voiceRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		voiceRecorder.setOutputFormat(output_formats[currentFormat]);
		voiceRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mCurrentVoicePath = getVoiceFilename();
		voiceRecorder.setOutputFile(mCurrentVoicePath);
		try {
			voiceRecorder.prepare();
			voiceRecorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void stopRecording() {
		if (null != voiceRecorder) {
			voiceRecorder.stop();
			voiceRecorder.reset();
			voiceRecorder.release();
			voiceRecorder = null;
		}
	}

	private void createPopUpWindow() {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = layoutInflater.inflate(R.layout.photo_popup, null);
		final PopupWindow popupWindow = new PopupWindow(popupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		Button btnDismiss = (Button) popupView
				.findViewById(R.id.popup_photo_cancel);
		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
				isClicked = false;
			}
		});

		Button btnCamera = (Button) popupView
				.findViewById(R.id.popup_photo_camera);
		btnCamera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent(CameraActionCode);
				popupWindow.dismiss();
				isClicked = false;
			}
		});
		Button btnGallery = (Button) popupView
				.findViewById(R.id.popup_photo_gallery);
		btnGallery.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				isClicked = false;
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, GalleryActionCode);
				popupWindow.dismiss();
			}
		});
		popupWindow.showAtLocation(lv, Gravity.BOTTOM, 0, -130);

	}

	private void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			takePictureIntent
					.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(takePictureIntent, actionCode);
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			mCurrentPhotoPath = null;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CameraActionCode && resultCode == RESULT_OK) {
			// Camera intent
			handleBigCameraPhoto();
		} else if (requestCode == GalleryActionCode && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			// String picturePath contains the path of selected Image
			String ID = "123";
			OneComment comment = new OneComment(false, editText1.getText()
					.toString(), ID);
			comment.setPicture_dir(picturePath);
			adapter.add(comment);

			if (groupTo.compareTo("all") == 0) {
				binder.sendAllPicture(picturePath);

			} else {
				binder.sendGroupPicture(picturePath);

			}

		}
	}

	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			OneComment comment = new OneComment(false, editText1.getText()
					.toString(), "asd");
			comment.setPicture_dir(mCurrentPhotoPath);
			// Log.d("sochat",mCurrentPhotoPath);
			adapter.add(comment);
			galleryAddPic();
			if (groupTo.compareTo("all") == 0) {
				binder.sendAllPicture(mCurrentPhotoPath);

			} else {
				binder.sendGroupPicture(mCurrentPhotoPath);

			}
			mCurrentPhotoPath = null;
		}

	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);

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

	private void setPicToView(ImageView mImageView, String mCurrentPhotoPath) {
		// Get the dimensions of the View
		int targetW = 200;
		int targetH = 500;

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		mImageView.setImageBitmap(bitmap);
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}
}
