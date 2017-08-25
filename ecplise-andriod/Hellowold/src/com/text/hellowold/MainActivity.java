package com.text.hellowold;

import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.ActionBar;
//import android.support.v4.app.Fragment;
import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
import android.view.View;
//import android.view.ViewGroup;
//import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.CheckBox;

public class MainActivity extends ActionBarActivity {
	//private EditText tvUserName=null;
	//private EditText tvPassWord=null;
	private Button btnSave=null;
	private RadioButton rbMale=null;
	private RadioButton rbFemale=null;
	private CheckBox cbFootBall=null;
	private CheckBox cbBasketBall=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /**tvUserName=(EditText)super.findViewById(R.id.userName);
        tvPassWord=(EditText)super.findViewById(R.id.passWord);
        btnLogin=(Button)super.findViewById(R.id.login);
        btnLogin.setOnClickListener(new LoginOnClickListener());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
                    
        }*/
        rbMale=(RadioButton)super.findViewById(R.id.male);
        rbFemale=(RadioButton)super.findViewById(R.id.female);
        cbFootBall=(CheckBox)super.findViewById(R.id.football);
        cbBasketBall=(CheckBox)super.findViewById(R.id.basketball);
        btnSave=(Button)super.findViewById(R.id.save);
        btnSave.setOnClickListener(new SaveOnClickListener());
    }


   
    /**public boolean onCreateOptionsMenu(Menu menu) {
        
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
     * A placeholder fragment containing a simple view.
     
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    private class LoginOnClickListener implements OnClickListener{
    	public void onClick(View v){
    		String username =tvUserName.getText().toString();
    		String password =tvPassWord.getText().toString();
    		String info="用户名："+username+"密码:"+password;
    		Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
    		
    	}
    }*/
   
    private class SaveOnClickListener implements OnClickListener{
    	public void onClick(View v){
    		String sGender="";
    		String sFav="";
    		String sInfo="";
    		if (rbFemale.isChecked())
    			sGender =rbFemale.getText().toString();
    		if (rbMale.isChecked())
    			sGender =rbMale.getText().toString();
    		if (cbFootBall.isChecked())
    			sFav=sFav+cbFootBall.getText().toString();
    		if (cbBasketBall.isChecked())
    			sFav=sFav+cbBasketBall.getText().toString();
    		sInfo="性别："+sGender+"***"+"爱好："+sFav;
    		
    		Toast.makeText(getApplicationContext(),sInfo,Toast.LENGTH_LONG).show();
    		
    	}
    }
 }
