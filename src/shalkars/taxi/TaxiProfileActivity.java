package shalkars.taxi;
import android.view.ViewGroup;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import shalkars.taxi.R;
import shalkars.taxi.Main2Activity.LoadTaxis;


import com.google.analytics.tracking.android.EasyTracker;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.NavUtils;

public class TaxiProfileActivity extends Activity {
	Handler handler;AlertDialog.Builder builder;
	AlertDialog ad;
	String phones[];
	ListView feedlistview;
	ArrayList<Map<String,String>> feedListData=new ArrayList<Map<String,String>>();
	ArrayList<Map<String,String>> tariffListData=new ArrayList<Map<String,String>>();
	SimpleAdapter feedadapter;SimpleAdapter adapter;
	Button view_more;TextView loading;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	Intent showContent = new Intent(getApplicationContext(),Main2Activity.class);
   		    startActivity(showContent);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    @TargetApi(11)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_profile);
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		TextView profileCity=(TextView)findViewById(R.id.profileCity);
		profileCity.setText(Datas.getCityName());
		final TabWidget tabWidget = tabHost.getTabWidget();
		final FrameLayout tabContent = tabHost.getTabContentView();
		
		// Get the original tab textviews and remove them from the viewgroup.
		TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
		for (int index = 0; index < tabWidget.getTabCount(); index++) {
			originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
		}
		tabWidget.removeAllViews();
		
		// Ensure that all tab content childs are not visible at startup.
		for (int index = 0; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}
		
		// Create the tabspec based on the textview childs in the xml file.
		// Or create simple tabspec instances in any other way...
		for (int index = 0; index < originalTextViews.length; index++) {
			final TextView tabWidgetTextView = originalTextViews[index];
			final View tabContentView = tabContent.getChildAt(index);
			TabSpec tabSpec = tabHost.newTabSpec((String) tabWidgetTextView.getTag());
			tabSpec.setContent(new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					return tabContentView;
				}
			});
			if (tabWidgetTextView.getBackground() == null) {
				tabSpec.setIndicator(tabWidgetTextView.getText());
			} else {
				tabSpec.setIndicator(tabWidgetTextView.getText(), tabWidgetTextView.getBackground());
			}
			tabHost.addTab(tabSpec);
		}
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    String tab = extras.getString("tab");
		    if (tab.equals("feed")){
		    	tabHost.setCurrentTab(1);
		    }
		}
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        handler =new Handler();
        Parse.initialize(this, "wYwjvSd8hs05UPISchXs1wEkIxJ7orGX43V1YaF0", "VSb0LZRRBp0lFU7Byd04F3YHHdArTdjgJr3a4iHV");
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		ParseACL.setDefaultACL(defaultACL, true);
		DownloadProfileInfoTask ltt=new DownloadProfileInfoTask();
		ltt.execute(new String[]{});
		TextView info = (TextView)findViewById(R.id.profile_info);
		info.setText(Datas.selectedTaxi.info);
		TextView tv=(TextView)findViewById(R.id.taxiprofiletitle);
		tv.setText(Datas.selectedTaxi.title);
		RatingBar rb = (RatingBar)findViewById(R.id.ratingIndicator);
		rb.setRating((float)Datas.selectedTaxi.rating);
		TextView reviewCount = (TextView)findViewById(R.id.feedbackcount);
		reviewCount.setText(String.valueOf(Datas.selectedTaxi.ratingCount));
		buildTariffList(tariffListData);
		String[] from = { "name","desc","price" };
		int[] to = { R.id.tariffname,  R.id.desc,R.id.price };
		ListView lv=(ListView)findViewById(R.id.tariffslist);
		adapter = new SimpleAdapter(this, tariffListData,R.layout.tariff_layout, from, to){
			public View getView(final int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				if (position % 2 == 0){
				    view.setBackgroundResource(R.drawable.alterselector1);
				} else {
				    view.setBackgroundResource(R.drawable.alterselector2);
				}
				return view;
			}
		};
		adapter.setViewBinder(new MyBinder());
		loading = new TextView(this);
		loading.setText("загрузка ...");
		loading.setTextColor(Color.BLACK);
		loading.setBackgroundResource(R.drawable.loadmore);
		lv.addFooterView(loading);
		lv.setAdapter(adapter);
		buildFeedbackList(feedListData);
		String from2[] = { "owner","rating","content", "date"};
		int[] to2 = { R.id.owner,  R.id.feedbackrating,R.id.content,R.id.feedbackdate };
		feedlistview=(ListView)findViewById(R.id.reviewslist);
		feedadapter = new SimpleAdapter(this, feedListData,R.layout.feedlayout, from2, to2){
			public View getView(final int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				if (position % 2 == 0){
				    view.setBackgroundResource(R.drawable.alterselector1);
				} else {
				    view.setBackgroundResource(R.drawable.alterselector2);
				}
				return view;
			}
		};
		feedadapter.setViewBinder(new MyBinder());
		view_more=new Button(this);
		//view_more.setBackgroundColor(0xF5F5F5);
		view_more.setBackgroundResource(R.drawable.loadmore);
		view_more.setText("загрузка ...");
		view_more.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DownloadFeedbacksTask dft=new DownloadFeedbacksTask();
				dft.execute(new String[]{});
			}});
		feedlistview.addFooterView(view_more);
		Button add_more=new Button(this);
		add_more.setBackgroundResource(R.drawable.loadmore);
		add_more.setText("Добавить");
		add_more.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent showContent = new Intent(getApplicationContext(),AddFeedbackActivity.class);
	   		    startActivity(showContent);
			}});
		feedlistview.addHeaderView(add_more);
		feedlistview.setAdapter(feedadapter);
		if (Datas.selectedTaxi.autopilot){
			ImageView im=(ImageView)findViewById(R.id.imageButton1);
			im.setImageResource(R.drawable.i01_small_est);
		}
		if (Datas.selectedTaxi.autonanny){
			ImageView im=(ImageView)findViewById(R.id.imageButton2);
			im.setImageResource(R.drawable.i02_small_est);
		}
		if (Datas.selectedTaxi.transfer){
			ImageView im=(ImageView)findViewById(R.id.imageButton3);
			im.setImageResource(R.drawable.i03_small_est);
		}
		if (Datas.selectedTaxi.courier){
			ImageView im=(ImageView)findViewById(R.id.imageButton4);
			im.setImageResource(R.drawable.i04_small_est);
		}
		if (Datas.selectedTaxi.bankcard){
			ImageView im=(ImageView)findViewById(R.id.imageButton5);
			im.setImageResource(R.drawable.i05_small_est);
		}
		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pick_phone);
		phones = new String[Datas.selectedTaxi.phones.size()];
		Iterator<String> it=Datas.selectedTaxi.phones.iterator();
		for (int i=0;it.hasNext();i++){
			phones[i]=it.next();
		}
	    builder.setItems(phones, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		Datas.selectedTaxi.call(phones[which]);
	    		String url = "tel:8"+phones[which];
			    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
			    startActivity(intent);
	    	}
	    });
	    ad=builder.create();
		ImageButton bt=(ImageButton)findViewById(R.id.phonenumber);
		//bt.setText(Datas.selectedTaxi.mainphone);
    	bt.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				ad.show();
			    /*UpdateCallCount ucc=new UpdateCallCount();
			    ucc.execute(new String[]{""});*/
			}
    	});
    	if (Datas.favorites.containsKey(Datas.selectedTaxi.id)){
    		ImageButton profileFavorite = (ImageButton)findViewById(R.id.profileFavorite);
    		profileFavorite.setImageResource(R.drawable.tofavorite);
    	}
    	else{
    		ImageButton profileFavorite = (ImageButton)findViewById(R.id.profileFavorite);
    		profileFavorite.setImageResource(R.drawable.tofavorite_inactive);
    	}
    }
	private HashMap<String, String> putData(String owner, String content, Date date, double rating) {
		System.out.println(content+date.toString());
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("owner", owner);
		item.put("content", content);
		SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");
		item.put("date", sdf.format(date));
		item.put("rating",Double.toString(rating));
		return item;
	}
    private void buildFeedbackList(ArrayList<Map<String, String>> list) {
		int amount = Datas.selectedTaxi.feedbacks.size();
		list.clear();
		
		for (int i=0;i<amount;i++){
			Feedback feed=Datas.selectedTaxi.feedbacks.get(i);
			list.add(putData(feed.owner,feed.content,feed.date,feed.rating));
		}
	}
    private void buildTariffList(ArrayList<Map<String, String>> list) {
		int amount = Datas.selectedTaxi.tariffs.size();
		for (int i=0;i<amount;i++){
			Tariff t=Datas.selectedTaxi.tariffs.get(i);
			list.add(putData(t.name,t.desc,t.price));
		}
	}

    private HashMap<String, String> putData(String name, String desc,int price) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("name", name);
		item.put("desc", desc);
		item.put("price", Integer.toString(price));
		return item;
	}
    private class DownloadProfileInfoTask extends AsyncTask<String,Void,ResponceStates> {
		@Override
		protected ResponceStates doInBackground(String... arg0) {
			try{
				ParseQuery query_tariffs = new ParseQuery("Tariff");
				query_tariffs.whereEqualTo("taxi",Datas.selectedTaxi.id);
				List<ParseObject> tariffs= query_tariffs.find();
				tariff_size = tariffs.size();
				for (int i=0;i<tariffs.size();i++){
					Tariff t=new Tariff(tariffs.get(i));
					Datas.selectedTaxi.tariffs.add(t);
				}
				buildTariffList(tariffListData);
				ParseQuery feedbacks = new ParseQuery("Review");
				feedbacks.whereEqualTo("taxiId", Datas.selectedTaxi.id);
				feedbacks.setLimit(5);
				feedbacks.orderByDescending("createdAt");
				List<ParseObject> feeds = feedbacks.find();
				feeds_size = feeds.size();
				Datas.selectedTaxi.feedbacks.clear();
				for (int j=0;j<feeds.size();j++){
					Feedback fd=new Feedback(feeds.get(j));
					//System.out.println(fd.content);
					Datas.selectedTaxi.feedbacks.add(fd);
				}
				buildFeedbackList(feedListData);
				handler.post(new Runnable(){
					@Override
					public void run() {
						if (feeds_size==5)
							view_more.setText("Загрузить еще");
						else if (feeds_size<5)
							view_more.setVisibility(View.GONE);
						System.out.println(tariff_size);
						if (tariff_size==0)
							loading.setText("нет данных о тарифах");
						else
							loading.setVisibility(View.INVISIBLE);
						feedadapter.notifyDataSetChanged();
						adapter.notifyDataSetChanged();
					}});
				return null;
			}catch(Exception e){}
			return null;
		}
    	
    }
    int feeds_size,tariff_size;
	private class DownloadFeedbacksTask extends AsyncTask<String, Void, ResponceStates> {
		@Override
		protected ResponceStates doInBackground(String... arg0) {
			ParseQuery subquery = new ParseQuery("Review");
			subquery.whereEqualTo("taxiId",Datas.selectedTaxi.id);
			subquery.addDescendingOrder("createdAt");
			//subquery.setLimit(10);
			try{
				List<ParseObject> ratings = subquery.find();
				Datas.updateFeedbacks(ratings);
				buildFeedbackList(feedListData);
				handler.post(new Runnable(){
					@Override
					public void run() {
						feedadapter.notifyDataSetChanged();
					}});
			}catch (ParseException e) {
				return ResponceStates.PARSEEXCEPTION;
			}
			return ResponceStates.ITISOK;
		}
    }
    boolean connected;
    public void showFeedbacks(View view){
		//Button b=(Button)findViewById(R.id.feedbacksnum);
		//b.setText("Загрузка ...");
		ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		connected = cm.getActiveNetworkInfo() != null && 
		       cm.getActiveNetworkInfo().isConnectedOrConnecting();
		if (!connected){
			Log.d("noinet", "noinet");
			Toast.makeText(this, "На вашем телефоне нет доступа к интернету. Включите интернет или же попробуйте позже", Toast.LENGTH_LONG).show();
		}
		else{
			Log.d("noinet", "inet");
			//Toast.makeText(this, "Почему-то нет соединения с сервером! :(", Toast.LENGTH_LONG).show();
			ProgressBar x=(ProgressBar)findViewById(R.id.loadFeedbacks);
			x.getLayoutParams().height=20;
			x.getLayoutParams().width=20;
			x.setVisibility(ProgressBar.VISIBLE);
			x.invalidate();

	    	DownloadFeedbacksTask task = new DownloadFeedbacksTask();
	        try {
				ResponceStates ans = task.execute(new String[] { String.valueOf(Datas.selectedTaxi) }).get();
				if (ans==ResponceStates.SERVERNOTAVAILABLE){
					Toast.makeText(this, "На вашем телефоне нет доступа к интернету. Включите интернет или же попробуйте позже", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_taxi_profile, menu);
        return true;
    }
    public void toFavorites(View v){
    	if (Datas.favorites.containsKey(Datas.selectedTaxi.id)){
    		Datas.favorites.remove(Datas.selectedTaxi.id);
    		ImageButton profileFavorite = (ImageButton)findViewById(R.id.profileFavorite);
    		profileFavorite.setImageResource(R.drawable.tofavorite_inactive);
    	}
    	else{
        	Datas.favorites.put(Datas.selectedTaxi.id, Datas.selectedTaxi);
    		ImageButton profileFavorite = (ImageButton)findViewById(R.id.profileFavorite);
    		profileFavorite.setImageResource(R.drawable.tofavorite);
    	}
    	try {
			FileOutputStream fos=new FileOutputStream( getFilesDir().getPath().toString()+"/vtaxifavorites.vt");
			ObjectOutputStream oos=new ObjectOutputStream(fos);
			oos.writeObject(Datas.favorites);
			oos.close();

		} catch (Exception e) {
			Toast.makeText(this, "Невозможно записать обновление избранные", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} 
    	
    }

    public void back(View view){
    	Intent showContent = new Intent(getApplicationContext(),Main2Activity.class);
		startActivity(showContent);
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
