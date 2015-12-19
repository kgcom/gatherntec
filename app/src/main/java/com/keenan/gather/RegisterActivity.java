package com.keenan.gather;

import com.keenan.gather.R;

import db.adapter.DBAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

	DBAdapter db = new DBAdapter(this);
	EditText etUserFullName, etUserMobile, etUserEmail, etUserPassword;
	Button reg;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		etUserFullName = (EditText) findViewById(R.id.et_userFullname);
		etUserMobile = (EditText) findViewById(R.id.et_userMobile);
		etUserEmail = (EditText) findViewById(R.id.et_userEmail);
		etUserPassword = (EditText) findViewById(R.id.et_userPassword);
		reg = (Button) findViewById(R.id.btn_register);
		reg.setOnClickListener(this);
		TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		String mobile = tm.getLine1Number();
		etUserMobile.setText(mobile);
	}

	@Override
	public void onClick(View v) {
		if (etUserFullName.getText().length() == 0
				&& etUserPassword.getText().length() == 0
				&& etUserMobile.getText().length() == 0
				&& etUserEmail.getText().length() == 0)
			Toast.makeText(getApplicationContext(),
					"Please fill out all fields", Toast.LENGTH_SHORT).show();
		else
			try {
				String fullname = etUserFullName.getText().toString();
				String password = etUserPassword.getText().toString();
				String mobile = etUserMobile.getText().toString();
				String email = etUserEmail.getText().toString();
				db.open();
				Cursor cursor = db.userCheckIfExist(mobile, email);

				if (cursor.getCount() <= 0) {
					db.open();
					db.insertUser(fullname, password, mobile, email);
					Toast.makeText(getApplicationContext(),
							"Successfully Registered!", Toast.LENGTH_SHORT)
							.show();
					etUserFullName.setText("");
					etUserPassword.setText("");
					etUserMobile.setText("");
					etUserEmail.setText("");
				}
				if (cursor.getCount() > 0) {
					Toast.makeText(getApplicationContext(),
							"Mobile or email already exist", Toast.LENGTH_SHORT)
							.show();

				}

			} catch (SQLException e) {
				Toast.makeText(getApplicationContext(), "Problem occured",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
	}
}
