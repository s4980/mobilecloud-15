package video.mooc.coursera.videodownloader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receiver for the notification.
        mUploadResultReceiver = new UploadResultReceiver();

        setContentView(R.layout.activity_video_detail);

        final Intent intent;
        if (savedInstanceState == null) {
            intent = getIntent();
        } else {
            intent = savedInstanceState.getParcelable("Saved_intent");
        }
//        intent.getLongExtra("video", 0);
//        intent.getStringExtra("videoTitle");
//        intent.getFloatExtra("videoAvgRating", 0);
//        intent.getFloatExtra("videoTotalRatings", 0);
//        intent.getStringExtra("videoDataUrl");
//        intent.getLongExtra("videoDuration", 0);

        if (intent != null) {
            TextView videoTitle = (TextView) findViewById(R.id.videoTitle);
            videoTitle.setText(intent.getStringExtra("videoTitle"));

            TextView videoRatingDetails = (TextView) findViewById(R.id.ratingDetails);
            videoRatingDetails.setText(String.format(OVERAL_RATING_FORMAT, intent.getFloatExtra("videoAvgRating", 0), intent.getFloatExtra("videoTotalRatings", 0)));

            final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            ratingBar.setRating(intent.getFloatExtra("videoAvgRating", 0));
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                public void onRatingChanged(RatingBar ratingBar,
                                            float rating,
                                            boolean fromUser) {

                    // call rest api to add new rating for video
                    // getOps().rateVideo(video.getId(), rating);
                    if (fromUser) {
                        getApplicationContext().startService(RateVideoService.makeIntent(
                                getApplicationContext(), intent.getLongExtra("videoId", 0), rating, "Rate"));
                    } else {
                        Utils.showToast(getApplicationContext(),
                                "Video was rated");
                    }
                }
            });

            // Get reference to the Floating Action Button.
            mPlayVideoButton = (FloatingActionButton) findViewById(R.id.playVideoButton);

            // Show the UploadVideoDialog Fragment when user clicks the
            // button.
            mPlayVideoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent in = new Intent(getBaseContext(), VideoViewActivity.class);
                    in.putExtra("videoDataUrl", intent.getStringExtra("videoDataUrl"));
                    in.putExtra("videoDuration", intent.getLongExtra("videoDuration", 0));
                    in.putExtra("videoTitle", intent.getStringExtra("videoTitle"));
                    in.putExtra("videoUri", VideoStorageUtils.getRecordedVideoUri(intent.getStringExtra("videoTitle")));

                    startActivityForResult(in, 1);
                    // VideoDetailActivity.this.finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // No need to do anything here
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
//            if (ACTION_RATE_VIDEO_SERVICE_RESPONSE.equals(intent.getAction())) {
//                if (intent.getCategories().contains("Rate")) {
////                    getApplicationContext().startService(RateVideoService.makeIntent(
////                            getApplicationContext(), video.getId(), rating));
//                }

//                if (intent.getCategories() == Intent.CATEGORY_DEFAULT) {
                    RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                    if (ratingBar != null) {
//                        intent.getLongExtra("video", 0);
//                        intent.getStringExtra("videoTitle");
//                        intent.getStringExtra("videoDataUrl");
//                        intent.getLongExtra("videoDuration", 0);
                        ratingBar.setRating(intent.getFloatExtra("videoAvgRating", 0));

                        TextView videoRatingDetails = (TextView) findViewById(R.id.ratingDetails);
                        videoRatingDetails.setText(
                                String.format(
                                        OVERAL_RATING_FORMAT, intent.getFloatExtra("videoAvgRating", 0),
                                                 intent.getFloatExtra("videoTotalRatings", 0)));
                    }
//                }
//            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("Saved_intent", getIntent());
    }

}
