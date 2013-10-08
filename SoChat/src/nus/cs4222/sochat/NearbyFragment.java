package nus.cs4222.sochat;

import java.util.ArrayList;
import java.util.Collection;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class NearbyFragment extends Fragment {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public NearbyListAdapter adapter;
	private TextView filterText;
	private User curUser;

	public NearbyListAdapter getAdapter() {
		return adapter;
	}

	public NearbyFragment() {
	}

	public void refreshNearByList(Collection<User> users) {
		FriendList f = FriendList.fromStorage(curUser.getID());
		if (f == null) {
			f = new FriendList(curUser.getID());
		}
		adapter.setFriendList(f.getFriends());
		adapter.clear();
		if (users.size() == 0) {
			adapter.add(User.getUtilityUser());
		} else {
			for (User u : users) {
				adapter.add(u);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_nearby,
				container, false);
		final ListView nearbyList = (ListView) rootView
				.findViewById(R.id.nearbylist);
		adapter = new NearbyListAdapter(nearbyList.getContext(),
				R.id.nearbylist, new ArrayList<User>());
		nearbyList.setAdapter(adapter);
		curUser = ((SochatApplication) getActivity().getApplication())
				.getCurrentUser();
		filterText = (TextView) rootView.findViewById(R.id.nearby_text_search);
		filterText.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				refreshNearByList(((MainActivity) getActivity()).filterOther(s
						.toString().trim()));
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		Log.d("Load nearby", "onCreateView");
		return rootView;
	}
}
