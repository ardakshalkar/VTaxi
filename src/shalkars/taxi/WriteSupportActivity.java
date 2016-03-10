package shalkars.taxi;

import java.util.List;
import shalkars.taxi.R;


import com.google.analytics.tracking.android.EasyTracker;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class WriteSupportActivity extends Activity {
	int addcount =0;
	Handler handler;
    @TargetApi(11)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler=new Handler();
        setContentView(R.layout.support);
    }
    public void cancel(View view){
		Intent showContent = new Intent(getApplicationContext(),Main2Activity.class);
		startActivity(showContent);
    }
    private class WriteSupportTask extends AsyncTask<String, Void, String>{
    	@Override
    	protected void onPostExecute(String result){
    		if (result.equals("NAMEORRATING")){
    			Toast.makeText(getApplicationContext(), "может вы хотите ввести ваше имя?",Toast.LENGTH_LONG).show();
    		}
    	}
		@Override
		protected String doInBackground(String... params) {
			EditText etowner=(EditText)findViewById(R.id.supportowner);
			EditText ettext =(EditText)findViewById(R.id.supporttext);
			if (ettext.getText().length()>0){
				String owner = "Аноним";
				if (etowner.getText().length()>0){
					owner = etowner.getText().toString();
				}
				Parse.initialize(WriteSupportActivity.this, "wYwjvSd8hs05UPISchXs1wEkIxJ7orGX43V1YaF0", "VSb0LZRRBp0lFU7Byd04F3YHHdArTdjgJr3a4iHV");				
				ParseUser.enableAutomaticUser();
				ParseACL defaultACL = new ParseACL();
				//defaultACL.setPublicReadAccess(true);
				//ParseACL.setDefaultACL(defaultACL, true);
				ParseObject support=new ParseObject("Feedback");
				support.put("author", owner);
				support.put("message", ettext.getText().toString());
				support.put("city", Datas.getCityName());
				support.saveInBackground();
				handler.post(new Runnable(){

					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "спасибо, мы обязательно прочитаем ваше обращение",Toast.LENGTH_LONG).show();
					}
					
				});
				Intent showContent = new Intent(getApplicationContext(),Main2Activity.class);
				startActivity(showContent);
			}
			else
				return "NAMEORRATING";
			return "NOTHING";
		}
    }
    public void addSupport(View view){
    	WriteSupportTask task = new WriteSupportTask();
        task.execute(new String[] { String.valueOf(Datas.selectedTaxi) });
    	
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_feedback, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this); // Add this method.
    }
}
