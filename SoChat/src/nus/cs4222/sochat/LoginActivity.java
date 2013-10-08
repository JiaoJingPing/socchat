package nus.cs4222.sochat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class LoginActivity extends Activity {
	static Button birthdayBtn;
	Button startBtn;
	EditText usernameText;
	EditText statusText;
	RadioButton femaleBtn;
	RadioButton maleBtn;
	private static GregorianCalendar birthday;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		setTitle("Signin");
		birthdayBtn = (Button) findViewById(R.id.birthday_btn);
		startBtn = (Button) findViewById(R.id.signin_button);
		usernameText = (EditText) findViewById(R.id.login_username);
		statusText = (EditText) findViewById(R.id.login_status);
		femaleBtn = (RadioButton) findViewById(R.id.login_radioButtonFemale);
		maleBtn = (RadioButton) findViewById(R.id.login_radioButtonMale);
	}

	public void signIn(View view) {
		// After start button is clicked
		String un = usernameText.getText().toString().trim();
		if (un == null || un.equals("")) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Pleas choose a username", Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		boolean isMale = true;
		if (femaleBtn.isChecked()) {
			isMale = false;
		}

		if (birthday == null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Pleas choose your birthday", Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		System.out.println(birthday);
		String st = statusText.getText().toString().trim();
		if (st == null) {
			st = "";
		}
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String currentUserID = telephonyManager.getDeviceId();
		User currentUser = new User(currentUserID, un, birthday, isMale);
		SochatApplication app = (SochatApplication) getApplication();
		app.setCurrentUser(currentUser);
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	public void maleClicked(View v) {
		femaleBtn.setChecked(false);
	}

	public void femaleClicked(View v) {
		maleBtn.setChecked(false);
	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			String placeHolder = "%d/%d/%d";
			birthdayBtn.setText(String
					.format(placeHolder, day, month + 1, year));
			birthday = new GregorianCalendar();
			birthday.set(year, month, day);
		}
	}
}
