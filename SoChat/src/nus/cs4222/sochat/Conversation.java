package nus.cs4222.sochat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

import android.os.Environment;

public class Conversation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5238081965966352941L;
	private Timestamp createOn;
	private String userFrom;
	private String userTo;
	private ArrayList<OneComment> comments;
	private static final String APP_FOLDER = "Sochat";

	public void addComment(OneComment comment) {
		comments.add(comment);
	}

	public Conversation(String from, String to) {
		setUserFrom(from);
		setUserTo(to);
		setComments(new ArrayList<OneComment>());
		setCreateOn(new Timestamp(new java.util.Date().getTime()));
	}

	public String getUserFrom() {
		return userFrom;
	}

	public void setUserFrom(String userFrom) {
		this.userFrom = userFrom;
	}

	public ArrayList<OneComment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<OneComment> comments) {
		this.comments = comments;
	}

	public String getUserTo() {
		return userTo;
	}

	public void setUserTo(String userTo) {
		this.userTo = userTo;
	}

	public Timestamp getCreateOn() {
		return createOn;
	}

	public void setCreateOn(Timestamp createOn) {
		this.createOn = createOn;
	}

	public void toStorage() {
		try {
			FileOutputStream fileOut = new FileOutputStream(
					getConversationFilePath(userFrom, userTo));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static Conversation fromStorage(String userFrom, String userTo) {
		try {
			FileInputStream fileIn = new FileInputStream(
					getConversationFilePath(userFrom, userTo));
			ObjectInputStream in = new ObjectInputStream(fileIn);
			return (Conversation) in.readObject();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return null;
		}
	}

	private static String getConversationFilePath(String userFrom, String userTo) {
		return (getAppFolderPath() + "/" + userFrom + "_" + userTo);
	}

	public static String getAppFolderPath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, APP_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}

	
}
