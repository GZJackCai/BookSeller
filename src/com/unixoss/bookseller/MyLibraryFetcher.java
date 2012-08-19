package com.unixoss.bookseller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

class MyLibList{
	public String barCode, marcNo, title, author, store;
	public MyLibList(String barCode, String marcNo, String title,
			String author, String store) {
		this.barCode = barCode;
		this.marcNo = marcNo;
		this.title = title;
		this.author = author;
		this.store = store;
	}
}
	
public class MyLibraryFetcher {
	private Context context;
	private static String logUrl, myUrl;
	private static List<NameValuePair> nvps;
	private ArrayList<MyLibList> myLib;
	private String idItem, titleNoInItem, endTimeInItem, htmlBody;
	
	
	public MyLibraryFetcher(Context context, Map<String,String> loginData) {
		this.context = context;
		loginDataHandler(loginData);
		myLib = new ArrayList<MyLibList>();
	}
	
	private void loginDataHandler(Map<String,String> loginData) {
		logUrl = loginData.get("logUrl");
		myUrl = loginData.get("myUrl");
		loginData.remove("logUrl");
		loginData.remove("myUrl");
		nvps = new ArrayList<NameValuePair>();
		for(String key:loginData.keySet()) {
			nvps.add(new BasicNameValuePair(key,loginData.get(key)));
		}
	}
	
    public String fetchData() throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(logUrl);
        HttpResponse response = httpclient.execute(httpget);
        String status = response.getStatusLine().toString();
        if(!status.contains("200")) {
        	return status;
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            Log.e("Cookies", "MyLibraryFetcher.java: Cookies is empty");
        }
        
        
        HttpPost httpost = new HttpPost(logUrl);
        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        response = httpclient.execute(httpost);
        entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
        
        httpget = new HttpGet(myUrl);
        // Create a response handler
        response = httpclient.execute(httpget);
        entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String tmp = null;
        String htmlRet="";
        while((tmp=reader.readLine())!=null) {
        	htmlRet += tmp + "\r\n";
        }
        htmlBody = new String(htmlRet.getBytes("UTF-8"), "UTF-8");

        if(htmlBody.contains("注销")) {
        	status = "0";
        } else {
        	status = "1";
        }
        return status;
    }
    
	public StringBuffer decodeUnicode(String dataStr) {
		dataStr=dataStr.replace("&#x", "\\u");
		final StringBuffer buffer = new StringBuffer();
		String tempStr = "";
		String operStr = dataStr;
		if (operStr != null && operStr.indexOf("\\u") == -1)
			return buffer.append(operStr);
		if (operStr != null && !operStr.equals("")
				&& !operStr.startsWith("\\u")) {
			tempStr = operStr.substring(0, operStr.indexOf("\\u"));
			operStr = operStr.substring(operStr.indexOf("\\u"), operStr
					.length());
		}
		buffer.append(tempStr);
		while (operStr != null && !operStr.equals("")
				&& operStr.startsWith("\\u")) {
			tempStr = operStr.substring(0, 6);
			operStr = operStr.substring(7, operStr.length());
			String charStr = "";
			charStr = tempStr.substring(2, tempStr.length());
			char letter = (char) Integer.parseInt(charStr, 16);
			buffer.append(new Character(letter).toString());
			if (operStr.indexOf("\\u") == -1) {
				buffer.append(operStr);
			} else { 
				tempStr = operStr.substring(0, operStr.indexOf("\\u"));
				operStr = operStr.substring(operStr.indexOf("\\u"), operStr
						.length());
				buffer.append(tempStr);
			}
		}
		return buffer;
	}
    
	/**
	 * 
	 * @param htmlBody
	 * @return
	 */
    public ArrayList<MyLibList> parseData() {
    	String barCode = null, marcNo = null, title = null, author = null, store = null;
		idItem = "(<td[^>]*>)(.*)(?=<(\\/td)>)";
		titleNoInItem = "<a.*?marc_no=([0-9]*)[^>]*>([^<]*)";
        Pattern itemPattern = Pattern.compile(idItem);
        Pattern titleNoPattern = Pattern.compile(titleNoInItem);
        Matcher itemMatcher  = itemPattern.matcher(htmlBody);
        for(int i=0;itemMatcher.find();i++) {
        	Matcher m;
        	String match = itemMatcher.group(2);
        	switch(i) {
        		case 8:
        			barCode = match;
        			break;
        		case 9:
        			m = titleNoPattern.matcher(match);
        			m.find();
        			marcNo = m.group(1);
        			title = decodeUnicode(m.group(2)).toString();
        			break;
        		case 10:
        			author = decodeUnicode(match).toString();
        			break;
        		case 13:
        			store = match;
        			break;
        		case 14:
        			myLib.add(new MyLibList(barCode, marcNo, title,
        					author, store));
        			i=7;
        			break;
        	}
        }
        return myLib;
    }
}