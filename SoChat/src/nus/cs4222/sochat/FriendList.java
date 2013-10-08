package nus.cs4222.sochat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.os.Environment;

public class FriendList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<User> friends = new ArrayList<User>();
	private String userID;
	private static final String FRIEND_FOLDER = "Sochat/Friend";

	/**
	 * 
	 */
	public boolean isFriend(String ID) {
		for (User u : friends) {
			if (u.getID().equals(ID)) {
				return true;
			}
		}
		return false;
	}

	public FriendList(String ID) {
		this.userID = ID;
	}

	public ArrayList<User> getFriends() {
		return friends;
	}

	public void removeFriend(User user) {
		if (isFriend(user.getID())) {
			friends.remove(user);
			this.toStorage();
		}
	}

	public void addFriend(User u) {
		if (isFriend(u.getID())) {
			return;
		}
		friends.add(u);
		this.toStorage();
	}

	public void toStorage() {
		try {
			FileOutputStream fileOut = new FileOutputStream(
					getConversationFilePath(userID));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static FriendList fromStorage(String ID) {
		try {
			FileInputStream fileIn = new FileInputStream(
					getConversationFilePath(ID));
			ObjectInputStream in = new ObjectInputStream(fileIn);
			return (FriendList) in.readObject();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return null;
		}
	}

	private static String getConversationFilePath(String ID) {
		return (getAppFolderPath() + "/" + ID);
	}

	public static String getAppFolderPath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, FRIEND_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}
}
