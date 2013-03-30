package nus.cs4222.sochat;

import nus.cs4222.sochat.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BroadcastFragment extends Fragment {
	public BroadcastFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_nearby,
				container, false);
		Log.d("Load broadcast", "onCreateView");
		return rootView;
	}
}
