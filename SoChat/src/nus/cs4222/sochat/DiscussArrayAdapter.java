package nus.cs4222.sochat;

import java.util.ArrayList;
import java.util.List;

import nus.cs4222.sochat.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiscussArrayAdapter extends ArrayAdapter<OneComment> {

	private TextView comment;
	private List<OneComment> comments = new ArrayList<OneComment>();
	private LinearLayout wrapper;

	@Override
	public void add(OneComment object) {
		comments.add(object);
		super.add(object);
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
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_chat, parent, false);
		}

		wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

		OneComment coment = getItem(position);

		comment = (TextView) row.findViewById(R.id.comment);
		ImageView profile = (ImageView)row.findViewById(R.id.image_profile);
		comment.setText(coment.comment);

		comment.setBackgroundResource(coment.left ? R.drawable.bubble_yellow
				: R.drawable.bubble_green);

		if (coment.left) {
			wrapper.setGravity(Gravity.LEFT);
		} else {
			wrapper.removeView(profile);
			wrapper.removeView(comment);
			wrapper.setGravity(Gravity.RIGHT);
			wrapper.addView(comment);
			wrapper.addView(profile);
		}
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}