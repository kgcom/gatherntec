package com.keenan.gather;

import com.keenan.gather.R;

import db.adapter.DBAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgotPasswordActivity extends Activity implements OnClickListener {
	DBAdapter db = new DBAdapter(this);
	EditText etRecMobile, etRecEmail;
	Button btnRec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		etRecMobile = (EditText) findViewById(R.id.rec_number);
		etRecEmail = (EditText) findViewById(R.id.rec_email);
		btnRec = (Button) findViewById(R.id.btnRecover);
		btnRec.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		try {
			String mobile = etRecMobile.getText().toString();
			String email = etRecEmail.getText().toString();

			if (etRecMobile.getText().length() == 0
					&& etRecEmail.getText().length() == 0) {
				Toast.makeText(getApplicationContext(),
						"Please fill out all fields", Toast.LENGTH_SHORT)
						.show();
			}

			db.open();
			Cursor cursor = db.userPasswordRecovery(mobile, email);
			if (cursor.getCount() > 0) {
				String text = cursor.getString((2)).toString();
				Toast.makeText(getApplicationContext(),
						"Your Password:" + text, Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getApplicationContext(),
						"Mobile and email not found", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (SQLException e) {
			Toast.makeText(getApplicationContext(), "Problem occured",
					Toast.LENGTH_SHORT).show();
		}

	}
}
