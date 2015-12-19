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

public class LoginActivity extends Activity implements OnClickListener {
	MainActivity ma;
	EditText etMobile, etPassword;
	Button btn;
	DBAdapter db = new DBAdapter(this);

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		etMobile = (EditText) findViewById(R.id.et_mobile);
		etPassword = (EditText) findViewById(R.id.et_password);
		btn = (Button) findViewById(R.id.btn_login);
		btn.setOnClickListener(this);
	}

	public void register_click(View v) {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
	}

	public void passRecovery(View v) {
		Intent intent = new Intent(this, ForgotPasswordActivity.class);
		startActivity(intent);

	}

	@Override
	public void onClick(View v) {
		String mobile = etMobile.getText().toString().trim();
		String password = etPassword.getText().toString();
		db.open();
		Cursor cursor = db.getSingleUser(mobile, password);

		try {

			if (etMobile.getText().length() == 0 && etPassword.getText().length() == 0){ 
				Toast.makeText(getApplicationContext(),
						"Please fill out all fields", Toast.LENGTH_SHORT)
						.show();
			}
		
			else if(cursor.getCount() > 0) {
				Intent intent = new Intent(this, MainActivity.class);
				intent.putExtra("herehere", etMobile.getText().toString());
				startActivity(intent);

			}

			else if (cursor.getCount()<=0){
				Toast.makeText(getApplicationContext(),
						"Invalid mobile or password", Toast.LENGTH_SHORT)
						.show();
			}

		} catch (SQLException e) {
			Toast.makeText(getApplicationContext(), "Problem occured.",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

}
