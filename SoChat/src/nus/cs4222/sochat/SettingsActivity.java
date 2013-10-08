package nus.cs4222.sochat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	static Button birthdayBtn;
	Button startBtn;
	EditText usernameText;
	EditText statusText;
	RadioButton femaleBtn;
	RadioButton maleBtn;

	private static Calendar birthday;
	private final int CameraActionCode = 999;
	private final int GalleryActionCode = 998;
	private String mCurrentPhotoPath = "";
	private String photoPath;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private static boolean isClicked = false;
	private ImageView profile_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		setTitle("Settings");
		profile_view = (ImageView) findViewById(R.id.setting_profile_view);
		birthdayBtn = (Button) findViewById(R.id.birthday_btn);
		usernameText = (EditText) findViewById(R.id.setting_username);
		statusText = (EditText) findViewById(R.id.setting_status);
		maleBtn = (RadioButton) findViewById(R.id.setting_radioButtonMale);
		femaleBtn = (RadioButton) findViewById(R.id.setting_radioButtonFemale);
		User currentUser = ((SochatApplication) getApplication())
				.getCurrentUser();
		loadUser(currentUser);
	}

	protected void onStart() {
		super.onStart();
	}

	public void loadUser(User user) {
		usernameText.setText(user.getName());
		statusText.setText(user.getStatus());
		birthday = user.getBirthday();
		birthdayBtn.setText(user.getBirthdayString());
		mCurrentPhotoPath = new String(user.getProfileImagePath());
		if (!user.getGender()) { // if user is female
			maleBtn.setChecked(false);
			femaleBtn.setChecked(true);
		}
		if (mCurrentPhotoPath != null
				&& !mCurrentPhotoPath.trim().equalsIgnoreCase("")) {
			setPicToView(profile_view, mCurrentPhotoPath);
		}
	}

	public void confirm(View view) {
		// After start button is clicked

		SochatApplication app = (SochatApplication) getApplication();
		User currentUser = app.getCurrentUser();

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

		String st = statusText.getText().toString().trim();
		if (st == null) {
			st = "";
		}

		currentUser.setName(un);
		currentUser.setBirthday(birthday);
		currentUser.setGender(isMale);
		currentUser.setStatus(st);
		currentUser.setProfileImagePath(photoPath);
		((SochatApplication)getApplication()).saveCurrentUser();

		Toast toast = Toast.makeText(getApplicationContext(), "Change Saved",
				Toast.LENGTH_SHORT);
		toast.show();

		quit();
	}

	private void quit() {
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
			if (birthday == null) {
				birthday = Calendar.getInstance();
			}
			int year = birthday.get(Calendar.YEAR);
			int month = birthday.get(Calendar.MONTH);
			int day = birthday.get(Calendar.DAY_OF_MONTH);

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

	public void selectProfile(View v) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

		if (!isClicked) {
			createPopUpWindow();
			isClicked = true;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CameraActionCode && resultCode == RESULT_OK) {
			// Camera intent
			handleBigCameraPhoto();
		} else if (requestCode == GalleryActionCode && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			mCurrentPhotoPath = cursor.getString(columnIndex);
			photoPath = new String(mCurrentPhotoPath);
			Log.d("chat", "$$" + mCurrentPhotoPath);
			cursor.close();
			setPicToView(profile_view, mCurrentPhotoPath);

		}
	}

	private void setPicToView(ImageView mImageView, String mCurrentPhotoPath) {
		// Get the dimensions of the View
		int targetW = 80;
		int targetH = 80;
		try {
			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,
					bmOptions);
			mImageView.setImageBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleBigCameraPhoto() {
		if (mCurrentPhotoPath != null) {
			setPicToView(profile_view, mCurrentPhotoPath);
			galleryAddPic();
		}
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);

	}

	private void createPopUpWindow() {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = layoutInflater.inflate(R.layout.photo_popup, null);
		final PopupWindow popupWindow = new PopupWindow(popupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		Button btnDismiss = (Button) popupView
				.findViewById(R.id.popup_photo_cancel);
		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
				isClicked = false;
			}
		});

		Button btnCamera = (Button) popupView
				.findViewById(R.id.popup_photo_camera);
		btnCamera.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent(CameraActionCode);
				popupWindow.dismiss();
				isClicked = false;
			}
		});
		Button btnGallery = (Button) popupView
				.findViewById(R.id.popup_photo_gallery);
		btnGallery.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				isClicked = false;
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, GalleryActionCode);
				popupWindow.dismiss();
			}
		});
		popupWindow.showAsDropDown(profile_view);

	}

	private void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			photoPath = new String(mCurrentPhotoPath);
			takePictureIntent
					.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(takePictureIntent, actionCode);
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
		}
	}

	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}
}