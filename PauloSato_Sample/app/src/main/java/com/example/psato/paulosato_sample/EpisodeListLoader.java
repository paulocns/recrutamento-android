package com.example.psato.paulosato_sample;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by psato on 10/4/15.
 */
public class EpisodeListLoader extends AsyncTaskLoader<List<String>> {

    static volatile private List<String> mEpisodes = null;

    public EpisodeListLoader(Context context) {
        super(context);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override public List<String> loadInBackground() {

        InputStream in =  null;
        List<String> result = null;
        try {
            URL url = new URL(Constants.URL_GET_EPISODE_LIST);
            URLConnection urlConnection = url.openConnection();
            urlConnection.addRequestProperty("Content-Type","application/json");
            urlConnection.addRequestProperty("trakt-api-version","2");
            urlConnection.addRequestProperty("trakt-api-key", Constants.CLIENT_ID);
            HttpURLConnection httpconnection = (HttpURLConnection)urlConnection;
            if(httpconnection.getResponseCode() == 200) {
                in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private List<String> readStream(InputStream in) {
        StringBuilder stringBuilder =  new StringBuilder();
        InputStreamReader isw = new InputStreamReader(in);
        int data = 0;
        try {
            data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                stringBuilder.append(current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = stringBuilder.toString();
        ArrayList<String> episodes = null;
        try {
            if(!result.isEmpty()) {
                JSONArray episodelist = new JSONArray(result);
                int length = episodelist.length();
                episodes = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject episode = episodelist.getJSONObject(i);
                    episodes.add(episode.getString("title"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            episodes = null;
        }
        return episodes;
    }


    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(List<String> episodes) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (episodes != null) {
                onReleaseResources(episodes);
            }
        }
        List<String> oldEpisodes = mEpisodes;
        mEpisodes = episodes;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(episodes);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldEpisodes != null) {
            onReleaseResources(oldEpisodes);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mEpisodes != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mEpisodes);
        }
        else{
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(List<String> episodes) {
        super.onCanceled(episodes);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(episodes);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mEpisodes != null) {
            onReleaseResources(mEpisodes);
            mEpisodes = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<String> episodes) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}