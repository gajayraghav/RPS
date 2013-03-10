package com.netowrks.rps1;

import java.io.File;

import com.rps.utilities.ConversationsAdapter;
import com.rps.utilities.OneMessage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;

public class Chat extends Activity {
	private ConversationsAdapter convAdapter;
	private ListView convList;
	private EditText messageBox;

	private static final int REQUEST_CODE = 1;
	protected static final int GALLERY_PICTURE = 11;
	protected static final int CAMERA_PICTURE = 12;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		convList = (ListView) findViewById(R.id.listView1);
		convList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		convList.setStackFromBottom(true);
		convAdapter = new ConversationsAdapter(this, R.layout.listitem_discuss);
		convList.setAdapter(convAdapter);

		messageBox = (EditText) findViewById(R.id.msgBox);		
	}

	boolean direction = false;
	int id = 1;

	public void sendMessage(View parent) {
		convAdapter.add(new OneMessage(direction, messageBox.getText()
				.toString(), false));
		direction = !direction;
		messageBox.setText("");
	}
	
	public void selectPicture(View parent) {
		getImageFromUser();
	}

	private String getImageFromUser() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Image");
		myAlertDialog.setMessage("Which picture?");

		myAlertDialog.setPositiveButton("Gallery",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						pickGalleryPicture();
					}
				});

		myAlertDialog.setNegativeButton("Camera",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						capturePicture();
					}
				});
		myAlertDialog.show();
		return "";
	}

	private void pickGalleryPicture() {

		Intent gintent = new Intent();
		gintent.setType("image/*");
		gintent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(gintent, "Select Picture"),
				GALLERY_PICTURE);
	}

	String photoPath;

	public void capturePicture() {
		long captureTime = System.currentTimeMillis();
		photoPath = Environment.getExternalStorageDirectory() + File.separator
				+ "trialDir" + File.separator;

		File sdImageMainDirectory = new File(photoPath);
		sdImageMainDirectory.mkdirs();

		photoPath += "pic" + captureTime + ".jpeg";
		sdImageMainDirectory = new File(photoPath);

		Uri outputFileUri = Uri.fromFile(sdImageMainDirectory);
		System.out.println("Cam : " + outputFileUri);
		Intent pictureActionIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		pictureActionIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(pictureActionIntent, CAMERA_PICTURE);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("Cam : " + requestCode + "-" + resultCode + "-"
				+ data);

		if (resultCode == RESULT_CANCELED) {
			System.out.println("Cam : Result canceled");
			return;
		}

		boolean displayImage = false;
		String imageFilePath = "";
		switch (requestCode) {
		case CAMERA_PICTURE:
			if (photoPath != null) {
				System.out.println("Cam : Success");
				imageFilePath = photoPath;
				displayImage = true;
			}
			break;

		case GALLERY_PICTURE:
			Uri uri = data.getData();
			if (uri != null) {
				Cursor cursor = getContentResolver()
						.query(uri,
								new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },
								null, null, null);
				cursor.moveToFirst();
				imageFilePath = cursor.getString(0);
				cursor.close();
				displayImage = true;
			}

			break;

		default:
			break;
		}

		if (displayImage) {
			System.out.println("photoPath " + imageFilePath);
			convAdapter.add(new OneMessage(direction, imageFilePath, true));
			direction = !direction;
		}
	}

}