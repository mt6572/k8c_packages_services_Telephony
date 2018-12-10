package com.lava.phone.dao;
import com.mediatek.phone.HyphonManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;

/**
 * 所有方法都为异步方法，提供一个返回的监听器或handler
 * @author rick
 *
 */
public class RecordNumberDao {

	//public static
	/**
	 * 判断是否对这个number进行录音，如果列表为空，返回true,如果不为空,则查询这个number是否存在，存在则不为空，不存在则为空
	 * @param context
	 * @param number
	 * @param handler 执行完成之后的回调
	 * @return
	 */
	public static void isRecordNumber(final Context context,final String number,final OnCompleteListener handler){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(handler!=null){
					String num = getRecordNumber(number);//lava modify by liuweibo 20140519 bug 37197
					boolean b = isRecordNumber(context,num);
					if(b){
						String name = findNameByNumber(context,num);
						handler.onComplete(name,num);
					}
				}
			}
		}).start();
	}
	public interface OnCompleteListener{
		public void onComplete(String name,String number);
	}
	public static boolean isExist(Context context,String number){
		if(number==null)return false;
		String num1 = "";
		if(number.startsWith("+91")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("091")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("+86")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("0")){
			num1 = number.substring(1);
		}
		else
			num1 = number;
		//else{ //没有前缀状态，但号码有前缀
		final String num2 = num1; //没有前缀
			num1 = "'+91"+num1 +
					"' or number='091"+num1+
					"' or number='+86"+num1+
					"' or number='0"+num1+"'";
		//}
		String sql = "";
		if(num1.length()>0){
			sql = "select number from num where number=? or number="+num1;
		}
		else{
			sql ="select number from num where number=?";
		}
		
		Database db = new Database(context);
		Cursor cursor1 = db.executeQuery(sql, num2);
		if(cursor1.moveToFirst()){
			number = cursor1.getString(0);
			cursor1.close();
			return true;
		}
		else {
			cursor1.close();
			return false;
		}
		
		
	}
	//lava add start by liuweibo 20140519 bug 37197
	private static String getRecordNumber(String number)
	{
		if(number==null)return null;
		String num1 = "";
		if(number.startsWith("+91")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("091")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("+86")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("0")){
			num1 = number.substring(1);
		}
		else
			num1 = number;
		return num1;
	}
	//lava add end by liuweibo 20140519 bug 37197
	private static boolean isRecordNumber(Context context,String number){
		if(number==null)return false;
		String num1 = "";
		if(number.startsWith("+91")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("091")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("+86")){
			num1 = number.substring(3);
		}
		else if(number.startsWith("0")){
			num1 = number.substring(1);
		}
		else
			num1 = number;
		//else{ //没有前缀状态，但号码有前缀
		final String num2 = num1; //没有前缀
			num1 = "'+91"+num1 +
					"' or number='091"+num1+
					"' or number='+86"+num1+
					"' or number='0"+num1+"'";
		//}
		String sql = "";
		if(num1.length()>0){
			sql = "select number from num where number=? or number="+num1;
		}
		else{
			sql ="select number from num where number=?";
		}
		
		Database db = new Database(context);
		Cursor cursor = db.executeQuery("select 1 from num ");  //如果这里没有数据，就录音所有
		if(cursor.moveToFirst()){
			Cursor cursor1 = db.executeQuery(sql, num2);
			if(cursor1.moveToFirst()){
				number = cursor1.getString(0);
				cursor1.close();
				cursor.close();
				return true;
			}
			else {
				cursor1.close();
				cursor.close();
				return false;
			}
		}
		else{
			cursor.close();
			return true;
		}
	}
	public static String findNameByNumber(Context context,String number){
		Cursor cursor = null;
		Cursor contactCursor = null;
		try{
			String formatNumber = HyphonManager.getInstance().formatNumber(number);
			String id = "";
			 contactCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
				ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? or "+
				ContactsContract.CommonDataKinds.Phone.DATA4+"=? or "+
				ContactsContract.CommonDataKinds.Phone.NUMBER+"=? or "+
				ContactsContract.CommonDataKinds.Phone.NUMBER+" like ?", new String[]{number,number,formatNumber,"%"+formatNumber}, null);
			 if(contactCursor.moveToNext()){
				 id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
			 }
			 if(id==null||id.equals(""))
				 return "";
			 
			cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
				new String[]{ContactsContract.Contacts.DISPLAY_NAME}, 
				ContactsContract.Contacts._ID+"=?", new String[]{id},null);
			
			if(cursor.moveToNext()){
				int displayNameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				String disPlayName = cursor.getString(displayNameColumn);
				System.out.println(number+":"+disPlayName);
				return disPlayName;
			}
			else{
				return "";
			}
		}
		catch(Exception e){
			System.out.println(e);
			return "";
		}
		finally{
			if(cursor!=null)
				cursor.close();
			if(contactCursor!=null)
				contactCursor.close();
		}
	}
}
