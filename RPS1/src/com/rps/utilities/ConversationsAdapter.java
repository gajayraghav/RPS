package com.rps.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.netowrks.rps1.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConversationsAdapter extends ArrayAdapter<ConvMessage> {

	private TextView oneMessageView;
	private ImageView oneImageView;
	private List<ConvMessage> allMessages = new ArrayList<ConvMessage>();
	private LinearLayout wrapper;
	private final Activity context;

	public ConversationsAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = (Activity) context;
	}

	@Override
	public void add(ConvMessage object) {
		allMessages.add(object);
		super.add(object);
	}

	@Override
	public void addAll(java.util.Collection<? extends ConvMessage> collection){
		for (ConvMessage message : collection) {
			this.add(message);
		}		
	}

	public int getCount() {
		return this.allMessages.size();
	}

	public ConvMessage getItem(int index) {
		return this.allMessages.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_discuss, parent, false);
		}

		wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

		ConvMessage msg = getItem(position);
		oneImageView = (ImageView) row.findViewById(R.id.messageImageView);
		oneMessageView = (TextView) row.findViewById(R.id.messageTextView);
		if (msg.isImage) {
			oneMessageView.setVisibility(View.GONE);
			oneImageView.setVisibility(View.VISIBLE);
			// oneMessageView.setVisibility(View.INVISIBLE);
			Bitmap bitmap = BitmapWrapper.getDownScaledBitmap(msg.comment,300);
			//Bitmap bitmap = BitmapFactory.decodeFile(msg.comment);
			// Bitmap bitmap = msg.originalBitmap;
			//bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
			oneImageView.setImageBitmap(bitmap);
			oneImageView.setContentDescription(msg.comment);
			oneImageView
					.setBackgroundResource(msg.left ? R.drawable.bubble_yellow
							: R.drawable.bubble_green);
			wrapper.setGravity(msg.left ? Gravity.LEFT : Gravity.RIGHT);
			oneImageView.setOnClickListener(new OnClickListener() {

				public void onClick(View sender) {
					showPicture(sender);
				}
			});
		} else {
			oneImageView.setVisibility(View.GONE);
			oneMessageView.setVisibility(View.VISIBLE);
			// oneImageView.setVisibility(View.INVISIBLE);

			oneMessageView.setText(msg.comment);
			oneMessageView
					.setBackgroundResource(msg.left ? R.drawable.bubble_yellow
							: R.drawable.bubble_green);
			wrapper.setGravity(msg.left ? Gravity.LEFT : Gravity.RIGHT);
		}

		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	public void showPicture(View sender) {
		ImageView oneImageView = (ImageView) sender;
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(oneImageView
				.getContentDescription().toString())), "image/*");
		context.startActivity(intent);
	}
}