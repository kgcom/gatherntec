package com.keenan.gather;

import java.util.zip.Inflater;

import com.keenan.gather.R;

import db.adapter.DBAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ContactsFragment extends Fragment implements OnItemClickListener {
	private DBAdapter db;
	ListView listView;
	MainActivity mainActivity = new MainActivity();
	TextView tvUser;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		db = new DBAdapter(getActivity());
		View rootView = inflater.inflate(R.layout.fragment_contacts, container,
				false);
		tvUser = (TextView) getActivity().findViewById(R.id.txtUser);

		listView = (ListView) rootView.findViewById(R.id.listView1);
		listView.setOnItemClickListener(this);
		db.open();
		populateListView();
		return rootView;

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			final long id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Options");
		builder.setItems(R.array.options,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						if (item == 0) {

							Cursor cursor = db.getAllContacts(tvUser.getText()
									.toString());
							String email = cursor.getString((3)).toString();
							composeEmail(email);

						}
						if (item == 1) {

							Cursor cursor = db.getAllContacts(tvUser.getText()
									.toString());
							String mobile = cursor.getString((2)).toString();
							dialPhoneNumber(mobile);
						}

						if (item == 2) {
							db.open();
							db.deleteContact(id);
							populateListView();
							deletedToast();
						}
					}

					private void deletedToast() {
						Toast.makeText(getActivity().getApplicationContext(),
								"Contact Deleted", Toast.LENGTH_SHORT).show();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	public void populateListView() {
		try {
			Cursor cursor = db.getAllContacts(tvUser.getText().toString());

			// The desired columns to be bound
			String[] columns = new String[] {

			DBAdapter.CONTACTS_NAME, DBAdapter.CONTACTS_MOBILE,
					DBAdapter.CONTACTS_EMAIL, DBAdapter.CONTACTS_TYPE,
					DBAdapter.CONTACTS_DEPARTMENT,

			};

			// the XML defined views which the data will be bound to
			int[] to = new int[] { R.id.textName, R.id.textNumber,
					R.id.textEmail, R.id.textType, R.id.textDepartment, };

			// create the adapter using the cursor pointing to the desired data
			// as well as the layout information
			SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(
					getActivity(), R.layout.list_layout, cursor, columns, to, 0);

			// Assign adapter to ListView
			listView.setAdapter(dataAdapter);
		} catch (Exception e) {
			Toast.makeText(getActivity(), "No contacts to display",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	public void composeEmail(String email) {
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("mailto:" + email)); // only email apps should
														// handle this
		/*
		 * intent.putExtra(Intent.EXTRA_EMAIL, addresses);
		 * intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		 */
		if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivity(intent);
		}
	}

	public void dialPhoneNumber(String phoneNumber) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + phoneNumber));
		if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivity(intent);
		}
	}

}
