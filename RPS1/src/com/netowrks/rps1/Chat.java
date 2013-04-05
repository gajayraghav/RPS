package com.netowrks.rps1;

import java.io.File;

import com.rps.utilities.BitmapWrapper;
import com.rps.utilities.ChatMessage;
import com.rps.utilities.ChatMessageTypes;
import com.rps.utilities.ConversationLists;
import com.rps.utilities.ConversationsAdapter;
import com.rps.utilities.ConvMessage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Chat extends Activity {
	private ConversationsAdapter convAdapter;
	private ListView convList;
	private EditText messageBox;
	ConversationLists conversations;
	LowerLayer Ll_instance = new LowerLayer();
	String currentPhoneNumber;

	protected static final int GALLERY_PICTURE = 11;
	protected static final int CAMERA_PICTURE = 12;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		conversations = ConversationLists.getInstance(getContentResolver());
		convList = (ListView) findViewById(R.id.listView1);
		convList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		convList.setStackFromBottom(true);
		convAdapter = new ConversationsAdapter(this, R.layout.listitem_discuss);
		convList.setAdapter(convAdapter);

		messageBox = (EditText) findViewById(R.id.msgBox);

		Intent intent = getIntent();
		currentPhoneNumber = intent.getStringExtra("phoneNum");

		
		System.out.println("Adding "
				+ conversations.getMessageCountWith(currentPhoneNumber)
				+ " messages to list.");
		convAdapter.addAll(conversations
				.getConversationsWith(currentPhoneNumber));
	}

	boolean direction = false;
	int id = 1;

	public void sendMessage(View parent) {
		sendMessageToLowerLayer(messageBox.getText().toString(), false);
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

		photoPath += "send_" + captureTime + ".jpeg";
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
			sendMessageToLowerLayer(imageFilePath, true);
		}
	}

	private void sendMessageToLowerLayer(String content, boolean isImage) {
		ConvMessage newMsg = new ConvMessage(false, content, isImage);

		ChatMessage sendPayLoad;
		if (!isImage)
			sendPayLoad = new ChatMessage(ChatMessageTypes.TEXT,
					newMsg.comment, newMsg.timestamp);
		else {
			
			BitmapWrapper wrappedBitmap = new BitmapWrapper(newMsg.comment);
			sendPayLoad = new ChatMessage(ChatMessageTypes.IMAGE,
					wrappedBitmap, newMsg.timestamp);
		}

		convAdapter.add(newMsg);

		System.out.println(conversations
				.getMessageCountWith(currentPhoneNumber));
		conversations.addMessageToConversationWith(currentPhoneNumber, newMsg);
		System.out.println(conversations
				.getMessageCountWith(currentPhoneNumber));

		LlPacket send_pkt = new LlPacket();
		send_pkt.payload = sendPayLoad;
		// Assumption: Can be of CHAT_MESSAGE type only because only
		// chat will call send
		// GPS_LIST and SENDER_GPS are sent automatically by lower layer
		send_pkt.type = 0;
		send_pkt.Recv_No = currentPhoneNumber;

		/*
		 * try { send_pkt.port = Integer .valueOf(portIn.getText().toString());
		 * } catch (NumberFormatException e) { return; }
		 */
		// LowerLayer.SendHelper Send_instance = Ll_instance.new SendHelper();
		// Send_instance.execute(send_pkt);

		if (!Ll_instance.send(send_pkt)) {
			Toast.makeText(getApplicationContext(), "Sending failed",
					Toast.LENGTH_LONG).show();
		}
	}
}
