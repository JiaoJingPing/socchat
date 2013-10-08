package nus.cs4222.sochat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nus.dtn.api.fwdlayer.ForwardingLayerException;
import nus.dtn.api.fwdlayer.ForwardingLayerInterface;
import nus.dtn.api.fwdlayer.ForwardingLayerProxy;
import nus.dtn.api.fwdlayer.MessageListener;
import nus.dtn.middleware.api.DtnMiddlewareInterface;
import nus.dtn.middleware.api.DtnMiddlewareProxy;
import nus.dtn.middleware.api.MiddlewareEvent;
import nus.dtn.middleware.api.MiddlewareException;
import nus.dtn.middleware.api.MiddlewareListener;
import nus.dtn.util.Descriptor;
import nus.dtn.util.DtnMessage;
import nus.dtn.util.DtnMessageException;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class LocalService extends Service {
	private NotificationManager mNM;
	private DtnMiddlewareInterface middleware;
	private ForwardingLayerInterface fwdLayer;
	private Descriptor descriptor;
	private Handler handler;
	private PacketListener messageListener;
	private User user;
	private boolean isBound;
	private File directory;
	private Object lock = new Object();
	

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	void sendMessage(final String receiver, final int type, final int code,
			final String chatMessage) {
		user = ((SochatApplication) getApplication()).getCurrentUser();
		final String sender = user.getName();
		final String IMEI = user.getID();

		Thread sendThread = new Thread() {
			public void run() {
				DtnMessage message = new DtnMessage();
				// Data part
				try {
					message.addData();
					message.writeString(
							String.valueOf(System.currentTimeMillis()))
							.writeInt(type).writeInt(code).writeString(sender)
							.writeString(IMEI).writeString(chatMessage);
					Log.d("chatMessage", chatMessage);

					// Log.d("Type", type + "");
					// Log.d("code", code + "");
				} catch (DtnMessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Chat message
					// Send the message using the multihop interface
				try {
					fwdLayer.sendMessage(descriptor, message, receiver, null);
				} catch (ForwardingLayerException e) {
					e.printStackTrace();
				}
			}
		};
		sendThread.start();
	}

	void sendGroupMessage(final String receiver, final int type,
			final int code, final String chatMessage, boolean isBroadcast) {
		user = ((SochatApplication) getApplication()).getCurrentUser();
		final String sender = user.getName();
		final String IMEI = user.getID();
		final String group;
		if (isBroadcast)
			group = "all";
		else
			group = user.getGroup();
		Log.d("kk",group);

		Thread sendThread = new Thread() {
			public void run() {
				DtnMessage message = new DtnMessage();
				// Data part
				try {
					message.addData();
					message.writeString(
							String.valueOf(System.currentTimeMillis()))
							.writeInt(type).writeInt(code).writeString(sender)
							.writeString(IMEI).writeString(group)
							.writeString(chatMessage);
					Log.d("chatMessage", chatMessage);

					// Log.d("Type", type + "");
					// Log.d("code", code + "");
				} catch (DtnMessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Chat message
					// Send the message using the multihop interface
				try {
					fwdLayer.sendMessage(descriptor, message, receiver, null);
				} catch (ForwardingLayerException e) {
					e.printStackTrace();
				}
			}
		};
		sendThread.start();
	}

	void sendGroupFile(final String path, final int type, final int code,
			 boolean isBroadcast) {
		final File photoFile = new File(path);
		user = ((SochatApplication) getApplication()).getCurrentUser();
		final String sender = user.getName();
		final String IMEI = user.getID();
		
		final String group;
		if (isBroadcast)
			group = "all";
		else
			group = user.getGroup();
		
		Thread sendThread = new Thread() {
			public void run() {

				try {

					// Create a file object for the photo selected
					String photoName = photoFile.getName();

					// Construct the DTN message
					DtnMessage message = new DtnMessage();
					// Data part
					message.addData();
					message.writeString(
							String.valueOf(System.currentTimeMillis()))
							.writeInt(type).writeInt(code).writeString(sender)
							.writeString(IMEI).writeString(group).writeString(photoName);
					message.addFile(photoFile);

					// Broadcast the message using the fwd layer interface
					fwdLayer.sendMessage(descriptor, message, "everyone", null);

					// Tell the user that the message has been sent

				} catch (Exception e) {
					// Log the exception
					Log.e("PhotoSharingApp", "Exception while sending photo", e);
					// Inform the user

				}
			}
		};
		sendThread.start();

	}
	
	void sendFile(final String path, final int type, final int code,
			final String receiver) {
		final File photoFile = new File(path);
		user = ((SochatApplication) getApplication()).getCurrentUser();
		final String sender = user.getName();
		final String IMEI = user.getID();
		Thread sendThread = new Thread() {
			public void run() {

				try {

					// Create a file object for the photo selected
					String photoName = photoFile.getName();

					// Construct the DTN message
					DtnMessage message = new DtnMessage();
					// Data part
					message.addData();
					message.writeString(
							String.valueOf(System.currentTimeMillis()))
							.writeInt(type).writeInt(code).writeString(sender)
							.writeString(IMEI).writeString(photoName);
					message.addFile(photoFile);

					// Broadcast the message using the fwd layer interface
					fwdLayer.sendMessage(descriptor, message, receiver, null);

					// Tell the user that the message has been sent

				} catch (Exception e) {
					// Log the exception
					Log.e("PhotoSharingApp", "Exception while sending photo", e);
					// Inform the user

				}
			}
		};
		sendThread.start();

	}
	
	
	

	public class LocalBinder extends Binder {

		public PacketListener getPktListener() {
			return messageListener;
		}

		public void setUser(User u) {
			user = u;
		}

		public void sendMessageTo(final String receiver, final String content) {
			sendMessage(receiver, 3, 0, content);
		}

		public void broadcast(String comment) {
			sendGroupMessage("everyone", 5, 0, comment, true);

		}

		public void groupBroadcast(String comment) {
			sendGroupMessage("everyone", 5, 1, comment, false);

		}

		public void addListener(MessageListener activityListener)
				throws ForwardingLayerException, InterruptedException {
			final MessageListener listener = activityListener;
			Thread waitThread = new Thread() {
				public void run() {
					Log.d("sochat", "wait");
					synchronized (lock) {
						if (descriptor == null) {
							Log.d("sochat", "waiting");
							Log.d("sochat", fwdLayer == null ? "ffff null"
									: "ffff not null");
							try {
								lock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Log.d("sochat", "waited");
						}

					}
					Log.d("sochat", descriptor == null ? "des null"
							: "des not null");
					try {
						fwdLayer.addMessageListener(descriptor, listener);
					} catch (ForwardingLayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("haha", "123haha");

				}
			};
			// flag_lock1 = true;
			waitThread.start();
			// android.os.SystemClock.sleep(900);
			// fwdLayer.addMessageListener(descriptor, listener);

		}

		public void removeListener(MessageListener activityListener)
				throws ForwardingLayerException {

			fwdLayer.removeMessageListener(descriptor, activityListener);
		}

		public void setBound(boolean flag) {

			isBound = flag; // set the bound/unbound
		}

		void sendPicture(final String path, final String receiver) {
			sendFile(path, 4, 1, receiver);
		}

		void sendRecord(final String path, final String receiver) {
			sendFile(path, 4, 2, receiver);
		}

		void sendOtherfile(final String path, final String receiver) {
			sendFile(path, 4, 3, receiver);
		}
		
	
		
		void sendGroupPicture(final String path){
			sendGroupFile(path, 6, 3,false);
			
		}
		void sendGroupRecord(final String path){
			sendGroupFile(path, 6, 4,false);
			
		}
		void sendAllPicture(final String path){
			sendGroupFile(path, 6, 1,true);
			
		}
		void sendAllRecord(final String path){
			sendGroupFile(path, 6, 2,true);
			
		}
		
		

		public void neighbourDiscover() { // send neighbour discover file
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			final String ID = telephonyManager.getDeviceId();

			// Thread waitThread = new Thread() {
			// public void run() {
			// // Log.d("sochat", "wait");
			// synchronized (lock2) {
			// if (descriptor == null) {
			// // Log.d("sochat", "waiting");
			// // Log.d("sochat", fwdLayer == null ? "ffff null"
			// // : "ffff not null");
			// try {
			// lock2.wait();
			//
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// // Log.d("sochat", "waited");
			// }//if
			//
			// }//lock
			// sendMessage("everyone", 1, 1, ID);
			// }
			// };
			// flag_lock2 = true;
			// waitThread.start();
           
			
			
			Thread sendThread = new Thread() {
				public void run() {
					synchronized (lock) {
						if (descriptor == null) {
							Log.d("sochat", "waiting");
							Log.d("sochat", fwdLayer == null ? "ffff null"
									: "ffff not null");
							try {
								lock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Log.d("sochat", "waited");
						}

					}
					try {
						Log.d("sochat", "discover neighbour sent");
						sendMessage("everyone", 1, 1,
								fwdLayer.getMyAddress(descriptor));
					} catch (ForwardingLayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};
			sendThread.start();

		}

		public LocalService getService() {
			return LocalService.this;
		}

		public void refreshUser() {
			user = ((SochatApplication) getApplication()).getCurrentUser();
		}

	}

	public class PacketListener implements MessageListener {

		public void onMessageReceived(String source, String destination,
				DtnMessage message) {

			try {
				Log.d("sochat", "service Lister called");
				// Read the DTN message
				// Data part
				message.switchToData();
				String ID;
				int type;
				int code;
				ID = message.readString();
				type = message.readInt();
				code = message.readInt();
				if (type == 1) { // request
					if (code == 1) { // neighbor
						// store new comer info and then send back own info
						user = ((SochatApplication) getApplication())
								.getCurrentUser();
						String src_user_id = message.readString();
						Log.d("sochat", "received addr " + src_user_id);
						Log.d("sochat",
								"my addr " + fwdLayer.getMyAddress(descriptor));
						Log.d("sochat", "ID " + user.getID());
						Log.d("sochat", "neighbour message received");

						DtnMessage reply_message = new DtnMessage();
						reply_message.addData();// Create data chunk
						reply_message
						      
								.writeString(
										String.valueOf(System
												.currentTimeMillis()))
								.writeInt(2).writeInt(1)
								  .writeString("DSF")
						        .writeString("asd")
								.writeString(fwdLayer.getMyAddress(descriptor))
								.writeString(user.getName())
								.writeString(user.getBirthdayString())
								.writeBoolean(user.getGender())
								.writeString(user.getGroup())
								.writeString(user.getStatus());

						final File photoFile;
						String fileName = "";

						if (user.getProfileImagePath() != null
								&& !user.getProfileImagePath()
										.equalsIgnoreCase("")) {
							photoFile = new File(user.getProfileImagePath());
							fileName = photoFile.getName();
							reply_message.writeString(fileName);
							reply_message.addFile(photoFile);
						} else
							reply_message.writeString(fileName);

						Log.d("sochat", "send message: hello world0");
						Log.d("sochat", "user: " + user.toString());
						fwdLayer.sendMessage(descriptor, reply_message,
								"everyone", null);
						Log.d("sochat", "send message: hello world1");

					}

					if (type == 3) { // chat
						if (!isBound) { // unbound show notification
							Toast toast = Toast.makeText(
									getApplicationContext(),
									"Your have a new message!",
									Toast.LENGTH_SHORT);
							toast.show();
						}

					}

				}

			} catch (Exception e) {
				// Log the exception
				// Log.e("BroadcastApp", "Exception on message event", e);
				// Tell the user
				// createToast("Exception on message event, check log");
			}
		}

	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		user = ((SochatApplication) getApplication()).getCurrentUser();

		// Display a notification about us starting. We put an icon in the
		// status bar.
		middleware = new DtnMiddlewareProxy(getApplicationContext());

		try {
			middleware.start(new MiddlewareListener() {
				public void onMiddlewareEvent(MiddlewareEvent event) {
					try {
						Log.d("sochat", "called start");
						// Check if the middleware failed to start
						if (event.getEventType() != MiddlewareEvent.MIDDLEWARE_STARTED) {
							Log.e("BroadcastApp", "Middleware failed to start");
							return;
						}

						// Get the fwd layer API
						fwdLayer = new ForwardingLayerProxy(middleware);
						Log.d("sochat", "called1");

						// Get a descriptor for this user
						// Typically, the user enters the username, but here we
						// simply use IMEI number
						TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
						String ID = telephonyManager.getDeviceId();

						descriptor = fwdLayer.getDescriptor(
								"nus.cs4222.sochat", ID);
						// Set the broadcast address
						fwdLayer.setBroadcastAddress("nus.cs4222.sochat",
								"everyone");

						// Register a listener for received chat messages
						messageListener = new PacketListener();
						fwdLayer.addMessageListener(descriptor, messageListener);
						Log.d("sochat", fwdLayer == null ? "newfwd null"
								: "newfwd not null");
						Log.d("sochat", descriptor == null ? "descriptor null"
								: "descriptor not null");

						Log.d("sochat", "notify");
						// if (flag_lock1) {
						synchronized (lock) {
							Log.d("sochat", "notifying");
							lock.notifyAll();

							Log.d("sochat", "notifyed");
						}
						// }
						// if (flag_lock2) {
						// synchronized (lock2) {
						// Log.d("sochat", "notifying");
						// lock2.notify();
						//
						// Log.d("sochat", "notifyed");
						// }
						// }
					} catch (Exception e) {
						// Log the exception
						Log.e("BroadcastApp",
								"Exception in middleware start listener", e);
						// Inform the user

					}// catch
				}// event
			});
		} catch (MiddlewareException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String filepath = Environment.getExternalStorageDirectory().getPath();
		directory = new File(filepath, "Received");

		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (!directory.isDirectory()) {
			try {
				throw new IOException(
						"Unable to create PhotoSharingApp directory");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		try {
			middleware.stop();
		} catch (MiddlewareException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Tell the user we stopped.
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

}