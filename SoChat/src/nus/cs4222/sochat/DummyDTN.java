package nus.cs4222.sochat;

import java.util.ArrayList;
import java.util.List;

public final class DummyDTN {
	public DummyDTN() {

	}

	public static User getCurrentUser() {
		User du0 = new User("du0", "MALE");
		return du0;
	}

	public static List<User> getNearbyUser() {
		List<User> result = new ArrayList<User>();
		User du1 = new User("du1", "MALE");
		User du2 = new User("du2", "FEMALE");
		User du3 = new User("du3", "MALE");
		User du4 = new User("du4", "FEMALE");
		User du5 = new User("du5", "MALE");
		User du6 = new User("du6", "FEMALE");
		result.add(du1);
		result.add(du2);
		result.add(du3);
		result.add(du4);
		result.add(du5);
		result.add(du6);
		return result;

	}
}
