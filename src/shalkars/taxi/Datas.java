package shalkars.taxi;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import shalkars.taxi.R;

import android.text.Editable;
import android.util.Log;

import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Datas{
	protected static int clickedIndex;
	static boolean hasloaded=false;
	//static Taxi taxis []=new Taxi[0];
	static String[] listIDs;
	private static int selectedCity = -1;
	private static String[] cities = {"Алматы","Астана","Караганда","Шымкент","Актобе","Кустанай","Кокшетау","Актау","Атырау","Павлодар","Семей","Тараз","Петропавловск","Уральск","Усть-Каменогорск","Кызылорда","Рудный","Темиртау","Талдыкорган"};
	private static String[] citiesEN = {"Almaty","Astana","Karaganda","Shymkent","Aktobe","Kustanai","Kokshetau","Aktau","Atyrau","Pavlodar","Semey","Taraz","Petropavlovsk","Uralsk","Ustkamenogorsk","Kyzylorda","Rudniy","Temirtau","Taldykorgan"};
	
	static HashMap<String,Taxi> taxies = new HashMap<String,Taxi>();
	static ArrayList<String> taxiTitles = new ArrayList<String>();
	//protected static int selectedTaxi;
	protected static float feedbackrating;
	protected static Editable feedbacktext;
	public static boolean cashed;
	public static boolean loadstarted;
	protected static boolean loadended;
	protected static String selectedID;
	public static Taxi selectedTaxi;
	public static HashMap<String, Taxi> favorites;
	public static String[] favoriteListIDs;
	public static ArrayList<String> deleteList=new ArrayList<String>();
	public static ArrayList<Taxi> orderedTaxies = new ArrayList<Taxi>();
	public static boolean isCitySelected(){
		if (Datas.selectedCity<0)
			return false;
		else
			return true;
	}
	public static void updateFeedbacks(List<ParseObject> ratings) {
		Datas.selectedTaxi.feedbacks.clear();
		for (int i=0;i<ratings.size();i++){
			Datas.selectedTaxi.feedbacks.add(new Feedback(ratings.get(i)));
		}
		//Datas.taxis[Datas.selectedTaxi].feedbacks.clear();
		/*Datas.taxies.get(Datas.selectedID).feedbacks.clear();
		for (int j=0;j<ratings.size();j++){
			Datas.taxies.get(key)
			 Datas.taxies.get(Datas.selectedID).feedbacks.add(new Feedback(ratings.get(j).getDouble("rating"),ratings.get(j).getString("content"),ratings.get(j).getString("owner"),ratings.get(j).getCreatedAt()));
		}*/
	}
	public static void setCity(String city) {
		Datas.selectedCity=Arrays.binarySearch(Datas.cities, city);
		if (Datas.selectedCity<0){
			Datas.selectedCity = 0;
		}
	}
	public static String getCityName() {
			return Datas.cities[Datas.selectedCity];
	}
	public static String getCityName(String lang) {
		if (lang.equals("EN")){
			return Datas.citiesEN[Datas.selectedCity];
		}
		else{
			return Datas.cities[Datas.selectedCity];
		}
	}
	public static CharSequence[] getCities() {
		return Datas.cities;
	}
	public static void setCity(int which) {
		Datas.selectedCity=which;
		if (Datas.selectedCity<0){
			Datas.selectedCity = 0;
		}
	}
	public static void sortTaxies() {
		Collections.sort(Datas.orderedTaxies,new Comparator<Taxi>(){
			@Override
			public int compare(Taxi lhs, Taxi rhs) {
				if (lhs.promoting != rhs.promoting){
					if (lhs.promoting){
						return -1;
					}
					else{
						return 1;
					}
				}
				else{
					if (lhs.rating>rhs.rating){
						return -1;
					}
					else if (lhs.rating<rhs.rating){
						return 1;
					}
					else{
						return (lhs.reviews>rhs.reviews)?-1:1;
					}
				}
			}
		});
	}
}
enum ResponceStates{
	NOINTERNET,SERVERNOTAVAILABLE,YOUHAVENOACCESS,PARSEEXCEPTION, ITISOK
}
class Taxi implements Comparable<Taxi>,Serializable{
	ArrayList<Tariff> tariffs=new ArrayList<Tariff>();
	HashSet<String> phones=new HashSet<String>();
	boolean promoting = false;
	boolean autonanny=false,autopilot=false,bankcard=false,courier=false,transfer=false;
	String city;
	public Taxi(ParseObject po) {
		try{
			rating =0;
			id = po.getObjectId();
			title = po.getString("title");
			mainphone = po.getString("phone");
			reviews = po.getInt("kReviewCount");
			url = po.getString("url");
			//ratingCount = po.getInt("ratingCount");
			ratingCount = po.getInt("kReviewCount");
			System.out.println(po.getInt("ratingCount"));
			promoting = po.getBoolean("promoting");
			city = po.getString("city");
			/*if (po.getInt("ratingCount")==0)
				rating = 0;
			else
				rating = po.getDouble("ratingTotal")/po.getDouble("ratingCount");*/
			rating = po.getDouble("kAverageStars");
			//Log.d("rating of "+title, String.valueOf(rating));
			info = po.getString("info");
			email = po.getString("email");
			address = po.getString("address");
			autonanny = po.getBoolean("sAutoNanny");
			autopilot = po.getBoolean("sAutoPilot");
			bankcard = po.getBoolean("sBankCard");
			courier = po.getBoolean("sCourier");
			transfer = po.getBoolean("sTransfer");
			/*
			JSONArray ja2= po.getJSONArray("tariffs");
			
			tariff=new Tariff[(ja2==null)?0:ja2.length()];
			for (int i=0;i<tariff.length;i++){
				JSONObject jo=ja2.getJSONObject(i);
				String desc=jo.getString("desc");
				String name=jo.getString("name");
				tariff[i]=new Tariff(desc,name);
			}*/
			phones.add(po.getString("phone"));
		}catch(Exception e){
			e.printStackTrace();
			Log.d("JSON error",e.toString());
		}
	}
	public Taxi() {
		title="notitle";
	}
	String address="";
	String email="";
	String url="";
	String info="";
	String id;
	String title;
	String mainphone;
	//Tariff tariff[]=new Tariff[0];
	int reviews=0;
	public double rating=0;
	public ArrayList<Feedback> feedbacks=new ArrayList<Feedback>();
	public int ratingCount;
	public int compareTo(Taxi other) {
		//Log.d("compare", String.valueOf(other.rating));
		if (other.rating>rating)
			return -1;
		else if (other.rating==rating){
			return 0;
		}
		else
			return 1;
	}
	public String toString(){
		return this.title;
	}
	public boolean filter(HashMap<String, Boolean> filterStates) {
		try{
		if (!autonanny && filterStates.get("autonanny"))
			return false;
		if (!autopilot && filterStates.get("autopilot"))
			return false;
		if (!this.transfer && filterStates.get("transfer"))
			return false;
		if (!courier && filterStates.get("courier"))
			return false;
		if (!bankcard && filterStates.get("bankcard"))
			return false;
		}catch(Exception e){System.out.println(filterStates);}
		return true;
	}
	public void call(String string) {
		ParseObject p=new ParseObject("CallCounter");
		p.put("city", city);
		p.put("device", ParseInstallation.getCurrentInstallation().getInstallationId());
		p.put("deviceType", "android");
		p.put("favorite", Datas.favorites.containsKey(this.id));
		p.put("phone", string);
		p.put("taxi", this.id);
		p.saveEventually();
		
	}
	
}
class Tariff implements Serializable{
	String desc,name,taxiID;
	int price;
	public Tariff(String desc2, String name2) {
		desc=desc2;name=name2;
	}
	public Tariff(ParseObject po) {
		desc=po.getString("desc");
		name=po.getString("name");
		price = po.getInt("price");
		taxiID = po.getString("taxi");
	}
}
class Feedback implements Serializable{
	double rating; String content,owner;Date date;
	/*public Feedback(double rate, String cont, String own, Date d) {
		rating=(float)rate;content=cont;owner=own;date = d;
		//Log.d("date", date.toString());
	}*/
	public Feedback(ParseObject x) {
		content = x.getString("content");
		owner = x.getString("owner");
		rating = x.getDouble("rating");
		date = x.getCreatedAt();
	}
}
