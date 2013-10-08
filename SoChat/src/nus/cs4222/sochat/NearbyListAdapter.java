package nus.cs4222.sochat;

import java.util.ArrayList;
import java.util.List;

import nus.cs4222.sochat.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NearbyListAdapter extends ArrayAdapter<User> implements Filterable {

	private ArrayList<User> nearbyUserList;
	private final Context context;
	private ArrayList<User> friends;

	public void setFriendList(ArrayList<User> friends) {
		this.friends = friends;
	}

	public NearbyListAdapter(Context context, int textViewResourceId,
			ArrayList<User> objects) {
		super(context, textViewResourceId, objects);
		this.nearbyUserList = objects;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		User curUser = nearbyUserList.get(position);

		if (curUser.getID().equals("0")) {
			return inflater.inflate(R.layout.nearby_nouser_item, parent, false);
		}

		View rowView = inflater.inflate(R.layout.nearby_item, parent, false);
		RelativeLayout layout = (RelativeLayout) rowView
				.findViewById(R.id.nearby_item_set);
		layout.setTag(String.valueOf(position));
		TextView genderTextView = (TextView) rowView
				.findViewById(R.id.nearby_gender);
		TextView nameTextView = (TextView) rowView
				.findViewById(R.id.nearby_name);
		ImageView profile = (ImageView) rowView
				.findViewById(R.id.nearby_profile_view);
		Button btn = (Button) rowView.findViewById(R.id.nearby_follow_btn);
		btn.setTag(String.valueOf(position));

		if (friends != null && friends.contains(curUser)) {
			btn.setText("Following");
			Drawable image =  context.getResources().getDrawable( R.drawable.favorite_after);
			image.setBounds( 0, 0, 60, 60 );
			btn.setCompoundDrawables( null, null, null, image );
			btn.setEnabled(false);
		} else {
			
			btn.setText("+ Follow");
			btn.setEnabled(true);
		}

		TextView ageText = (TextView) rowView.findViewById(R.id.nearby_ages);
		TextView statusText = (TextView) rowView
				.findViewById(R.id.nearby_item_status);
		if (curUser.getGender()) {
			genderTextView.setText("Male");
			nameTextView.setTextColor(Color.BLUE);

		} else {
			genderTextView.setText("Female");
			nameTextView.setTextColor(Color.CYAN);
		}
		nameTextView.setText(curUser.getName());
		if (curUser.getProfileImagePath() != null
				&& !curUser.getProfileImagePath().equalsIgnoreCase("")) {
			setPicToView(profile, curUser.getProfileImagePath());
		}
		ageText.setText(String.valueOf(curUser.getAge()));
		statusText.setText(curUser.getStatus());
		return rowView;
	}

	public List<User> getNearbyUserList() {
		return nearbyUserList;
	}

	public void setNearbyUserList(ArrayList<User> list) {
		nearbyUserList = list;
	}

	private void setPicToView(ImageView mImageView, String mCurrentPhotoPath) {
		// Get the dimensions of the View
		int targetW = 80;
		int targetH = 80;

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
