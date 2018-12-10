package com.lava.phone.preference;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Switch;
import com.android.phone.R;
/**
 * 
 * @author rick.peng
 * pfl add
 *
 */
public class SwitchButtonPreference extends Preference{
	private static final String TAG = "SwitchButtonPreference";
	//接收xml中传入的参数
	private TextView title;
	private TextView summary;
	private Switch checkbox;
	
	//标题
	private String titles;
	//注释
	private String summarys;
	//默认值，是否check
	private boolean isCheck;
	//点击标题后的动作，eg :com.lava.example.activity
	private String action;
	
	public static final String NAMESPACE = "http://schemas.android.com/apk/res/android";
	
	public SwitchButtonPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		for(int index=0;index<attrs.getAttributeCount();index++){
			String name = attrs.getAttributeName(index);
			if(name==null)
				continue;
			if(name.equals("title")){
				titles = attrs.getAttributeValue(NAMESPACE, name);
			}
			else if(name.equals("summary")){
				summarys = attrs.getAttributeValue(NAMESPACE, name);
			}
			else if(name.equals("action")){
				action = attrs.getAttributeValue(NAMESPACE, name);
			}
			else if(name.equals("defaultValue")){
				isCheck = attrs.getAttributeBooleanValue(NAMESPACE, name,false);
			}
		}
	}
	public SwitchButtonPreference(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	public SwitchButtonPreference(Context context) {
		this(context,null);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		title = (TextView)view.findViewById(android.R.id.title);
		summary = (TextView)view.findViewById(android.R.id.summary);
		checkbox = (Switch)view.findViewById(android.R.id.checkbox);
		
		boolean ch = this.getPersistedBoolean(isCheck);
		checkbox.setChecked(ch);
		title.setEnabled(ch);
		summary.setEnabled(ch);
		checkbox.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View arg0) {
				if(checkbox.isChecked()){
					checkbox.setChecked(true);
				}
				else{
					checkbox.setChecked(false);
				}
			}
		});
		checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					title.setEnabled(true);
					summary.setEnabled(true);
					getEditor().putBoolean(getKey(), true).commit();
				}
				else{
					title.setEnabled(false);
					summary.setEnabled(false);
					getEditor().putBoolean(getKey(), false).commit();
				}
			}
		});
	}
	@Override
	protected View onCreateView(ViewGroup parent) {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_switch,parent, false);
		
		return view;
	}

	@Override
	protected void onClick() {
		if(!checkbox.isChecked()){
			return;
		}
		Class clazz = null;
		try{
			clazz = Class.forName(action);
		}
		catch(ClassNotFoundException e){
			Log.e(TAG, e.getMessage());
			return;
		}
		
		if(action!=null&&!action.trim().equals("")){
			Intent intent = new Intent();
			intent.setClass(this.getContext(), clazz);
			this.getContext().startActivity(intent);
		}
	}
	
}
