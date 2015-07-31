package video.mooc.coursera.videodownloader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import video.mooc.coursera.videodownloader.R;
import video.mooc.coursera.videodownloader.common.GenericActivity;
import video.mooc.coursera.videodownloader.common.Utils;
import video.mooc.coursera.videodownloader.model.services.RateVideoService;
import video.mooc.coursera.videodownloader.presenter.VideoOps;
import video.mooc.coursera.videodownloader.utils.VideoStorageUtils;
import video.mooc.coursera.videodownloader.view.ui.FloatingActionButton;
import video.mooc.coursera.videodownloader.view.ui.UploadVideoDialogFragment;

import static video.mooc.coursera.videodownloader.model.services.RateVideoService.ACTION_RATE_VIDEO_SERVICE_RESPONSE;

public class VideoDetailActivity extends GenericActivity<VideoOps.View, VideoOps> {

    public static final String OVERAL_RATING_FORMAT = "%.1f/%.0f";
    
    /**
     * The Broadcast Receiver that registers itself to receive the
     * result from UploadVideoService when a video upload completes.
     */
    private UploadResultReceiver mUploadResultReceiver;
    
    /**
     * The Floating Action Button that will show a Dialog Fragment to
     * upload Video when user clicks on it.
     */
    private FloatingActionButton mPlayVideoButton;

    /**
     * The Floating Action Button that will show a Dialog Fragment to
     * upload Video when user clicks on it.
     */
    private FloatingActionButton mDownloadVideoButton;

    /**
     * Referance to ratingBar
     */
    private RatingBar mRatingBar;

    /**
     * Referance to rating details text field
     */
    private TextView mVideoRatingDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receiver for the notification.
        mUploadResultReceiver = new UploadResultReceiver();

        setContentView(R.layout.activity_video_detail);

        // Recive intent from either saved activity state or from other activity
        final Intent intent;
        if (savedInstanceState == null) {
            intent = getIntent();
        } else {
            intent = savedInstanceState.getParcelable("Saved_intent");
        }

        if (intent != null) {
            // Set video title from Intent 
            TextView videoTitle = (TextView) findViewById(R.id.videoTitle);
            videoTitle.setText(intent.getStringExtra("videoTitle"));

            // Set rating bar details from Intent 
            mVideoRatingDetails = (TextView) findViewById(R.id.ratingDetails);
            mVideoRatingDetails.setText(String.format(OVERAL_RATING_FORMAT,
                    intent.getFloatExtra("videoAvgRating", 0),
                    intent.getFloatExtra("videoTotalRatings", 0)));

            // Set rating bar value from Intent 
            mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
            mRatingBar.setRating(intent.getFloatExtra("videoAvgRating", 0));

            // Add listener to rating bar to react on new ratings from user and to be able to display average video rating
            mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                public void onRatingChanged(RatingBar ratingBar,
                                            float rating,
                                            boolean fromUser) {

                    // Check if ratingBar change is comming from user input
                    if (fromUser) {
                        // call rest api to add new rating for video
                        getApplicationContext().startService(RateVideoService.makeIntent(
                                                                getApplicationContext(),
                                                                intent.getLongExtra("videoId", 0),
                                                                rating,
                                                                "Rate"));
                    } else {
                        // If rating bar was changed from inside of the code to display average ratings display Toast
                        Utils.showToast(getApplicationContext(), "Video was rated");
                    }
                }
            });

            // Get reference to ImageView
            ImageView thumbnail = (ImageView) findViewById(R.id.videoThumbnail);
            Uri videoUri = VideoStorageUtils.getRecordedVideoUri(intent.getStringExtra("videoTitle"));
            if (videoUri != null) {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                thumbnail.setImageBitmap(bitmap);
            }

            // Get reference to the Floating Play Action Button.
            mPlayVideoButton = (FloatingActionButton) findViewById(R.id.playVideoButton);

            if (mPlayVideoButton != null) {
                // Show the UploadVideoDialog Fragment when user clicks the
                // button.
                mPlayVideoButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent in = new Intent(getBaseContext(), VideoViewActivity.class);
                        in.putExtra("videoDataUrl", intent.getStringExtra("videoDataUrl"));
                        in.putExtra("videoDuration", intent.getLongExtra("videoDuration", 0));
                        in.putExtra("videoTitle", intent.getStringExtra("videoTitle"));
                        in.putExtra("videoUri", VideoStorageUtils.getRecordedVideoUri(intent.getStringExtra("videoTitle")));

                        // Start VideoViewActivity to play video
                        startActivityForResult(in, 1);
                    }
                });
            }

            // Get reference to the Floating Download Action Button.
            mDownloadVideoButton = (FloatingActionButton) findViewById(R.id.playDownloadButton);

            if (mDownloadVideoButton != null) {
                mDownloadVideoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getOps().downloadVideo(intent.getLongExtra("videoId", 0));
                    }
                });
            }

            // Set visiblility for buttons
            if (videoUri == null) {
                // Video is not downloaded
                mPlayVideoButton.setVisibility(View.GONE);
                mDownloadVideoButton.setVisibility(View.VISIBLE);
            } else {
                mPlayVideoButton.setVisibility(View.VISIBLE);
                mDownloadVideoButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent in = new Intent();
        setResult(999, in);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver();
    }

    /**
     * Hook method that gives a final chance to release resources and
     * stop spawned threads.  onDestroy() may not always be
     * called-when system kills hosting process.
     */
    @Override
    protected void onPause() {
        // Call onPause() in superclass.
        super.onPause();

        Bundle bundle = new Bundle();
        onSaveInstanceState(bundle);

        // Unregister BroadcastReceiver.
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mUploadResultReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("Saved_intent", getIntent());
    }

    /**
     * Register a BroadcastReceiver that receives a result from the
     * UploadVideoService when a video upload completes.
     */
    private void registerReceiver() {

        // Create an Intent filter that handles Intents from the
        // UploadVideoService.
        IntentFilter uploadIntentFilter = new IntentFilter();
        uploadIntentFilter.addAction(ACTION_RATE_VIDEO_SERVICE_RESPONSE);
        uploadIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Register the BroadcastReceiver.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mUploadResultReceiver,
                        uploadIntentFilter);
    }

    /**
     * The Broadcast Receiver that registers itself to receive result
     * from UploadVideoService.
     */
    private class UploadResultReceiver
            extends BroadcastReceiver {
        /**
         * Hook method that's dispatched when the UploadService has
         * uploaded the Video.
         */
        @Override
        public void onReceive(Context context,
                              Intent intent) {
            // RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

            // Update ratingBar with average ratings for video
            if (mRatingBar != null) {
                mRatingBar.setRating(intent.getFloatExtra("videoAvgRating", 0));
            }

            // Update videoRatingDetails text filed with average video rating and total number of ratings
            if (mVideoRatingDetails != null) {
                // TextView videoRatingDetails = (TextView) findViewById(R.id.ratingDetails);
                mVideoRatingDetails.setText(String.format(
                        OVERAL_RATING_FORMAT,
                        intent.getFloatExtra("videoAvgRating", 0),
                        intent.getFloatExtra("videoTotalRatings", 0)));
            }
        }
    }
}
