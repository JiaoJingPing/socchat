package nus.cs4222.sochat;

public class User {
	private String id;
	private String name;
	private String gender;
	private String faculty;
	private String[] interests;

	public User(String name, String gender) {
		this.setDUMMRYID();
		this.setName(name);
		this.setGender(gender);
	}

	private void setDUMMRYID() {
		this.setId(String.valueOf(Math.random() * 100));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String[] getInterests() {
		return interests;
	}

	public void setInterests(String[] interests) {
		this.interests = interests;
	}

	public String getFaculty() {
		return faculty;
	}

	public void setFaculty(String faculty) {
		this.faculty = faculty;
	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}
}
