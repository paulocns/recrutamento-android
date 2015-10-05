package com.example.psato.paulosato_sample;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

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
public class SeasonRatingLoader extends AsyncTaskLoader<String> {

    static volatile private String mRating = null;

    public SeasonRatingLoader(Context context) {
        super(context);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public String loadInBackground() {
        InputStream in = null;
        String result = null;
        try {
            URL url = new URL(Constants.URL_GET_SEASON_RATING);
            URLConnection urlConnection = url.openConnection();
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("trakt-api-version", "2");
            urlConnection.addRequestProperty("trakt-api-key", Constants.CLIENT_ID);
            HttpURLConnection httpconnection = (HttpURLConnection) urlConnection;
            if (httpconnection.getResponseCode() == 200) {
                in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private String readStream(InputStream in) {
        StringBuilder stringBuilder = new StringBuilder();
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
        String rating = null;
        try {
            JSONObject ratingObject = new JSONObject(result);
            String longRating = ratingObject.getString("rating");
            float ratingNumber = Float.parseFloat(longRating);
            rating = String.format("%.1f", ratingNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rating;
    }


    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(String rating) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (rating != null) {
                onReleaseResources(rating);
            }
        }
        String oldRating = mRating;
        mRating = rating;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(rating);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldRating != null) {
            onReleaseResources(oldRating);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mRating != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mRating);
        } else {
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(String rating) {
        super.onCanceled(rating);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(rating);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mRating != null) {
            onReleaseResources(mRating);
            mRating = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(String rating) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}