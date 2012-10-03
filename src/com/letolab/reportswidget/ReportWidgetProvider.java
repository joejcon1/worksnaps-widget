package com.letolab.reportswidget;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

public class ReportWidgetProvider extends AppWidgetProvider {
	private static String key = "iM7NSisGte1Wfmp8Is1MUKRUpGkhSVDqazwJ8o88";
	private static String SERVER_URL = "http://www.worksnaps.net/api/";
	public enum TimeSpan {TODAY, YESTERDAY, THIS_WEEK, LAST_WEEK, THIS_MONTH, LAST_MONTH}
	public TimeSpan span = TimeSpan.TODAY;


	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews;
		ComponentName watchWidget;
		int minutes = getMinutesTotal();
		int hours = minutes / 60;
		minutes = minutes % 60;
		
		
		remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget_layout );
		watchWidget = new ComponentName( context, ReportWidgetProvider.class );
		remoteViews.setTextViewText( R.id.label, hours+ ":"+minutes);
		appWidgetManager.updateAppWidget( watchWidget, remoteViews );
	}
	
	
	
	private int getMinutesTotal(){
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("key", toBase64(key, ""));
		args.put("urls", formUrls());
		APICall task = new APICall();
		ArrayList<String> responses = null;
		try {
			responses = task.execute(args).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int minutes = 0;
		for(String s:responses){
			String formatted = formatResponseString(s);
			XMLHashMap<String, String> dict = (XMLHashMap<String, String>)parseXMLResponse(formatted);
			minutes += Integer.parseInt(dict.get("duration_in_minutes"));
		}
		return minutes;
	}
	
	
	
	
	public ArrayList<String> formUrls(){
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<String> projects = getProjectCodes();
		String user_id = getUserID();
		String start = getStartTime();
		String end = getEndTime();

		for(String projectCode: projects){
			String ret = SERVER_URL;

			ret = ret + "projects/"+projectCode+"/";
			ret = ret + "reports?name=time_summary&from_timestamp="+start;
			ret = ret + "&user_ids="+user_id;
			ret = ret + "&to_timestamp="+end+"&time_entry_type=online";
			urls.add(ret);
		}

		return urls;
	}
	private ArrayList<String> getProjectCodes(){
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("key", toBase64(key, ""));
		args.put("urls", formUrls());
		APICall task = new APICall();
		ArrayList<String> responses = null;
		try {
			responses = task.execute(args).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String s:responses){
			String formatted = formatResponseString(s);
			XMLHashMap<String, String> dict = (XMLHashMap<String, String>)parseXMLResponse(formatted);
			minutes += Integer.parseInt(dict.get("duration_in_minutes"));
		}
		ArrayList<String> rets = new ArrayList<String>();
		/*
		 * TODO: make api call to get list of projects
		 */
		rets.add("3818"); //3813 is SOA
		return rets;
	}
	private String getStartTime(){
		Calendar c = Calendar.getInstance();
		long millis = 0;
		switch(span){
			case TODAY:
				c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 1);
				break;
	
			case YESTERDAY:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE-1, 0, 0, 0);
				break;
	
			case THIS_WEEK:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 0, 0, 0);
				break;
	
			case LAST_WEEK:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 0, 0, 0);
				break;
	
			case THIS_MONTH:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 0, 0, 0);
				break;
	
			case LAST_MONTH:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 0, 0, 0);
				break;

		}
		Log.w("DATE","Start: " + c.getTime().toString());
		millis = c.getTimeInMillis()/1000;
		return String.valueOf(millis);
	}
	private String getEndTime(){
		Calendar c = Calendar.getInstance();
		long millis = 0;
		switch(span){
			case TODAY:
				c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE),  23, 59, 59);
				break;
	
			case YESTERDAY:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE-1, 23, 59, 59);
				break;
	
			case THIS_WEEK:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 23, 59, 59);
				break;
	
			case LAST_WEEK:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 23, 59, 59);
				break;
	
			case THIS_MONTH:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 23, 59, 59);
				break;
	
			case LAST_MONTH:
				c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, 23, 59, 59);
				break;

		}
		Log.w("DATE","End: " + c.getTime().toString());
		millis = c.getTimeInMillis()/1000;
		return String.valueOf(millis);
	}
	private String getUserID(){
		/*
		 * TODO: get user id from api call for me.xml
		 */
		return "2285";
	}
	public static String makeXMLObject(HashMap<String, String> dict){
		String key = "";
		String value = "";
		String ret = "";
		for(Object k:dict.keySet()){
			key = (String) k;
			value = dict.get(k);
			ret = ret+ "<"+key+">"+value+ "</"+key+">";
		}
		return ret;

	}

	private static HashMap<String,String> parseXMLResponse(String xml){
		HashMap<String,String> dict = new HashMap<String,String>();

		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();

			is.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(is);
			return new XMLHashMap<String, String>(doc);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}


		return dict;
	}
	private String toBase64(String username, String password){
		String pre = username+":"+password;
		String ret="Basic "+Base64.encodeToString(pre.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
		return ret;
		
	}
	private String formatResponseString(String result){
		result = result.replace("<?xml version=\"1.0\"?>", "");
		
		//because the api returns 2 xml elements we need to wrap in a single root element
		result =  "<xml>"+result+"</xml>";
		return result;
	}
	static class APICall extends AsyncTask<HashMap<String, Object>, Integer, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(HashMap<String, Object>... content) {
			boolean debugNetworkCall = false;
			String key = (String) content[0].get("key");
			@SuppressWarnings("unchecked") //we definitely know what this class is
			ArrayList<String> urls = (ArrayList<String>) content[0].get("urls");

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = null;
			ArrayList<String> responses = new ArrayList<String>();
			for(String url:urls){
				Log.w("APICALL", url);
				httpget = new HttpGet(url);
				httpget.addHeader("Authorization", key);
				
				if (debugNetworkCall) {
					Log.i("APICALL", "request");
					printHeaders(httpget.getAllHeaders());
				}
				try {

					HttpResponse httpresponse = httpclient.execute(httpget);
					HttpEntity resEntity = httpresponse.getEntity();
					responses.add(IOUtils.toString(resEntity.getContent()));
					
					if (debugNetworkCall) {
						Log.i("APICALL", "response");
						printHeaders(httpresponse.getAllHeaders());
						Log.e("REPORTS", " "+ httpresponse.getStatusLine().getStatusCode());
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return responses;
		}  
		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
			Log.w("APICALL","Progress update: " + progress);
		}

		protected void onPostExecute(String result) {
			//print result
//			
//			Log.i("APICALL",result);
//			String mins = parseXMLResponse(result).get("duration_in_minutes");
//			Log.w("APICALL","Network Call Complete\n" + mins);

		}
		private void printHeaders(Header[] headers){
			for(Header h:headers){
				Log.i("APICALL",h.getName() +" "+h.getValue());
			}
		}
	}
}