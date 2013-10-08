package nus.cs4222.sochat;

import java.util.ArrayList;

import nus.cs4222.sochat.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BroadcastFragment extends Fragment {
	private GroupListAdapter adapter;
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	ListView groupList;

	public BroadcastFragment() {
	}

	public void refreshGroupList() {

		ArrayList<String> groups = ((SochatApplication) getActivity()
				.getApplication()).getGroups();
		adapter.clear();
		for (String g : groups) {
			adapter.add(g);
		}
	}

	public void onResume() {
		super.onResume();
		refreshGroupList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_broadcast,
				container, false);
		groupList = (ListView) rootView.findViewById(R.id.group_list);
		ArrayList<String> groups = ((SochatApplication) getActivity()
				.getApplication()).getGroups();
		adapter = new GroupListAdapter(groupList.getContext(), R.id.group_list,
				groups);
		groupList.setAdapter(adapter);

		groupList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getActivity(),
						BroadcastActivity.class);
				intent.putExtra(EXTRA_MESSAGE, adapter.getItem(position));
				if (position == 0) {
					intent.putExtra("name", "Public Channel");
				} else {
					intent.putExtra("name", adapter.getItem(position));
				}
				startActivity(intent);
			}
		});

		return rootView;
	}

	public GroupListAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(GroupListAdapter adapter) {
		this.adapter = adapter;
	}
}
