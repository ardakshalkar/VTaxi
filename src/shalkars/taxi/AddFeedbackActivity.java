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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class AddFeedbackActivity extends Activity {
	Handler handler;
	int addcount =0;
    @TargetApi(11)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feedback);
        handler = new Handler();
        //getActionBar().setDisplayHomeAsUpEnabled(true);
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
    public void cancel(View view){
    	Intent showContent = new Intent(getApplicationContext(),TaxiProfileActivity.class);
		startActivity(showContent);
    }
    private class AddFeedbackTask extends AsyncTask<String, Void, String>{
    	@Override
    	protected void onPostExecute(String result){

	    	findViewById(R.id.feedbackLoading).setVisibility(View.GONE);
	    	findViewById(R.id.feedbackProgressBar).setVisibility(View.GONE);
    	}
		@Override
		protected String doInBackground(String... params) {
			Parse.initialize(AddFeedbackActivity.this, "wYwjvSd8hs05UPISchXs1wEkIxJ7orGX43V1YaF0", "VSb0LZRRBp0lFU7Byd04F3YHHdArTdjgJr3a4iHV");
			EditText etowner=(EditText)findViewById(R.id.feedbackowner);
	    	EditText et=(EditText)findViewById(R.id.feedbacktext);
			RatingBar rb=(RatingBar)findViewById(R.id.feedbackrating);
			ParseUser.enableAutomaticUser();
			ParseACL defaultACL = new ParseACL();
			defaultACL.setPublicReadAccess(true);
			ParseACL.setDefaultACL(defaultACL, true);
			ParseObject feedback=new ParseObject("Review");
			String owner = "Аноним";
			if (etowner.getText().length()>0){
				owner=etowner.getText().toString();
			}
			feedback.put("owner", owner);
			feedback.put("content", et.getText().toString());
			feedback.put("rating",(int)rb.getRating());
			feedback.put("taxiId", Datas.selectedTaxi.id);
			feedback.saveInBackground();
			ParseQuery subquery = new ParseQuery("Review");
			subquery.whereEqualTo("taxiId",Datas.selectedTaxi.id);
			subquery.orderByDescending("createdAt");
			try{
				List<ParseObject> ratings = subquery.find();
				Datas.updateFeedbacks(ratings);
				
			}catch (Exception e){e.printStackTrace();}
		    Intent showContent = new Intent(getApplicationContext(),TaxiProfileActivity.class);
		    showContent.putExtra("tab","feed");
	   		startActivity(showContent);
			return "NORMAL";
		}
    }
    public void addFeedback(View view){
    	EditText et=(EditText)findViewById(R.id.feedbacktext);
		RatingBar rb=(RatingBar)findViewById(R.id.feedbackrating);
		EditText etowner=(EditText)findViewById(R.id.feedbackowner);
		if (et.getText().length()>0){
			if ((rb.getRating()>.0f&&etowner.getText().length()>0)||addcount==1){
				addcount=0;
				ImageButton bt = (ImageButton)findViewById(R.id.savefeedbackbutton);
				bt.setEnabled(false);
				Datas.feedbackrating = rb.getRating();
				Datas.feedbacktext = et.getText();
		    	findViewById(R.id.feedbackLoading).setVisibility(View.VISIBLE);
		    	findViewById(R.id.feedbackProgressBar).setVisibility(View.VISIBLE);
		    	AddFeedbackTask task = new AddFeedbackTask();
		        task.execute(new String[] { String.valueOf(Datas.selectedTaxi) });
			}
			else if (addcount==0){
				if (etowner.getText().length()==0&&!(rb.getRating()>.0f)){
					Toast.makeText(getApplicationContext(), "может вы хотите дать оценку или ваше имя?",Toast.LENGTH_LONG).show();
				}
				else if (!(rb.getRating()>.0f)){
					Toast.makeText(getApplicationContext(), "может вы хотите дать оценку?",Toast.LENGTH_LONG).show();
				}
				else{
					Toast.makeText(getApplicationContext(), "может вы хотите ввести имя?",Toast.LENGTH_LONG).show();
				}
				addcount++;
			}
		}
		else {
			Toast.makeText(getApplicationContext(), "введите отзыв", Toast.LENGTH_LONG).show();
		}
    	
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
}
