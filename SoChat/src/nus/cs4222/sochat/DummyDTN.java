package nus.cs4222.sochat;

import java.util.ArrayList;
import java.util.Calendar;

public final class DummyDTN {
	public DummyDTN() {

	}

	public static User getCurrentUser() {
		User du0 = new User("0", "Dummy user1", Calendar.getInstance(), true,
				"SOC", "Hellow");
		return du0;
	}

	public static ArrayList<User> getNearbyUser() {
		ArrayList<User> result = new ArrayList<User>();
		User du1 = new User("1", "Dummy user1", Calendar.getInstance(), true,
				"SOC", "Hellow");
		User du2 = new User("2", "Dummy user2", Calendar.getInstance(), true,
				"SOC", "Hellow");
		User du3 = new User("3", "Dummy user3", Calendar.getInstance(), true,
				"SOC", "Hellow");
		User du4 = new User("4", "Dummy user4", Calendar.getInstance(), true,
				"SOC", "Hellow");
		User du5 = new User("5", "Dummy user5", Calendar.getInstance(), true,
				"SOC", "Hellow");
		User du6 = new User("6", "Dummy user6", Calendar.getInstance(), false,
				"SOC", "Hellow");
		result.add(du1);
		result.add(du2);
		result.add(du3);
		result.add(du4);
		result.add(du5);
		result.add(du6);
		return result;

	}
}