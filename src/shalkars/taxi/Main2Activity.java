package shalkars.taxi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import android.widget.TabHost.TabContentFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.google.analytics.tracking.android.EasyTracker;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import shalkars.taxi.Datas;
import shalkars.taxi.MyBinder;
import shalkars.taxi.Taxi;
import shalkars.taxi.TaxiProfileActivity;
import shalkars.taxi.R;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.ToggleButton;

public class Main2Activity extends Activity {
	boolean connected =false;
	Handler handler;
	SharedPreferences settings;
	AlertDialog citylist,phonelist;
	HashMap<String,Boolean> filterStates=new HashMap<String,Boolean>();
	ArrayList<Map<String,String>> taxiList = new ArrayList<Map<String,String>>();
	ArrayList<Map<String,String>> favoriteList = new ArrayList<Map<String,String>>();
	SimpleAdapter listAdapter;TabHost tabHost;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
			FileInputStream fos=new FileInputStream(new File(getFilesDir().getPath().toString()+"/vtaxifavorites.vt"));
        	ObjectInputStream oos=new ObjectInputStream(fos);
			Datas.favorites = (HashMap<String,Taxi>)oos.readObject();
			oos.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.toString());
			Datas.favorites=new HashMap<String,Taxi>();
			//Datas.favorites.put("dad",new Taxi());
		}
		Parse.initialize(Main2Activity.this, "wYwjvSd8hs05UPISchXs1wEkIxJ7orGX43V1YaF0", "VSb0LZRRBp0lFU7Byd04F3YHHdArTdjgJr3a4iHV");
		JSONArray favs=new JSONArray(Datas.favorites.keySet());
		PushService.setDefaultPushCallback(this, Main2Activity.class);
		ParseInstallation.getCurrentInstallation().put("favorites", favs);
		ParseInstallation.getCurrentInstallation().put("testing", "true");
		ParseInstallation.getCurrentInstallation().saveInBackground();
		ParseAnalytics.trackAppOpened(getIntent());
		PushService.subscribe(Main2Activity.this, "", Main2Activity.class);
        handler = new Handler();
        setContentView(R.layout.activity_main);	
        try{
			FileInputStream fos=new FileInputStream(new File(getFilesDir().getPath().toString()+"/vtaxifavorites.vt"));
        	ObjectInputStream oos=new ObjectInputStream(fos);
			Datas.favorites = (HashMap<String,Taxi>)oos.readObject();
			oos.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.toString());
			Datas.favorites=new HashMap<String,Taxi>();
			//Datas.favorites.put("dad",new Taxi());
		}
		ProgressBar x=(ProgressBar)findViewById(R.id.progressBar1);
		x.setVisibility(ProgressBar.GONE);
		findViewById(R.id.textView1).setVisibility(ProgressBar.GONE);
		
        buildFavoriteList(favoriteList);
        String[] from1 = { "title","rating","id","ratingCount","id","id","autopilot","autonanny","transfer","courier","bankcard" };
		int[] to1 = { R.id.label,  R.id.ratingIndicator,R.id.phone,R.id.ratingCount,R.id.toProfile,R.id.taxiline,R.id.imageButton1,R.id.imageButton2,R.id.imageButton3,R.id.imageButton4,R.id.imageButton5 };
		
		ListView lv2=(ListView)findViewById(R.id.favoritelist);
		listAdapter = new SimpleAdapter(this, favoriteList,R.layout.row_layout, from1, to1);
		listAdapter.setViewBinder(new MyBinder());
		lv2.setAdapter(listAdapter);
		lv2.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Datas.selectedID = Datas.listIDs[position];
				Datas.selectedTaxi=Datas.taxies.get(Datas.selectedID);
				Intent showContent = new Intent(getApplicationContext(),TaxiProfileActivity.class);
	   		    startActivity(showContent);
	   		    System.out.println("list item pressed");
			}
		});
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
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
			final View tabContentView = tabContent.getChildAt(index);
			final TextView tabWidgetTextView = originalTextViews[index];
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

        ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		connected = cm.getActiveNetworkInfo() != null && 
		       cm.getActiveNetworkInfo().isConnectedOrConnecting();
		settings = getSharedPreferences("taxi",0);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pick_city);
	    builder.setItems(Datas.getCities(), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		Datas.setCity(which);
	    		PushService.subscribe(Main2Activity.this, Datas.getCityName("EN"), Main2Activity.class);
	    		
	    		
	    		Editor ed=settings.edit();
	    		System.out.println("----");
	    		ed.putString("city", Datas.getCityName());
	    		ed.commit();
	    		Datas.orderedTaxies.clear();
	    		taxiList.clear();
	    		buildList(taxiList);
	    		Button bt=(Button)findViewById(R.id.citybutton);
	    		bt.setText(Datas.getCityName());
	    		TextView tv=(TextView)findViewById(R.id.settingcity);
	    		tv.setText(Datas.getCityName());
	    		PushService.subscribe(Main2Activity.this, Datas.getCityName("EN"), Main2Activity.class);
	    		ProgressBar x=(ProgressBar)findViewById(R.id.progressBar1);
	    		//x.setIndeterminate(true);
	    		x.setVisibility(ProgressBar.VISIBLE);
	    		TextView t = (TextView)findViewById(R.id.textView1);
	    		t.setVisibility(View.VISIBLE);
	    		listAdapter.notifyDataSetChanged();
	    		LoadTaxis ltt=new LoadTaxis();
	    		ltt.execute(new String[]{});
	    		tabHost.setCurrentTab(0);
	    	}

	    });
	    citylist=builder.create();

		buildList(taxiList);
		String[] from = { "title","rating","id","ratingCount","id","id","autopilot","autonanny","transfer","courier","bankcard" };
		int[] to = { R.id.label,  R.id.ratingIndicator,R.id.phone,R.id.ratingCount,R.id.toProfile,R.id.taxiline,R.id.imageButton1,R.id.imageButton2,R.id.imageButton3,R.id.imageButton4,R.id.imageButton5 };
		ListView lv=(ListView)findViewById(R.id.list);
		listAdapter = new SimpleAdapter(this, taxiList,R.layout.row_layout, from, to);
		listAdapter.setViewBinder(new MyBinder());
		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Datas.selectedID = Datas.listIDs[position];
				Datas.selectedTaxi=Datas.taxies.get(Datas.selectedID);
	   		    //System.out.println("list item presse");
				Intent showContent = new Intent(getApplicationContext(),TaxiProfileActivity.class);
	   		    startActivity(showContent);
			}
		});
        String city = settings.getString("city", "none");
        //Log.d("city", city+String.valueOf(Datas.selectedCity));
		if (!Datas.isCitySelected() && city.equals("none")){
		    citylist.show();
		}
		else if (!city.equals("none")&&!Datas.isCitySelected()){
			Datas.setCity(city);
    		PushService.subscribe(Main2Activity.this, Datas.getCityName("EN"), Main2Activity.class);
			Button bt=(Button)findViewById(R.id.citybutton);
			TextView tv=(TextView)findViewById(R.id.settingcity);
			bt.setText(Datas.getCityName());
			tv.setText(Datas.getCityName());
			LoadTaxis ltt=new LoadTaxis();
			
			ltt.execute(new String[]{});
			PushService.subscribe(Main2Activity.this, Datas.getCityName("EN"), Main2Activity.class);
		}
		else {
			Button bt=(Button)findViewById(R.id.citybutton);
			bt.setText(Datas.getCityName());
			TextView tv=(TextView)findViewById(R.id.settingcity);
			tv.setText(Datas.getCityName());
			PushService.subscribe(Main2Activity.this, Datas.getCityName("EN"), Main2Activity.class);
		}
		//PushService.setDefaultPushCallback(this.getApplicationContext(), Main2Activity.class);
		filterStates.put("autonanny",false);
		filterStates.put("autopilot",false);
		filterStates.put("bankcard", false);
		filterStates.put("courier", false);
		filterStates.put("transfer", false);
	}
    public void buildFavoriteList(ArrayList<Map<String, String>> list) {
    	list.clear();
		int amount = Datas.favorites.size();
		ArrayList<Taxi> favs = new ArrayList<Taxi>(Datas.favorites.values());
		Datas.favoriteListIDs=new String[amount];
		for (int i=0,listIndex=0;i<amount;i++){
			System.out.println("====>>>>"+favs.get(i)+" "+favs.get(i).rating+" "+favs.get(i).ratingCount);
			list.add(putData(favs.get(i).id,favs.get(i).title,favs.get(i).rating,favs.get(i).ratingCount));
			Datas.favoriteListIDs[listIndex++]=favs.get(i).id;
		}
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 	getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	public void toProfile(View view){
		System.out.println(view.toString()+view.getTag()+view.getId());
		Datas.selectedID = view.getTag().toString();
		Datas.selectedTaxi=Datas.taxies.get(Datas.selectedID);
		Intent showContent = new Intent(getApplicationContext(),TaxiProfileActivity.class);
		    startActivity(showContent);
	}
    public void changeCity(View view){
    	citylist.show();
    }

    class LoadTaxis extends AsyncTask<String, Void, String>{
    	protected void onPreExecute(){

			Datas.taxies.clear();
			Datas.orderedTaxies.clear();

			listAdapter.notifyDataSetChanged();
    	}
		@Override
		protected String doInBackground(String... arg0) {
			Log.d("run", "run");
			
			
			//ParseUser.enableAutomaticUser();
			//ParseACL defaultACL = new ParseACL();
			//ParseACL.setDefaultACL(defaultACL, true);
			try {
				ParseQuery query = new ParseQuery("taxi");
				query.whereEqualTo("city", Datas.getCityName());
				query.orderByDescending("promoting");
				query.addDescendingOrder("kReviewAverageStars");
				query.addDescendingOrder("kReviewRatingTotal");
				//query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
				query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
				Datas.cashed = query.hasCachedResult();
				if (!Datas.cashed&&!connected)
					return "nointernet";
				handler.post(new Runnable(){
					@Override
					public void run() {
						ProgressBar x=(ProgressBar)findViewById(R.id.progressBar1);
						//x.setIndeterminate(false);
					}});
				List<ParseObject> evacs = query.find();
				int size = evacs.size();
				//Log.d("taxis", String.valueOf(size));
	             for (int i=0;i<size;i++){
	            	 Taxi t = new Taxi(evacs.get(i));
	            	 Datas.orderedTaxies.add(t);
	            	 Datas.taxies.put(t.id,t);
	            	 System.out.println(t.title+" "+t.promoting+" "+t.rating+" "+t.ratingCount);
	             }
	             Datas.sortTaxies();
	             /*
				ParseQuery query_tariffs = new ParseQuery("Tariff");
				query_tariffs.whereContainedIn("taxi",Datas.taxies.keySet());
				List<ParseObject> tariffs= query_tariffs.find();
				for (int i=0;i<tariffs.size();i++){
					Tariff t=new Tariff(tariffs.get(i));
					try{
					Datas.taxies.get(t.taxiID).tariffs.add(t);
					}catch(NullPointerException ne){}
				}
				ArrayList<Taxi> taxies=new ArrayList<Taxi>(Datas.taxies.values());
				for (int i=0;i<taxies.size();i++){
					ParseQuery feedbacks = new ParseQuery("Review");
					feedbacks.whereEqualTo("taxiId", taxies.get(i).id);
					feedbacks.setLimit(5);
					feedbacks.orderByDescending("createdAt");
					List<ParseObject> feeds = feedbacks.find();
					for (int j=0;j<feeds.size();j++){
						ParseObject x=feeds.get(j);
						Feedback fd=new Feedback(x);
						taxies.get(i).feedbacks.add(fd);
					}
				}*/
				ParseQuery query_phones=new ParseQuery("Phone");
				query_phones.whereContainedIn("taxi",Datas.taxies.keySet());
				List<ParseObject> phones = query_phones.find();
				for (int i=0;i<phones.size();i++){
					String id = phones.get(i).getString("taxi");
					String number = phones.get(i).getString("number");
					try{
						Datas.taxies.get(id).phones.add(number);
					}catch(Exception e){System.out.println(id+Datas.taxies.get(id)+Datas.taxies.size());}
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
				Log.d("internet", e1.toString()+e1.getMessage());
			}
			
			Log.d("list builded", ">>>");
			handler.post(new Runnable(){
				@Override
				public void run() {
					listAdapter.notifyDataSetChanged();
					ProgressBar x=(ProgressBar)findViewById(R.id.progressBar1);
					x.setVisibility(View.GONE);
					//x.setIndeterminate(false);
					TextView tv=(TextView)findViewById(R.id.textView1);
					tv.setVisibility(View.GONE);
					buildList(taxiList);
				}
			});
	    	
			return "none";
		}
    	
    }
    private void buildList(ArrayList<Map<String, String>> list) {
    	list.clear();
		int amount = Datas.orderedTaxies.size();
		Datas.listIDs=new String[amount];
		for (int i=0,listIndex=0;i<amount;i++){
			if (Datas.orderedTaxies.get(i).filter(filterStates)){
				Taxi taxi = Datas.orderedTaxies.get(i);
				list.add(putData(taxi.id,taxi.title,taxi.rating,taxi.ratingCount));
				Datas.listIDs[listIndex++]=taxi.id;
			}
		}
	}
    private HashMap<String, String> putData(String id,String title, Double rating,Integer ratingCount) {
		HashMap<String, String> item = new HashMap<String, String>();
		Taxi  taxi = Datas.taxies.get(id);
		if (taxi==null){
			taxi = Datas.favorites.get(id);
		}
		if (taxi==null){
			taxi = new Taxi();
		}
		item.put("title", title);
		//item.put("mainphone", mainphone);
		item.put("rating", Double.toString(rating));
		item.put("id",id);
		item.put("ratingCount","("+Integer.toString(ratingCount)+")");
		item.put("autopilot",String.valueOf(taxi.autopilot));
		item.put("autonanny", String.valueOf(taxi.autonanny));
		item.put("transfer",String.valueOf( taxi.transfer));
		item.put("courier", String.valueOf(taxi.courier));
		item.put("bankcard", String.valueOf(taxi.bankcard));
		//item.put(key, value);
		return item;
	}
    public void writeToApp(View v){
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse("market://details?id=shalkars.taxi"));
    	startActivity(intent);
    }
    public void writeToSupport(View v){

		Intent showContent = new Intent(getApplicationContext(),WriteSupportActivity.class);
		startActivity(showContent);
    }
    public void deleteFavorite(View v){
    	Button b=(Button)v;
    	b.setText("удалено");
    	String delID=(String)v.getTag();
    	Datas.deleteList.add(delID);
    }
    public void editFavorites(View v){
    	ToggleButton tb=(ToggleButton)v;
		ListView lv2=(ListView)findViewById(R.id.favoritelist);
		if (!tb.isChecked()){
    		for (int j=0;j<Datas.deleteList.size();j++){
	        	Datas.favorites.remove(Datas.deleteList.get(j));
    		}
        	listAdapter.notifyDataSetChanged();
        	try {
    			FileOutputStream fos=new FileOutputStream( getFilesDir().getPath().toString()+"/vtaxifavorites.vt");
    			ObjectOutputStream oos=new ObjectOutputStream(fos);
    			oos.writeObject(Datas.favorites);
    			oos.close();
    			
    		} catch (Exception e) {
    			Toast.makeText(this, "Невозможно записать в избранные", Toast.LENGTH_LONG).show();
    			e.printStackTrace();
    		} 
		}
		for (int i=0;i<lv2.getChildCount();i++){
	    	if (tb.isChecked()){
	    		lv2.getChildAt(i).findViewById(R.id.deletefavorite).setVisibility(View.VISIBLE);
	    	}
	    	else{
	    		lv2.getChildAt(i).findViewById(R.id.deletefavorite).setVisibility(View.GONE);
	    	}
		}
    }
    public void filter(View v){
    	System.out.println("filter!!");
    	ToggleButton tb=(ToggleButton)v;
    	filterStates.put(tb.getTag().toString(),tb.isChecked());
    	buildList(taxiList);
    	listAdapter.notifyDataSetChanged();
    }
    String phones[];
    Taxi calledTaxi = null;
    public void call(View v){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pick_phone);
		calledTaxi = Datas.taxies.get(v.getTag());
		phones = new String[calledTaxi.phones.size()];
		Iterator<String> it=calledTaxi.phones.iterator();
		
		for (int i=0;it.hasNext();i++){
			phones[i]=it.next();
		}
	    builder.setItems(phones, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		calledTaxi.call(phones[which]);
	    		String url = "tel:8"+phones[which];
			    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
			    startActivity(intent);
	    	}
	    });
	    phonelist=builder.create();
	    builder.show();
    	System.out.println("Call is called");
    	System.out.println(v.getTag());
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

class MyBinder implements ViewBinder{
    public boolean setViewValue(View view, Object data, String textRepresentation) {
    	if (view.getId() == R.id.desc){
    		String text = (String)data;
    		if (text != null && text.length()==0){
    			TextView tv = (TextView)view;
    			tv.setVisibility(View.GONE);
    		}
    		return true;
    	}
    	if (view.getId() == R.id.imageButton1||view.getId() == R.id.imageButton2||view.getId() == R.id.imageButton3||view.getId() == R.id.imageButton4||view.getId() == R.id.imageButton5){
    		if (Boolean.parseBoolean(textRepresentation)){
        		ImageButton im = (ImageButton)view;
        		switch (view.getId()){
        			case R.id.imageButton1:
        				im.setImageResource(R.drawable.i01_small_est);
        				break;
        			case R.id.imageButton2:
        				im.setImageResource(R.drawable.i02_small_est);
        				break;
        			case R.id.imageButton3:
        				im.setImageResource(R.drawable.i03_small_est);
        				break;
        			case R.id.imageButton4:
        				im.setImageResource(R.drawable.i04_small_est);
        				break;
        			case R.id.imageButton5:
        				im.setImageResource(R.drawable.i05_small_est);
        				break;
        		}
        		
    		}
    		return true;
    	}
    	if (view.getId() == R.id.taxiline){
    		view.setTag((String)data);
    		return true;
    	}
    	if (view.getId() == R.id.deletefavorite){
    		Button bt=(Button)view;
    		bt.setTag((String)data);
    		bt.setText("удал.");
    		return true;
    	}
    	if (view.getId() == R.id.phone){
    		ImageButton bt=(ImageButton)view;
    		bt.setTag((String)data);
    		//bt.setText("Позвонить");
    		return true;
    	}
    	if (view.getId() == R.id.toProfile){
    		System.out.println("to Profile");
    		ImageButton bt=(ImageButton)view;
    		bt.setTag((String)data);
    		//bt.setText("Позвонить");
    		return true;
    	}
        if(view.getId() == R.id.ratingIndicator||view.getId() == R.id.feedbackrating){
            String stringval = (String) data;
            float ratingValue = Float.parseFloat(stringval);
            RatingBar ratingBar = (RatingBar) view;
            ratingBar.setRating(ratingValue);
            return true;
        }
        return false;
    }
}