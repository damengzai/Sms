package com.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.sms.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Sms extends Activity implements OnClickListener {
	private EditText to_et, content_et;
	private TextView receive_content;
	private Button send_btn;
	private ContentResolver resolver = null;
	String contentStr = "";
	List<String> list = new ArrayList<String>();
	Uri uriAll = Uri.parse("content://sms/");
	Uri uriSend = Uri.parse("content://sms/sent");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_layout);
		to_et = (EditText) findViewById(R.id.sms_to_et);// 发给谁
		content_et = (EditText) findViewById(R.id.sms_content_et);// 内容
		receive_content = (TextView) findViewById(R.id.sms_receive_content);// 收到的内容
		send_btn = (Button) findViewById(R.id.sms_send_btn);// 发送按钮
		send_btn.setOnClickListener(this);
		resolver = this.getContentResolver();// 用于读取ContentProvider提供的内容
		getSms();
		resolver.registerContentObserver(uriAll, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {// 当系统中的短信有变化的时候，查询短信
				// TODO Auto-generated method stub
				super.onChange(selfChange);
				getSms();
			}
		});
	}

	private void getSms() {// 读取系统的短信
		String text = null;
		Cursor cursor = resolver.query(uriAll, new String[] { "address", "type", "date", "body" }, null, null, null);
		list.clear();
		contentStr = "";
		// if (cursor.moveToFirst()) {
		while (cursor.moveToNext()) {
			String address = cursor.getString(0);
			int type = cursor.getInt(1);
			long date = cursor.getLong(2);
			String body = cursor.getString(3);
			text = (type == 1 ? "发件人:" : "发送成功 \n 收件人:") + address + "\n 时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(date)) + "\n 内容:" + body;
			list.add(text);
		}
		// }
		for (String s : list) {
			contentStr = contentStr + (s + "\n" + "-------------------------" + "\n");
		}
		receive_content.setText(contentStr + "\n");
		cursor.close();
	}

	private void storeSms(String destination, String content) {// 把短信存到系统中
		ContentValues cv = new ContentValues();
		cv.put("address", destination);
		cv.put("person", "");
		cv.put("protocol", "0");
		cv.put("read", "1");
		cv.put("status", "-1");
		cv.put("body", content);
		this.getContentResolver().insert(uriSend, cv);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sms_send_btn:
			if (to_et.getText().toString().equals("") || content_et.getText().toString().equals("")) {
				Toast.makeText(this, "收件人和内容不可为空", Toast.LENGTH_LONG).show();
			} else {
				// 点击发送按钮后，把信息发送到指定的人并把信息存到系统中
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(to_et.getText().toString() + "", null, content_et.getText().toString() + "", null, null);
				storeSms(to_et.getText().toString() + "", content_et.getText().toString() + "");
			}
			break;

		default:
			break;
		}
	}
}
