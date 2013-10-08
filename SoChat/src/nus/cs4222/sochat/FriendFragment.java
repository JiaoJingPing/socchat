package nus.cs4222.sochat;

import java.util.TreeSet;

import nus.cs4222.sochat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FriendFragment extends Fragment {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public FriendListAdapter adapter;
	private User curUser;

	public FriendFragment() {
	}

	public void refreshFriendList(TreeSet<User> neighbors) {
		if (neighbors != null) {
			adapter.setNeighbors(neighbors);
			FriendList f = FriendList.fromStorage(curUser.getID());
			if (f == null) {
				f = new FriendList(curUser.getID());
			}
			adapter.clear();
			if (f.getFriends().size() != 0) {
				for (User u : f.getFriends()) {
					adapter.add(u);
				}
			} else {
				adapter.add(User.getUtilityUser());
			}
		}
	}

	public FriendListAdapter getAdapter() {
		return adapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friend, container,
				false);
		final ListView friendList = (ListView) rootView
				.findViewById(R.id.friendlist);
		final Activity curActivity = this.getActivity();
		curUser = ((SochatApplication) getActivity().getApplication())
				.getCurrentUser();

		FriendList f = FriendList.fromStorage(curUser.getID());
		if (f == null) {
			f = new FriendList(curUser.getID());
		}
		
		adapter = new FriendListAdapter(friendList.getContext(),
				R.id.friendlist, f.getFriends());
		if (f.getFriends().size() == 0){
			adapter.add(User.getUtilityUser());
		}
		friendList.setAdapter(adapter);
		friendList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long arg) {
				FriendListAdapter friendAdapter = (FriendListAdapter) adapter
						.getAdapter();
				User clickedUser = friendAdapter.getFriendList().get(position);
				if (User.isNoUser(clickedUser)) {
					return;
				}
				Intent intent = new Intent(curActivity, ChatActivity.class);
				intent.putExtra(EXTRA_MESSAGE, clickedUser.getID());
				startActivity(intent);
			}
		});

		Log.d("Load friend", "onCreateView");
		return rootView;
	}

}
