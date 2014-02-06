package com.example.httprequesttest;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.R.string;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landscape_layout_handset);
		
		AsyncGetReleaseGroupIDFromMusicBrainzTask asyncGetReleaseGroupIDFromMusicBrainzTask=new AsyncGetReleaseGroupIDFromMusicBrainzTask();
		asyncGetReleaseGroupIDFromMusicBrainzTask.execute("That was then, this is now", "Andy Timmons");
		String releaseGroupID="";
		try {
			releaseGroupID=asyncGetReleaseGroupIDFromMusicBrainzTask.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(releaseGroupID!=null)
		{
		
			AsyncGetCoverArtFromCoverArtArchiveTask asyncGetCoverArtFromCoverArtArchiveTask=new AsyncGetCoverArtFromCoverArtArchiveTask();
			asyncGetCoverArtFromCoverArtArchiveTask.execute(releaseGroupID);
		}
		else
		{
			Log.i("Get album cover art", "No release ID");
		}

	}   

	
	
}
