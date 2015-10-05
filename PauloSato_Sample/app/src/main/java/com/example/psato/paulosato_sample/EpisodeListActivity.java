package com.example.psato.paulosato_sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.lang.ref.WeakReference;
import java.util.List;


public class EpisodeListActivity extends AppCompatActivity {

    private final static double TOOLBAR_STARTING_ALPHA = 0x6c;
    private final static double TOOLBAR_TRASHHOLD_FACTOR = 2.44;
    private LinearLayout mListLayout = null;
    private AppBarLayout mAppBarLayout;
    private AppBarLayout.OnOffsetChangedListener mListener;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private Toolbar mToolbar;
    private TextView mSeasonTitle;
    private TextView mSeasonRating;
    private ProgressDialog mProgress;
    private EpisodeLoaderManager mEpisodeLoaderManager;
    private RatingLoaderManager mRatingLoaderManager;
    private ShowCoverLoaderManager mShowCoverLoaderManager;
    private SeasonCoverLoaderManager mSeasonCoverLoaderManager;
    private ImageView mShowCoverImage;
    private ImageView mSeasonCoverImage;
    private boolean isLoadingEpisodes = false;
    private boolean isLoadingRating = false;
    private boolean isLoadingShowCover = false;
    private boolean isLoadingSeasonCover = false;
    private boolean isEpisodesLoaded = false;
    private boolean isRatingLoaded = false;
    private boolean isShowCoverLoaded = false;
    private boolean isSeasonCoverLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_episode_list);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mListLayout = (LinearLayout) findViewById(R.id.episode_list);
        mSeasonTitle = (TextView) findViewById(R.id.season_title_toolbar);
        mSeasonTitle.setText("Season " + Constants.SEASONS);
        mSeasonRating = (TextView) findViewById(R.id.season_rating_text);
        mShowCoverImage = (ImageView) findViewById(R.id.show_cover_image);
        mSeasonCoverImage = (ImageView) findViewById(R.id.serie_cover_image);
        setSupportActionBar(mToolbar);
        mEpisodeLoaderManager = new EpisodeLoaderManager(this);
        mRatingLoaderManager = new RatingLoaderManager(this);
        mShowCoverLoaderManager = new ShowCoverLoaderManager(this);
        mSeasonCoverLoaderManager = new SeasonCoverLoaderManager(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgress = new ProgressDialog(this);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setTitle("Loading");
        mProgress.setMessage("Wait while loading season information");
        mProgress.setOnCancelListener(new LoadingCancelListener(this));
        mListener = new ToolbarColorOffsetChangeListener(this);
        mAppBarLayout.addOnOffsetChangedListener(mListener);
        loadData();
    }

    private static class LoadingCancelListener implements DialogInterface.OnCancelListener{
        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;
        public LoadingCancelListener(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                reference.onBackPressed();
            }
        }
    }

    private static class ToolbarColorOffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {

        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;

        public ToolbarColorOffsetChangeListener(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                double originalAlpha = TOOLBAR_STARTING_ALPHA;
                double minHeight = TOOLBAR_TRASHHOLD_FACTOR * ViewCompat.getMinimumHeight(reference.mCollapsingToolbar);
                if (reference.mCollapsingToolbar.getHeight() + verticalOffset < minHeight) {
                    reference.mToolbar.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    double percentage = reference.mCollapsingToolbar.getHeight() + verticalOffset - minHeight;
                    percentage = percentage / (reference.mCollapsingToolbar.getHeight() - minHeight);
                    Double alpha = originalAlpha * percentage;
                    reference.mToolbar.setBackgroundColor(Color.argb(alpha.intValue(), 0, 0, 0));
                }
            }
        }
    }

    private void loadData() {
        isLoadingEpisodes = true;
        isLoadingRating = true;
        isLoadingShowCover = true;
        isLoadingSeasonCover = true;
        isEpisodesLoaded = false;
        isRatingLoaded = false;
        isShowCoverLoaded = false;
        isSeasonCoverLoaded = false;
        mProgress.show();
        getSupportLoaderManager().initLoader(0, null, mEpisodeLoaderManager);
        getSupportLoaderManager().initLoader(1, null, mRatingLoaderManager);
        getSupportLoaderManager().initLoader(2, null, mShowCoverLoaderManager);
        getSupportLoaderManager().initLoader(3, null, mSeasonCoverLoaderManager);
    }

    private void resetLoaders() {
        getSupportLoaderManager().restartLoader(0, null, mEpisodeLoaderManager);
        getSupportLoaderManager().restartLoader(1, null, mRatingLoaderManager);
        getSupportLoaderManager().restartLoader(2, null, mShowCoverLoaderManager);
        getSupportLoaderManager().restartLoader(3, null, mSeasonCoverLoaderManager);
    }

    private void finishedLoadingAllData() {
        mProgress.dismiss();
        if (!isEpisodesLoaded && !isRatingLoaded
                && !isRatingLoaded && !isSeasonCoverLoaded) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Error");

            builder.setMessage("Problem when downloading data.");
            builder.setPositiveButton("Retry",new ErrorDialogRetryListener(this));
            builder.setNegativeButton("Cancel", null);
            builder.show();

        }
    }

    private static class ErrorDialogRetryListener implements DialogInterface.OnClickListener{
        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;

        public ErrorDialogRetryListener(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                reference.resetLoaders();
                reference.loadData();
            }
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateScrollview(List<String> episodes) {
        mListLayout.removeAllViews();
        for (int i = 0; i < episodes.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View listItem = inflater.inflate(R.layout.list_item_layout, null);
            TextView title = (TextView) listItem.findViewById(R.id.episode_title_text);
            TextView number = (TextView) listItem.findViewById(R.id.episode_number_text);
            title.setText(episodes.get(i));
            number.setText("E" + (i + 1));
            mListLayout.addView(listItem);
        }
    }

    @Override
    protected void onDestroy() {
        mAppBarLayout.removeOnOffsetChangedListener(mListener);
        mListener = null;
        mProgress.dismiss();
        mListLayout.removeAllViews();
        super.onDestroy();
    }


    private static class EpisodeLoaderManager implements LoaderManager.LoaderCallbacks<List<String>> {

        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;

        public EpisodeLoaderManager(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public Loader<List<String>> onCreateLoader(int id, Bundle args) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                return new EpisodeListLoader(reference);
            } else {
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                if (data != null) {
                    reference.populateScrollview(data);
                    reference.isEpisodesLoaded = true;
                }
                reference.isLoadingEpisodes = false;
                if (!reference.isLoadingEpisodes && !reference.isLoadingRating &&
                        !reference.isLoadingShowCover && !reference.isLoadingSeasonCover) {
                    reference.finishedLoadingAllData();
                }
            }
            // To dismiss the dialog
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {
        }
    }

    private static class RatingLoaderManager implements LoaderManager.LoaderCallbacks<String> {

        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;

        public RatingLoaderManager(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                return new SeasonRatingLoader(reference);
            } else {
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                if (data != null) {
                    reference.mSeasonRating.setText(data);
                    reference.isRatingLoaded = true;
                }
                reference.isLoadingRating = false;
                if (!reference.isLoadingEpisodes && !reference.isLoadingRating &&
                        !reference.isLoadingShowCover && !reference.isLoadingSeasonCover) {
                    reference.finishedLoadingAllData();
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
        }
    }

    private static class ShowCoverLoaderManager implements LoaderManager.LoaderCallbacks<Bitmap> {

        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;

        public ShowCoverLoaderManager(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                return new ShowCoverLoader(reference);
            } else {
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                if (data != null) {
                    reference.mShowCoverImage.setImageBitmap(data);
                    reference.isShowCoverLoaded = true;
                }
                reference.isLoadingShowCover = false;
                if (!reference.isLoadingEpisodes && !reference.isLoadingRating &&
                        !reference.isLoadingShowCover && !reference.isLoadingSeasonCover) {
                    reference.finishedLoadingAllData();
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {
        }
    }

    private static class SeasonCoverLoaderManager implements LoaderManager.LoaderCallbacks<Bitmap> {

        private WeakReference<EpisodeListActivity> mEpisodeListActivityReference;

        public SeasonCoverLoaderManager(EpisodeListActivity activity) {
            mEpisodeListActivityReference = new WeakReference<EpisodeListActivity>(activity);
        }

        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                return new SeasonCoverLoader(reference);
            } else {
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
            EpisodeListActivity reference = mEpisodeListActivityReference.get();
            if (reference != null) {
                if (data != null) {
                    reference.mSeasonCoverImage.setImageBitmap(data);
                    reference.isSeasonCoverLoaded = true;
                }
                reference.isLoadingSeasonCover = false;
                if (!reference.isLoadingEpisodes && !reference.isLoadingRating &&
                        !reference.isLoadingShowCover && !reference.isLoadingSeasonCover) {
                    reference.finishedLoadingAllData();
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {
        }
    }

}
