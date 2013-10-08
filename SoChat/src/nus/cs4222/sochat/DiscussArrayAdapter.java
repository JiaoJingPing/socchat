package nus.cs4222.sochat;

import java.util.ArrayList;
import nus.cs4222.sochat.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DiscussArrayAdapter extends ArrayAdapter<OneComment> {

	private TextView commentView;
	ImageView profileView;
	ImageView imageView;
	private ArrayList<OneComment> comments = new ArrayList<OneComment>();

	@Override
	public void add(OneComment object) {
		comments.add(object);
		super.add(object);
	}

	public ArrayList<OneComment> getComments() {
		return comments;
	}

	public DiscussArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.comments.size();
	}

	public OneComment getItem(int index) {
		return this.comments.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		OneComment comment = getItem(position);
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row;

		// Comment is voice
		if (comment.getVoice_dir() != null
				&& !comment.getVoice_dir().equalsIgnoreCase("")) {

			if (comment.isLeft())
				row = inflater.inflate(R.layout.listitem_voice_left, parent,
						false);
			else
				row = inflater.inflate(R.layout.listitem_voice_right, parent,
						false);
			return row;
		}
		// Comment is picture
		if (comment.getPicture_dir() != null
				&& !comment.getPicture_dir().equalsIgnoreCase("")) {
			if (comment.isLeft())
				row = inflater.inflate(R.layout.listitem_image_left, parent,
						false);
			else
				row = inflater.inflate(R.layout.listitem_image_right, parent,
						false);

			imageView = (ImageView) row.findViewById(R.id.chat_image);
			setPicToView(imageView, comment.getPicture_dir());
			return row;
		}

		// Else comment is text
		if (comment.isLeft())
			row = inflater.inflate(R.layout.listitem_text_left, parent, false);
		else
			row = inflater.inflate(R.layout.listitem_text_right, parent, false);
		commentView = (TextView) row.findViewById(R.id.comment);
		commentView.setText(comment.getComment());
		return row;

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