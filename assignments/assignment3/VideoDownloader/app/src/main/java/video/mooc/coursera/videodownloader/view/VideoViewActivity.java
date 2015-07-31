package video.mooc.coursera.videodownloader.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import video.mooc.coursera.videodownloader.R;
import video.mooc.coursera.videodownloader.utils.VideoMediaStoreUtils;
import video.mooc.coursera.videodownloader.utils.VideoStorageUtils;

public class VideoViewActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private static final String POSITION_STATE = "Position";

    private int position = 0;
    private VideoView mVideoView;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        Intent intent = getIntent();

        String videoTitle = "";
        String videoUrl = "";
        Uri videoUri = null;
        Long videoDuration = 0L;
        if (intent != null) {
            videoTitle = intent.getStringExtra("videoTitle");
            videoUrl = intent.getStringExtra("videoDataUrl");
            videoDuration = intent.getLongExtra("videoDuration", 0);
            videoUri = intent.getParcelableExtra("videoUri");
        }

        if (videoUri == null) {
            Toast.makeText(getApplicationContext(), "Video url is empty.", Toast.LENGTH_LONG).show();

            VideoViewActivity.this.finish();
        }

        //initialize the VideoView
        mVideoView = (VideoView) findViewById(R.id.video_view);

        // create a progress bar while the video file is loading
        progressDialog = new ProgressDialog(VideoViewActivity.this);
        // set a title for the progress bar
        progressDialog.setTitle(videoTitle.isEmpty() ? "Video file" 
                                                     : String.format("%s (%.0f sec)", videoTitle, videoDuration/1000.0));
        // set a message for the progress bar
        progressDialog.setMessage("Loading...");
        //set the progress bar not cancelable on users' touch
        progressDialog.setCancelable(false);
        // show the progress bar
        progressDialog.show();

        try {
            if (mediaControls == null) {
                mediaControls = new MediaController(VideoViewActivity.this);
            }
            //set the media controller in the VideoView
            mVideoView.setMediaController(mediaControls);

            //set the uri of the video to be played
            mVideoView.setVideoURI(videoUri);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getApplicationContext(), "Unable to find video.", Toast.LENGTH_LONG).show();

            VideoViewActivity.this.finish();
        }

        mVideoView.requestFocus();
        // we also set an setOnPreparedListener in order to know when the video file is ready for playback
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {
                // close the progress bar and play the video
                progressDialog.dismiss();
                //if we have a position on savedInstanceState, the video playback should start from here
                mVideoView.seekTo(position);
                if (position == 0) {
                    mVideoView.start();
                } else {
                    //if we come from a resumed activity, video playback will be paused
                    mVideoView.pause();
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // we use onSaveInstanceState in order to store the video playback position for orientation change
        savedInstanceState.putInt(POSITION_STATE, mVideoView.getCurrentPosition());
        mVideoView.pause();
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // we use onRestoreInstanceState in order to play the video playback from the stored position
        position = savedInstanceState.getInt(POSITION_STATE);
        mVideoView.seekTo(position);
    }
}
