package com.keenan.gather;

import java.io.IOException;

import com.keenan.gather.R;

import db.adapter.DBAdapter;

import the_gather.adapter.TabsPagerAdapter;
import android.annotation.SuppressLint;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputFilter.LengthFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, OnItemSelectedListener {
	ViewPager viewPager;
	TabsPagerAdapter mAdapter;
	ActionBar actionBar;
	public DBAdapter db = new DBAdapter(this);
	ListView listView;
	Cursor cursor;

	// Tab titles

	private String[] tabs = { "Home", "Contacts", "Profile" };
	TextView tv, tvName, tvMobile, tvEmail, tvUser;
	String s1, s2, s3, s4, s5, user;
	Spinner spinner, spinner2;
	Button btn;
	private static final String TAG = "gather";
	private boolean mResumed = false;
	private boolean mWriteMode = false;
	NfcAdapter mNfcAdapter;
	EditText mNote, etName, etPhone, etEmail;
	PendingIntent mNfcPendingIntent;
	IntentFilter[] mWriteTagFilters;
	IntentFilter[] mNdefExchangeFilters;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db.open();
		setContentView(R.layout.activity_main);
		ScreenFragment();
		turnNFC();

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		tv = (TextView) findViewById(R.id.textView4);
		tvName = (TextView) findViewById(R.id.textView1);
		tvMobile = (TextView) findViewById(R.id.textView2);
		tvEmail = (TextView) findViewById(R.id.textView3);
		tvUser = (TextView) findViewById(R.id.txtUser);
		listView = (ListView) findViewById(R.id.listView1);
		mNote = (EditText) findViewById(R.id.etHere);
		etName = (EditText) findViewById(R.id.personName);
		etPhone = (EditText) findViewById(R.id.phoneNumber);
		etEmail = (EditText) findViewById(R.id.eMail);
		btn = (Button) findViewById(R.id.write);
		spinner = (Spinner) findViewById(R.id.spinner1);
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.user_type, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.user_department,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		spinner2.setAdapter(adapter2);
		spinner2.setOnItemSelectedListener(this);

		findViewById(R.id.write).setOnClickListener(mTagWriter);
		mNote.addTextChangedListener(mTextWatcher);

		getUserIntent();
		profileConfig();

		// Handle all of our received NFC intents in this activity.
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Intent filters for reading a note from a tag or exchanging over p2p.
		IntentFilter ndefDetected = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
		}
		mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

		// Intent filters for writing to a tag
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };

	}

	// for populating profile text fields
	private void profileConfig() {
		db.open();
		Cursor cursor = db.getUser(tvUser.getText().toString());
		String name = cursor.getString((1)).toString();
		String mobile = cursor.getString((3)).toString();
		String email = cursor.getString((4)).toString();

		etName.setText(name);
		etPhone.setText(mobile);
		etEmail.setText(email);
	}

	// intent to get user mobile
	private void getUserIntent() {
		Intent intent = getIntent();
		String mobileText = intent.getStringExtra("herehere");
		tvUser.setText(mobileText);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mResumed = true;
		// Text received from Android
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			NdefMessage[] messages = getNdefMessages(getIntent());
			byte[] payload = messages[0].getRecords()[0].getPayload();
			setNoteBody(new String(payload));
			setIntent(new Intent()); // Consume this intent.
		}
		enableNdefExchangeMode();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mResumed = false;
		mNfcAdapter.disableForegroundNdefPush(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// NDEF exchange mode
		if (!mWriteMode
				&& NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			NdefMessage[] msgs = getNdefMessages(intent);
			promptForContent(msgs[0]);
		}

		// Tag writing mode
		if (mWriteMode
				&& NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			writeTag(getNoteAsNdef(), detectedTag);
		}
	}

	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void afterTextChanged(Editable arg0) {
			if (mResumed) {
				mNfcAdapter.enableForegroundNdefPush(MainActivity.this,
						getNoteAsNdef());
			}
		}
	};

	private View.OnClickListener mTagWriter = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// Write to a tag for as long as the dialog is shown.
			if (etName.getText().toString().trim().equals("")
					|| etPhone.getText().toString().trim().equals("")
					|| etEmail.getText().toString().trim().equals("")) {
				Toast.makeText(getApplicationContext(),
						"Please fill out all fields.", Toast.LENGTH_LONG)
						.show();

			} else {
				profileCompiler();
				disableNdefExchangeMode();
				enableTagWriteMode();

				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Data Exchange").setMessage("Touch sticker to write | Press back if you are using Android Beam and touch phones.")
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										disableTagWriteMode();
										enableNdefExchangeMode();
									}
								}).create().show();
			}
		}
	};

	public void profileCompiler() {
		// TODO Auto-generated method stub
		// EditText etName = (EditText)findViewById(R.id.personName);
		// EditText etPhone = (EditText)findViewById(R.id.phoneNumber);
		// EditText etEmail = (EditText)findViewById(R.id.eMail);
		// TextView tv = (TextView)findViewById(R.id.textView4);
		s1 = etName.getText().toString();
		s2 = etPhone.getText().toString();
		s3 = etEmail.getText().toString();
		s4 = spinner.getSelectedItem().toString();
		s5 = spinner2.getSelectedItem().toString();
		mNote.setText(s1 + "\r\n" + s2 + "\r\n" + s3 + "\r\n" + s4 + "\r\n"
				+ s5);

	}

	public void promptForContent(final NdefMessage msg) {
		new AlertDialog.Builder(this)
				.setTitle("Save Contact?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								String body = new String(msg.getRecords()[0]
										.getPayload());
								setNoteBody(body);
								addContact();
							}

							public void addContact() {

								String compile = mNote.getText().toString();

								String[] lines = compile.split(System
										.getProperty("line.separator"));
								String sName = lines[0];
								String sMobile = lines[1];
								String sEmail = lines[2];
								String sType = lines[3];
								String sDepartment = lines[4];

								user = tvUser.getText().toString();
								db.open();
								Cursor cursor = db.getContact(sMobile, user);
								if (cursor.getCount() <= 0) {
									db.open();
									db.insertContact(sName, sMobile, sEmail,
											sType, sDepartment, user);
									Toast.makeText(getApplicationContext(),
											"Contact Added", Toast.LENGTH_LONG)
											.show();
									sendSMS(sMobile, "I scanned your contact details using NTEC Gather Mobile Application."); 
									populateListView();
								}
								if (cursor.getCount() > 0) {
									db.open();
									db.deleteExistingContact(sMobile);
									db.insertContact(sName, sMobile, sEmail,
											sType, sDepartment, user);
									Toast.makeText(getApplicationContext(),
											"Contact updated",
											Toast.LENGTH_LONG).show();
									populateListView();
								}
								/*
								 * db.open(); ;
								 */
							}

							
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				}).show();
	}

	private void setNoteBody(String body) {

		Editable text = mNote.getText();
		text.clear();
		text.append(body);

	}

	private NdefMessage getNoteAsNdef() {

		byte[] textBytes = mNote.getText().toString().getBytes();
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"text/plain".getBytes(), new byte[] {}, textBytes);
		return new NdefMessage(new NdefRecord[] { textRecord });
	}

	NdefMessage[] getNdefMessages(Intent intent) {
		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		} else {
			Log.d(TAG, "Unknown intent.");
			finish();
		}
		return msgs;
	}

	private void enableNdefExchangeMode() {
		mNfcAdapter
				.enableForegroundNdefPush(MainActivity.this, getNoteAsNdef());
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mNdefExchangeFilters, null);
	}

	private void disableNdefExchangeMode() {
		mNfcAdapter.disableForegroundNdefPush(this);
		mNfcAdapter.disableForegroundDispatch(this);
	}

	private void enableTagWriteMode() {
		mWriteMode = true;
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mWriteTagFilters, null);
	}

	private void disableTagWriteMode() {
		mWriteMode = false;
		mNfcAdapter.disableForegroundDispatch(this);
	}

	boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					toast("Tag is read-only.");
					return false;
				}
				if (ndef.getMaxSize() < size) {
					toast("Tag capacity is " + ndef.getMaxSize()
							+ " bytes, message is " + size + " bytes.");
					return false;
				}

				ndef.writeNdefMessage(message);
				toast("Ready to go!");
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						toast("Formatted tag and wrote message");
						return true;
					} catch (IOException e) {
						toast("Failed to format tag.");
						return false;
					}
				} else {
					toast("Tag doesn't support NDEF.");
					return false;
				}
			}
		} catch (Exception e) {
			toast("Failed to write tag");
		}

		return false;
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_exit) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void populateListView() {

		Cursor cursor = db.getAllContacts(tvUser.getText().toString());

		// The desired columns to be bound
		String[] columns = new String[] {

		DBAdapter.CONTACTS_NAME, DBAdapter.CONTACTS_MOBILE,
				DBAdapter.CONTACTS_EMAIL, DBAdapter.CONTACTS_TYPE,
				DBAdapter.CONTACTS_DEPARTMENT,

		};

		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.textName, R.id.textNumber, R.id.textEmail,
				R.id.textType, R.id.textDepartment,

		};

		// create the adapter using the cursor pointing to the desired data
		// as well as the layout information
		SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this,
				R.layout.list_layout, cursor, columns, to, 0);

		listView = (ListView) findViewById(R.id.listView1);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);

	}

	public void ScreenFragment() {
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));

		}
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				if (position == 2) {

					// tvName.setVisibility(View.VISIBLE);
					// tvMobile.setVisibility(View.VISIBLE);
					// tvEmail.setVisibility(View.VISIBLE);

					btn.setVisibility(View.VISIBLE);
					etEmail.setVisibility(View.VISIBLE);
					etName.setVisibility(View.VISIBLE);
					etPhone.setVisibility(View.VISIBLE);
					spinner.setVisibility(View.VISIBLE);
					spinner2.setVisibility(View.VISIBLE);

				} else {

					// tvName.setVisibility(View.INVISIBLE);
					// tvMobile.setVisibility(View.INVISIBLE);
					// tvEmail.setVisibility(View.INVISIBLE);
					btn.setVisibility(View.INVISIBLE);
					etEmail.setVisibility(View.INVISIBLE);
					etName.setVisibility(View.INVISIBLE);
					etPhone.setVisibility(View.INVISIBLE);
					spinner.setVisibility(View.INVISIBLE);
					spinner2.setVisibility(View.INVISIBLE);

				}

				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	private void turnNFC() {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		nfcAdapter.isEnabled();
		if (nfcAdapter.isEnabled() != true) {
			Toast.makeText(this, "Please turn on NFC.", Toast.LENGTH_LONG)
					.show();
			startActivity(new Intent(
					android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	public void sendSMS(String mobile, String message) {
		Intent intent  = new Intent(this, MainActivity.class);
		 PendingIntent pi = PendingIntent.getActivity(this,0, intent, 0);                
		      SmsManager sms = SmsManager.getDefault();
		        sms.sendTextMessage(mobile, null, message, pi, null); 
		}
	}

