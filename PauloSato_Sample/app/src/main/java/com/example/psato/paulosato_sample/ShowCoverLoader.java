package com.example.psato.paulosato_sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;

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


/**
 * Created by psato on 10/4/15.
 */
public class ShowCoverLoader extends AsyncTaskLoader<Bitmap> {

    static volatile private Bitmap mShowCover = null;

    public ShowCoverLoader(Context context) {
        super(context);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public Bitmap loadInBackground() {
        InputStream inCoverURL = null;
        InputStream inCoverImage = null;
        Bitmap result = null;
        try {
            URL url = new URL(Constants.URL_GET_SHOW_IMAGES);
            URLConnection urlConnection = url.openConnection();
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("trakt-api-version", "2");
            urlConnection.addRequestProperty("trakt-api-key", Constants.CLIENT_ID);
            HttpURLConnection httpconnection = (HttpURLConnection) urlConnection;
            if (httpconnection.getResponseCode() == 200) {
                inCoverURL = new BufferedInputStream(urlConnection.getInputStream());
                String coverURL = readCoverURLStream(inCoverURL);
                url = new URL(coverURL);
                inCoverImage = (InputStream) url.getContent();
                result = readCoverImageStream(inCoverImage);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inCoverURL != null) {
                try {
                    inCoverURL.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inCoverImage != null) {
                try {
                    inCoverImage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private String readCoverURLStream(InputStream in) {
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
        String imageCoverURL = null;
        try {
            JSONObject response = new JSONObject(result);
            JSONObject imageListObject = response.getJSONObject("images");
            JSONObject posterImagesObject = imageListObject.getJSONObject("poster");
            imageCoverURL = posterImagesObject.getString("thumb");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imageCoverURL;
    }

    private Bitmap readCoverImageStream(InputStream in) {
        Bitmap cover = BitmapFactory.decodeStream(in);
        return cover;
    }


    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(Bitmap imageCover) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (imageCover != null) {
                onReleaseResources(imageCover);
            }
        }
        Bitmap oldImagesCover = mShowCover;
        mShowCover = imageCover;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(imageCover);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldImagesCover != null) {
            onReleaseResources(oldImagesCover);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mShowCover != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mShowCover);
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
    public void onCanceled(Bitmap imageCover) {
        super.onCanceled(imageCover);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(imageCover);
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
        if (mShowCover != null) {
            onReleaseResources(mShowCover);
            mShowCover = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(Bitmap imageCover) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}