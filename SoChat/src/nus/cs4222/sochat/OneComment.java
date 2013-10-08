package nus.cs4222.sochat;

import java.io.Serializable;
import java.sql.Timestamp;

public class OneComment implements Serializable, Comparable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timestamp timestamp;
	private boolean left;
	private String comment;
	private String picture_dir;
	private String voice_dir;
	private String ID;

	public OneComment(boolean left, String comment, String ID) {
		this(left, comment, "", "", ID);
	}

	public OneComment(boolean left, String comment, String picture_dir,
			String voice_dir, String ID) {
		super();
		this.timestamp = new Timestamp(new java.util.Date().getTime());
		this.setID(ID);
		this.setLeft(left);
		this.setComment(comment);
		this.setPicture_dir(picture_dir);
		this.setVoice_dir(voice_dir);
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getVoice_dir() {
		return voice_dir;
	}

	public void setVoice_dir(String voice_dir) {
		this.voice_dir = voice_dir;
	}

	public String getPicture_dir() {
		return picture_dir;
	}

	public void setPicture_dir(String picture_dir) {
		this.picture_dir = picture_dir;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}
	
	public boolean equals(Object o) {
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		OneComment one = (OneComment) o;
		if (one.getID().compareTo(this.getID()) == 0)
			return true;
		else
			return false;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub

		 OneComment one = (OneComment)o;
		return this.getID().compareTo(one.getID());
	}
	
	
	
}