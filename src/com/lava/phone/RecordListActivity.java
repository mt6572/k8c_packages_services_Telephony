package com.lava.phone;
import android.os.storage.StorageManager;
import java.io.File;
import java.util.ArrayList;
import com.mediatek.storage.StorageManagerEx;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phone.InCallScreen;
import com.android.phone.R;
import com.lava.phone.dao.Database;
import com.lava.phone.dao.RecordNumberDao;

/**
 * 
 * @author rick
 *
 */
public class RecordListActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener{
	private static final String TAG = "RecordListActivity";
	private ListView numberList = null;
	private TextView emptyText = null;
	private ArrayAdapter<String> adapter = null;
	private static final int CALL_LIST_DIALOG_EDIT = 1;
	private static final int CALL_LIST_DIALOG_WAIT = 2;
	private static final int CALL_LIST_DIALOG_SELECT = 3; 
	private static final int CALL_LIST_DIALOG_INFO = 4;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_list);
		
		emptyText = (TextView)findViewById(android.R.id.empty);
		
		numberList = (ListView)findViewById(android.R.id.list);
		numberList.setEmptyView(emptyText);
		numberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				AlertDialog.Builder builder = new AlertDialog.Builder(RecordListActivity.this);
				final int postiton =pos;
				builder.setTitle(R.string.conform_delete).setPositiveButton(R.string.record_number_delete,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String number = adapter.getItem(postiton);
						Database db = new Database(RecordListActivity.this);
						db.executeSQL("delete from num where number=?",number);
						initView();
					}
				}).setNegativeButton(R.string.record_number_cancel, null).show();;
			}
			
		});
		
		initView();
		View other = findViewById(R.id.add_record_item);
        other.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	showDialog(1);
            }
        });
        other.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundColor(0xff017B93);
					break;
				case MotionEvent.ACTION_UP:
					v.setBackgroundColor(0xffdddddd);
					break;
				}
				return false;
			}
		});
	}
	private ProgressDialog progress = null;
	private ImageButton mAddContactsBtn;
	private EditText mNumberEditText;
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch(id){
		case CALL_LIST_DIALOG_EDIT:
			LayoutInflater inflater= (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			final View textEntryView=inflater.inflate(R.layout.record_add, null);

			AlertDialog dlg = new AlertDialog.Builder(this).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					String number = mNumberEditText.getText().toString();
					if(number.equals("")){
					}
					else{
						if(!insertNumbers(number)){
							Toast.makeText(RecordListActivity.this, "number "+number+" exist", Toast.LENGTH_SHORT).show();
						}
						else
							initView();
					}
					mNumberEditText.clearFocus();
					
				}
			})
			.setNegativeButton(R.string.close_profile, null)
			.setTitle(R.string.record_add)
			.setView(textEntryView).create(); //关键;
			
			mAddContactsBtn = (ImageButton)textEntryView.findViewById(R.id.select_contact);
			if (mAddContactsBtn != null) {
                mAddContactsBtn.setOnClickListener(this);
            }
			mNumberEditText = (EditText)textEntryView.findViewById(android.R.id.text1);
			mNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(final View v, boolean hasFocus) {
					if(hasFocus){
						
					}
					else{
						InputMethodManager imm =
				                (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					}
				}
			});
			//dlg.show();
			return dlg;
		case CALL_LIST_DIALOG_WAIT:
			if(progress==null||!progress.isShowing()){
				progress=new ProgressDialog(this);
				progress.setMessage(this.getString(R.string.loading));
			}
			return progress;
		case CALL_LIST_DIALOG_SELECT:
			Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.call_reject_dialog_contact);
            dialog.setTitle(getResources().getString(R.string.select_from));
            ListView listview = (ListView)dialog.findViewById(R.id.list);
            listview.setOnItemClickListener(this);

			return dialog;
		case CALL_LIST_DIALOG_INFO:
			
			AlertDialog dialog1 = new AlertDialog.Builder(this).setPositiveButton("OK",null)
			.setTitle("Path")
			.create();
			return dialog1;
		}
		return null;
	}
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case CALL_LIST_DIALOG_EDIT:
			mNumberEditText.setText("");
			break;
		case CALL_LIST_DIALOG_INFO:
			String title = "";
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			title = sharedPreferences.getString("save_location_key", getDefaultPath());
			((AlertDialog)dialog).setTitle(title);
			break;
		}
	}

	public static String getDefaultPath(){
		File sampleDir = new File(StorageManagerEx.getDefaultPath());

        if (!sampleDir.canWrite()) {
            Log.i(TAG, "----- file can't write!! ---");
           return "/storage/sdcard0"+"/Call Recordings";
        }
        else
        	return StorageManagerEx.getDefaultPath()+"/Call Recordings";
	}
	public boolean insertNumbers(String number){
		//Cursor cursor = db.executeQuery("SELECT 1 FROM num WHERE number=?", new String[]{number});
		if(!RecordNumberDao.isExist(RecordListActivity.this, number)){
			Database db1 = new Database(RecordListActivity.this);
			ContentValues cv = new ContentValues();
			cv.put("number", number);
			db1.insert("num", cv);
			db1 = null;
			return true;
		}
		else
			return false;
	}
	
	private boolean isEmpty = true;
	private ArrayList<String> mNumberList ;
	private void initView() {
		showDialog(2);
		isEmpty = true;
		if(adapter==null){
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
			numberList.setAdapter(adapter);
		}
		else 
			adapter.clear();
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(mNumberList==null){
					mNumberList = new ArrayList<String>();
				}else mNumberList.clear();
				
				Database db = null;
				Cursor cursor = null;
				try{
					db = new Database(RecordListActivity.this);
					cursor = db.executeQuery("select _id,number from num", null);
					if(cursor.moveToFirst()){
						int index1 = cursor.getColumnIndex("_id");
						int index2 = cursor.getColumnIndex("number");
						do{
							/*Message msg = mHandler.obtainMessage();
							msg.obj = cursor.getString(index2);
							//adapter.add();
							msg.what = ADD_UPDATE_UI;
							mHandler.sendMessage(msg);*/
							mNumberList.add(cursor.getString(index2));
							isEmpty = false;
						}
						while(cursor.moveToNext());
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally{
					if(cursor!=null&&!cursor.isClosed())
					cursor.close();
					mHandler.sendEmptyMessage(ADD_UPDATE_COMPLETE);
				}
			}
		}).start();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.record_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.record_clean_all:
			showDialog(2);
			new Thread(new Runnable() {
				@Override
				public void run() {
					Database db = new Database(RecordListActivity.this);
					db.executeSQL("delete from num");
					mHandler.sendEmptyMessage(DELETE_UPDATE_COMPLETE);
				}
			}).start();
			break;
		case R.id.add_icon:
			showDialog(CALL_LIST_DIALOG_EDIT);
			if(mNumberEditText!=null)
				mNumberEditText.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					im.showSoftInput(mNumberEditText, 0);
				}
			},100);
			break;
		case R.id.pick_path:
			Intent intent = new Intent(PICK_PATH_ACTION);
			startActivityForResult(intent, Call_SELECT_PATH_REQUEST);
			break;
		case R.id.view_save_path:
			showDialog(CALL_LIST_DIALOG_INFO);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private static final int DELETE_UPDATE_COMPLETE = 2;
	private static final int ADD_UPDATE_UI = 1;
	private static final int	ADD_UPDATE_COMPLETE = 3;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			/*case ADD_UPDATE_UI:
				adapter.add(msg.obj.toString());
				break;*/
			case ADD_UPDATE_COMPLETE:
				if(progress!=null&&progress.isShowing())
					progress.cancel();
				/*if(isEmpty == false){
					numberList.setVisibility(View.VISIBLE);
					emptyText.setVisibility(View.GONE);
					numberList.setAdapter(adapter);
					numberList.invalidate();
				}
				else{
					emptyText.setVisibility(View.VISIBLE);
					numberList.setVisibility(View.GONE);
				}*/
				adapter.addAll(mNumberList);
				adapter.notifyDataSetChanged();
				break;
			case DELETE_UPDATE_COMPLETE:
				initView();
				break;
			}
		}
		
	};
	@Override
	public void onClick(View v) {
		 if (v == mAddContactsBtn) {
	            dismissDialog(CALL_LIST_DIALOG_EDIT);
	            showDialog(CALL_LIST_DIALOG_SELECT);
	     }
	}
	private static final String CONTACTS_ADD_ACTION = "android.intent.action.contacts.list.PICKMULTIPHONES";
	private static final String CALL_LOG_SEARCH = "android.intent.action.SEARCH";
	private static final String CONTACTS_ADD_ACTION_RESULT = "com.mediatek.contacts.list.pickdataresult";
	private static final String PICK_PATH_ACTION = "com.mediatek.filemanager.DOWNLOAD_LOCATION";
	public static final String DOWNLOAD_PATH_KEY = "download path";
	private static final int CALL_CONTACTS_REQUEST = 125; 
    private static final int CALL_LOG_REQUEST = 126; 
    private static final int Call_SELECT_PATH_REQUEST = 127;
    private Intent mResultIntent;
    private static final Uri CONTACT_URI = Data.CONTENT_URI;
    private static final String[] CALLER_ID_PROJECTION = new String[] {
        Phone._ID,                      // 0
	    Phone.NUMBER,                   // 1
	    Phone.LABEL,                    // 2
	    Phone.DISPLAY_NAME,             // 3
	};
    private static final int PHONE_ID_COLUMN = 0;
    private static final int PHONE_NUMBER_COLUMN = 1;
    private static final int PHONE_LABEL_COLUMN = 2;
    private static final int CONTACT_NAME_COLUMN = 3;
    private static final Uri CALLLOG_URI = Uri.parse("content://call_log/calls");
    public static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,                       // 0
        Calls.NUMBER,                    // 1
    };

    public static final int ID = 0;
    public static final int NUMBER = 1;
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0) {
            Intent intent = new Intent(CONTACTS_ADD_ACTION);            
            intent.setType(Phone.CONTENT_TYPE);
            try {
                startActivityForResult(intent, CALL_CONTACTS_REQUEST);
                dismissDialog(CALL_LIST_DIALOG_SELECT);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, e.toString());
            }
        } else if (position == 1) {
            Intent intent = new Intent(CALL_LOG_SEARCH);
            intent.setClassName("com.android.contacts", 
                "com.mediatek.contacts.activities.CallLogMultipleChoiceActivity");
            try {
                startActivityForResult(intent, CALL_LOG_REQUEST);
                dismissDialog(CALL_LIST_DIALOG_SELECT);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, e.toString());
            }
        }
	}
	private AddContactsTask mAddContactsTask = null;
	@Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        mAddContactsTask = new AddContactsTask();
        mResultIntent = data;
        mAddContactsTask.execute(requestCode, resultCode);
    }
	
	class AddContactsTask extends AsyncTask<Integer, Integer, String> {  

        @Override  
        protected void onPreExecute() {  
            showDialog(CALL_LIST_DIALOG_WAIT);
            invalidateOptionsMenu();
            super.onPreExecute();
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {  
            updataCallback(params[0], params[1], mResultIntent);
            return "";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {  
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {  
            if (!this.isCancelled()) {
                dismissDialog(CALL_LIST_DIALOG_WAIT);
                initView();
            }
            super.onPostExecute(result);  
        }  

        @Override
        protected void onCancelled(String result) {
            super.onCancelled(result);
        }
    }
	private void updataCallback(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
        case RESULT_OK:
            if (requestCode == CALL_CONTACTS_REQUEST) {
                final long[] contactId = data.getLongArrayExtra(CONTACTS_ADD_ACTION_RESULT);
                if (contactId == null || contactId.length < 0) {
                    break;
                }
                for (int i = 0; i < contactId.length && !mAddContactsTask.isCancelled(); i++) {
                    updateContactsNumbers((int)contactId[i]);
                }
            } else if (requestCode == CALL_LOG_REQUEST) {
                final String callLogId = data.getStringExtra("calllogids");
                updateCallLogNumbers(callLogId);
            } else if(requestCode == Call_SELECT_PATH_REQUEST){
            	String path = data.getStringExtra(DOWNLOAD_PATH_KEY);
            	//change path
            	System.out.println(path);
            	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            	Editor editor = sharedPreferences.edit();
            	editor.putString("save_location_key", path);
            	editor.commit();
    			
            }
            break;
        default:
            break;
        }
    }
	
	private void updateContactsNumbers(int id) {
        Uri existNumberURI = ContentUris.withAppendedId(CONTACT_URI, id);
        Cursor cursor = getContentResolver().query(
            existNumberURI, CALLER_ID_PROJECTION, null, null, null);
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                String number = allWhite(cursor.getString(PHONE_NUMBER_COLUMN));
                insertNumbers(number);
                cursor.moveToNext();
           }
        } finally {
            cursor.close();
        }
    }
	private String allWhite(String str) {
        if (str != null) {
            str = str.replaceAll(" ", "");
        }
        return str;
    }
	private void updateCallLogNumbers(String callLogId) {
        Log.v(TAG, "---------[" + callLogId + "]----------");
        if (callLogId == null || callLogId.isEmpty()) {
            return;
        }    
        if (!callLogId.startsWith("_id")) {
            return;
        }
        String ids = callLogId.substring(8, callLogId.length() - 1);
        String [] idsArray = ids.split(",");
        for (int i = 0; i < idsArray.length && !mAddContactsTask.isCancelled(); i++) {
            try {
                int id = Integer.parseInt(idsArray[i].substring(1, idsArray[i].length() - 1));
                updateCallLogNumbers(id);
                Log.i(TAG, "id is " + id);
            } catch (NumberFormatException e) {
                Log.e(TAG, "parseInt failed, the id is " + e);
            }
        }
    }
	
	private void updateCallLogNumbers(int id) {
        Uri existNumberURI = ContentUris.withAppendedId(CALLLOG_URI, id);
        Cursor cursor = getContentResolver().query(existNumberURI, CALL_LOG_PROJECTION, null, null, null);
        cursor.moveToFirst();
        Log.v(TAG, "----updateCallLogNumbers---[calllogid"+id+"]-------");
        String data_id = "";
        try {
            if (!cursor.isAfterLast()) {
                String number = allWhite(cursor.getString(NUMBER));
                /*use to update the call log username end*/
                insertNumbers(number);
                cursor.moveToNext();
            }
       } finally {
           cursor.close();
       }
    }
	
}
