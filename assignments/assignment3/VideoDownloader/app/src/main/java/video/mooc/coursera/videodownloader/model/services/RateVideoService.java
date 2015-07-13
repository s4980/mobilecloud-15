package video.mooc.coursera.videodownloader.model.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import video.mooc.coursera.videodownloader.model.mediator.VideoDataMediator;
import video.mooc.coursera.videodownloader.model.mediator.VideoMetadataMediator;

public class RateVideoService extends IntentService {

    /**
     * Custom Action that will be used to send Broadcast to the
     * VideoListActivity.
     */
    public static final String ACTION_RATE_VIDEO_SERVICE_RESPONSE =
            "video.mooc.coursera.videodownloader.model.services.RateVideoService.RESPONSE";

    public static final String EXTRA_VIDEO_ID = "Video_id";
    public static final String EXTRA_VIDEO_RATING = "Video_rating";

    /**
     * VideoDataMediator mediates the communication between Video
     * Service and local storage in the Android device.
     */
    private VideoMetadataMediator mVideoMediator;

    /**
     * It is used by Notification Manager to send Notifications.
     */
    private static final int NOTIFICATION_ID = 3;

    /**
     * Constructor for RateVideoService.
     *
     * @param name
     */
    public RateVideoService(String name) {
        super("RateVideoService");
    }

    /**
     * Constructor for RateVideoService.
     */
    public RateVideoService() {
        super("RateVideoService");
    }

    /**
     * Factory method that makes the explicit intent another Activity
     * uses to call this Service.
     *
     * @param context
     * @param id
     * @return
     */
    public static Intent makeIntent(Context context,
                                    long id,
                                    float rating) {
        return new Intent(context,
                          RateVideoService.class)
                            .putExtra(EXTRA_VIDEO_ID, id)
                            .putExtra(EXTRA_VIDEO_RATING, rating);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create VideoDataMediator that will mediate the communication
        // between Server and Android Storage.
        mVideoMediator = new VideoMetadataMediator();
        mVideoMediator.rateVideo(getApplicationContext(), intent.getLongExtra(EXTRA_VIDEO_ID, 0), intent.getFloatExtra(EXTRA_VIDEO_RATING, 0));

        // Send the Broadcast to VideoListActivity that the Video
        // Upload is completed.
        sendBroadcast();
    }

    /**
     * Send the Broadcast to Activity that the Video Upload is
     * completed.
     */
    private void sendBroadcast(){
        // Use a LocalBroadcastManager to restrict the scope of this
        // Intent to the VideoUploadClient application.
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(ACTION_RATE_VIDEO_SERVICE_RESPONSE)
                        .addCategory(Intent.CATEGORY_DEFAULT));
    }
}
