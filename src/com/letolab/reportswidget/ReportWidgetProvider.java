package com.letolab.reportswidget;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class ReportWidgetProvider extends AppWidgetProvider {
	//private static String key = "aU03TlNpc0d0ZTFXZm1wOElzMU1VS1JVcEdraFNWRHFhendKOG84ODo
	private static String key = "aU03TlNpc0d0ZTFXZm1wOElzMU1VS1JVcEdraFNWRHFhendKOG84ODo=";
	private static String SERVER_URL = "http://www.worksnaps.net/api//projects/3818/reports?name=time_summary&from_timestamp=1348617600&user_ids=2285&to_timestamp=1351209600&time_entry_type=online";



	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews;
		ComponentName watchWidget;

		APICall task = new APICall();
		try {
			task.execute(key).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DateFormat format = SimpleDateFormat.getTimeInstance( SimpleDateFormat.MEDIUM, Locale.getDefault() );

		remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget_layout );
		watchWidget = new ComponentName( context, ReportWidgetProvider.class );
		remoteViews.setTextViewText( R.id.label, "Time = " + format.format( new Date()));
		appWidgetManager.updateAppWidget( watchWidget, remoteViews );
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

	static class APICall extends AsyncTask<String, Integer, HashMap<String,String>> {

		@Override
		protected HashMap<String,String> doInBackground(String... content) {
			String response = "";
			
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = null;
			
			httpget = new HttpGet(SERVER_URL);
			httpget.addHeader("Authorization", "Basic "+key);
			try {

				HttpResponse httpresponse = httpclient.execute(httpget);
				HttpEntity resEntity = httpresponse.getEntity();
				response = IOUtils.toString(resEntity.getContent());
				Log.e("REPORTS", " "+httpresponse.getStatusLine().getStatusCode());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return parseXMLResponse(response);
		}  
		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
			System.out.println("Progress update: " + progress);
		}

		protected void onPostExecute(HashMap<String,String> result) {
			//print result
			System.out.println("Network Call Complete\n" + result);

		}
	}
}