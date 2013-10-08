package nus.cs4222.sochat;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GroupListAdapter extends ArrayAdapter<String> {
	private ArrayList<String> groups;
	private Context context;

	public GroupListAdapter(Context context, int textViewResourceId,
			ArrayList<String> groups) {
		super(context, textViewResourceId, groups);
		this.context = context;
		this.groups = groups;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.group_item, parent, false);
		TextView groupName = (TextView) rowView.findViewById(R.id.group_name);
		if (position == 1) {
			groupName.setText("Personal Channel (" + groups.get(position) + ")");
		} else {
			groupName.setText("Public Channel");
		}
		return rowView;
	}

	public ArrayList<String> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<String> groups) {
		this.groups = groups;
	}

}
