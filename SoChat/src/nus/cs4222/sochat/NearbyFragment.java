package nus.cs4222.sochat;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class NearbyFragment extends Fragment {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

	public NearbyFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_nearby,
				container, false);
		final ListView nearbyList = (ListView) rootView
				.findViewById(R.id.nearbylist);
		final Activity curActivity = this.getActivity(); 
		NearbyListAdapter adapter = new NearbyListAdapter(
				nearbyList.getContext(), R.id.nearbylist,
				DummyDTN.getNearbyUser());
		nearbyList.setAdapter(adapter);
		nearbyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long arg) {
				NearbyListAdapter nearbyAdapter = (NearbyListAdapter) adapter
						.getAdapter();
				User clickedUser = nearbyAdapter.getNearbyUserList().get(
						position);
				
				Intent intent = new Intent(curActivity, ChatActivity.class);
				intent.putExtra(EXTRA_MESSAGE, clickedUser.getId());
				startActivity(intent);
			}
		});
		Log.d("Load nearby", "onCreateView");
		return rootView;
	}
}
