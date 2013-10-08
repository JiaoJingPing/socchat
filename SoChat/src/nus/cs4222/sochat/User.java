package nus.cs4222.sochat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;

public class User implements Comparable<User>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_GROUP = "";
	private static final String DEFAULT_STATUS = "Hello Everyone";

	private static final String APP_FOLDER = "Sochat";
	private String name;
	private Calendar birthday;
	private boolean gender;
	private String group;
	private String status;
	private String ID;
	private String profileImagePath;

	public String getProfileImagePath() {
		return profileImagePath;
	}

	public void setProfileImagePath(String profileImagePath) {
		this.profileImagePath = profileImagePath;
	}

	public int getAge() {
		return Calendar.getInstance().get(Calendar.YEAR)
				- birthday.get(Calendar.YEAR);
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public User(String ID, String name, Calendar birthday, boolean isMale) {
		this(ID, name, birthday, isMale, DEFAULT_GROUP, DEFAULT_STATUS);
	}

	public String getBirthdayString() {
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
		return formatter.format(birthday.getTime());
	}

	public User(String ID, String name, Calendar birthday, boolean gender,
			String group, String status) {
		this.ID = ID.trim();
		this.name = name.trim();
		this.birthday = birthday;
		this.gender = gender;
		this.group = group.trim();
		this.status = status.trim();
		this.profileImagePath = "";
	}

	public static User UserWith(String ID, String name, String birthday,
			boolean isMale, String group, String status) {
		Calendar birth = Calendar.getInstance();
		DateFormat formatter;
		Date date;
		formatter = new SimpleDateFormat("dd-MMM-yy");
		try {
			date = (Date) formatter.parse(birthday);
			birth.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new User(ID, name, birth, isMale, group, status);
	}

	public Calendar getBirthday() {
		return birthday;
	}

	public void setBirthday(Calendar birthday) {
		this.birthday = birthday;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getGender() {
		return gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group.toLowerCase();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return ID + " " + name + " " + getBirthdayString() + " " + gender + " "
				+ group + " " + status + " " + profileImagePath + "\n";

	}

	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass())
			return false;
		else {
			User user = (User) obj;
			return this.ID.equals(user.getID());
		}
	}

	// Save to disk
	public void toStorage() {
		try {
			File file = new File(getUserFilename(this.ID));
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(name + "\n");
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
			bw.write(formatter.format(birthday.getTime()) + "\n");
			bw.write(gender + "\n");
			bw.write(status + "\n");
			bw.write(group + "\n");
			if (profileImagePath != null) {
				bw.write(profileImagePath + "\n");
			}
			bw.close();
			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Read from disk
	public static User fromStorage(String ID) {
		String username = "";
		Calendar age = Calendar.getInstance();
		boolean isMale = true;
		String status = "";
		String profileImagePath = "";
		String group = "ALL";
		BufferedReader br = null;
		try {
			String sCurrentLine;
			int i = 0;
			br = new BufferedReader(new FileReader(getUserFilename(ID)));
			while ((sCurrentLine = br.readLine()) != null) {
				if (i == 0) {
					username = sCurrentLine;
				} else if (i == 1) {
					DateFormat formatter;
					Date date;
					formatter = new SimpleDateFormat("dd-MMM-yy");
					date = (Date) formatter.parse(sCurrentLine);
					age.setTime(date);
				} else if (i == 2) {
					isMale = Boolean.valueOf(sCurrentLine);
				} else if (i == 3) {
					status = sCurrentLine;
				} else if (i == 4) {
					group = sCurrentLine;
				} else if (i == 5) {
					profileImagePath = sCurrentLine;
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		User newUser = new User(ID, username, age, isMale, group, status);
		newUser.setProfileImagePath(profileImagePath);
		return newUser;
	}

	private static String getUserFilename(String ID) {
		return (getAppFolderPath() + "/" + ID);
	}

	public static String getAppFolderPath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, APP_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}

	@Override
	public int compareTo(User other) {
		// TODO Auto-generated method stub
		if (Long.parseLong(this.getID()) - Long.parseLong(other.getID()) > 0)
			return 1;
		if (Long.parseLong(this.getID()) - Long.parseLong(other.getID()) < 0)
			return -1;
		return 0;

	}

	public static boolean isNoUser(User u) {
		return u.getID().trim().equalsIgnoreCase("0");
	}

	public static User getUtilityUser() {
		return new User("0", "No user", Calendar.getInstance(), true,
				DEFAULT_GROUP, DEFAULT_STATUS);
	}

}
