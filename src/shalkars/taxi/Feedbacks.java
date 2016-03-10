package shalkars.taxi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import shalkars.taxi.R;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.app.NavUtils;

public class Feedbacks extends Activity {
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	Intent showContent = new Intent(getApplicationContext(),TaxiProfileActivity.class);
   		    startActivity(showContent);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    @TargetApi(11)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_feedbacks);
        ArrayList<Map<String,String>> list=buildList();
		String[] from = { "owner", "content","date", "rating" };
		int[] to = { R.id.owner, R.id.content, R.id.feedbackdate,R.id.feedbackrating };
		ListView lv=(ListView)findViewById(R.id.feedbackslist);
		SimpleAdapter adapter = new SimpleAdapter(this, list,R.layout.feedlayout, from, to);
		adapter.setViewBinder(new MyBinder());
		lv.setAdapter(adapter);
		Button bt=(Button)findViewById(R.id.addfeedback);
		bt.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent showContent = new Intent(getApplicationContext(),AddFeedbackActivity.class);
	   		    startActivity(showContent);
			}
    	});*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_feedbacks, menu);
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
    private ArrayList<Map<String, String>> buildList() {
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		/*int amount = Datas.taxis[Datas.selectedTaxi].feedbacks.size();
		ArrayList<Feedback> feed = Datas.taxis[Datas.selectedTaxi].feedbacks;
		//amount=4;
		list.clear();
		Log.d(Datas.taxis[Datas.selectedTaxi].id, String.valueOf(amount));
		for (int i=0;i<amount;i++){
			list.add(putData(feed.get(i).owner,feed.get(i).content,feed.get(i).date,feed.get(i).rating));
		}*/
		//list.add(putData("",""));
		return list;
	}
	private HashMap<String, String> putData(String owner, String content, Date date, Float rating) {
		HashMap<String, String> item = new HashMap<String, String>();
		/*item.put("owner", owner);
		item.put("content", content);
		SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");
		item.put("date", sdf.format(date));
		item.put("rating", rating.toString());*/
		return item;
	}
}