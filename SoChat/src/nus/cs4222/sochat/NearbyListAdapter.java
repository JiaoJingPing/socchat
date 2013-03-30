package nus.cs4222.sochat;

import java.util.List;

import nus.cs4222.sochat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NearbyListAdapter extends ArrayAdapter<User> {
	private final List<User> nearbyUserList;
	private final Context context;

	public NearbyListAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId, objects);
		this.nearbyUserList = objects;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.nearby_item, parent, false);
		TextView genderTextView = (TextView) rowView
				.findViewById(R.id.nearby_gender);
		TextView nameTextView = (TextView) rowView
				.findViewById(R.id.nearby_name);

		genderTextView.setText(nearbyUserList.get(position).getGender());
		nameTextView.setText(nearbyUserList.get(position).getName());
		
		return rowView;
	}
	
	public List<User> getNearbyUserList() {
		return nearbyUserList;
	}

}
