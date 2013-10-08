package nus.cs4222.sochat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import nus.cs4222.sochat.User;

import android.app.Application;
import android.os.Environment;

public class SochatApplication extends Application {
	private static final String APP_FOLDER = "Sochat";
	private static final String DEFAULT_GROUP = "all";

	private static final String USER_DATA_FILE = "sochat_user.txt";
	private User currentUser;

	public String getAppFolderPath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, APP_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}

	public User getCurrentUser() {
		this.readUserInfo();
		return currentUser;
	}

	public void setGroup(String group) {
		currentUser.setGroup(group);
		currentUser.toStorage();
	}

	public ArrayList<String> getGroups() {
		ArrayList<String> groups = new ArrayList<String>();
		groups.add(DEFAULT_GROUP);
		if (currentUser != null && currentUser.getGroup() != null
				&& !currentUser.getGroup().trim().equalsIgnoreCase("")) {
			groups.add(currentUser.getGroup());
		}
		return groups;
	}

	public void setCurrentUser(User cUser) {
		if (cUser != null) {
			currentUser = cUser;
			saveCurrentUser();
		}
	}

	public void saveCurrentUser() {
		if (currentUser != null) {
			currentUser.toStorage();
			writeUserInfo();
		}
	}

	private void writeUserInfo() {
		try {
			File file = new File(getUserFilename());
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(currentUser.getID() + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readUserInfo() {
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(getUserFilename()));
			sCurrentLine = br.readLine();
			if (sCurrentLine != null) {
				String currentUserID = sCurrentLine.trim();
				currentUser = User.fromStorage(currentUserID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private String getUserFilename() {
		return (getAppFolderPath() + "/" + USER_DATA_FILE);
	}
}
