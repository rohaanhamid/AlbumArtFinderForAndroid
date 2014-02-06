package com.example.httprequesttest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;



public class GetCoverArtFromReleaseIDTask extends AsyncTask<String, Integer, String>{
	String ns=null;
	
	public static class ReleaseInfo{
		public String releaseID;
		public String releaseTitle;
		
		public ReleaseInfo(String releaseID, String releaseTitle) {
			this.releaseID = releaseID;
			this.releaseTitle = releaseTitle;
		}	
	}
	
    public static class Recording {
	    public ReleaseInfo releaseInfo;
	    public String artistName;
		public Recording(String recordingTitle, String releaseTitle,
				String releaseID, String artistName) {
			this.releaseInfo = new ReleaseInfo(releaseID, releaseTitle);
			this.artistName = artistName;
		}    
    }

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		String albumName=params[0];
		String artistName=params[1];
		String musicBrainzSearchUrl="http://www.musicbrainz.org/ws/2/recording/?query="+albumName+"%20AND%20artist:"+artistName;
		
		InputStream musicBrainzSearchIs=retrieveXMLStreamFromURL(musicBrainzSearchUrl);
		
		if(musicBrainzSearchIs==null)
		{
			return null;
		}
		
		String releaseID=null;
		
		try {
			releaseID=parseSearchXMLStreamForReleaseID(musicBrainzSearchIs);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private InputStream retrieveXMLStreamFromURL(String URL){
		HttpResponse response;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet=new HttpGet(URL);
		InputStream is=null;

		try {
			response = httpclient.execute(httpGet);
			StatusLine statusLine=response.getStatusLine();
			int statusLineCode=statusLine.getStatusCode();
			
			if(statusLineCode==200)
			{
				HttpEntity httpEntity = response.getEntity();
				is=httpEntity.getContent();
			}
			else 
			{
	            Log.i("HttpRequestFailureReason", statusLine.getReasonPhrase());
	        }  		
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return is;	
	}
	
    public String parseSearchXMLStreamForReleaseID(InputStream in) throws XmlPullParserException, IOException {
    	List recordings=new ArrayList<Recording>();
    	
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            //parser.nextTag();
            //return readFeed(parser);
            recordings=readSearchMetadata(parser);
        } finally {
            in.close();
        }
		return null;
    }
	
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
     }
    
    private List readSearchMetadata(XmlPullParser parser)throws XmlPullParserException, IOException{
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "metadata");
        Log.i("XMLParser", "metadata");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("recording-list")) {
                entries.add(readRecordingList(parser));
            } else {
                skip(parser);
            }
        }  
        return entries;
    }
    
    private Recording readRecordingList(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "recording-list");
        Recording recording= new Recording(null, null, null, null);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("recording")) {
                recording = readRecording(parser);
            } else {
                skip(parser);
            }
        }
        return recording;
    }
    
    private Recording readRecording(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "recording");
        Recording recording=new Recording(null,null,null,null);
        String albumTitle=null;
        String artistName=null;
        ReleaseInfo releaseInfo;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
           // Log.i("XMLParserNodeName", name);
            if(name.equals("release-list")){ 
            	releaseInfo=readReleaseInfo(parser);
            	Log.i("XMLParserReleaseID", releaseInfo.releaseID);
            	Log.i("XMLParserReleaseTitle", releaseInfo.releaseTitle);
            }else if(name.equals("artist-credit")){ 
            	artistName=readArtistName(parser);
            	Log.i("XMLParserArtistName", artistName);
            }else {
                skip(parser);
            }
        }
        return recording;
    }
    
    private ReleaseInfo readReleaseInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
    	ReleaseInfo releaseInfo=new ReleaseInfo(null,null);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("release")){ 
            	String releaseID=parser.getAttributeValue(null, "id");
            	parser.nextTag();
            	String releaseTitle=readTitle(parser);
            	releaseInfo.releaseID=releaseID;
            	releaseInfo.releaseTitle=releaseTitle;	
            }
            else{
            	skip(parser);
            }
        }
        return releaseInfo;
    }
    
    private String readArtistName(XmlPullParser parser) throws XmlPullParserException, IOException {
    	
    	String artistName=null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.i("XMLParserNodeName", name);
            if(name.equals("name")){ 
            	artistName=readName(parser);
            }else{
            	parser.next();
            }
        }
        return artistName;
    }
    
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }
    
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return title;
    }
    
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
