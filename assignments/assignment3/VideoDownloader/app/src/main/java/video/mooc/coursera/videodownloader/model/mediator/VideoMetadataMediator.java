package video.mooc.coursera.videodownloader.model.mediator;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy;
import video.mooc.coursera.videodownloader.api.webdata.Video;

import static video.mooc.coursera.videodownloader.utils.Constants.SERVER_URL;

/**
 * Mediates communication between the Video Service and the app
 * The methods in this class block, so
 * they should be called from a background thread (e.g., via an
 * AsyncTask).
 */
public class VideoMetadataMediator {

    /**
     * Status code to indicate that video is successfully
     * rated.
     */
    public static final String STATUS_RATING_SUCCESSFUL = "Thanks for rating ;)";

    /**
     * Status code to indicate that video rating failed.
     */
    public static final String STATUS_RATING_ERROR = "Ups, couldn't rated this ;(";

    /**
     * Defines methods that communicate with the Video Service.
     */
    private VideoServiceProxy mVideoServiceProxy;

    /**
     * Constructor that initializes the VideoDataMediator.
     */
    public VideoMetadataMediator() {
        // Initialize the VideoServiceProxy.
        mVideoServiceProxy = new RestAdapter
                .Builder()
                .setEndpoint(SERVER_URL)
                .build()
                .create(VideoServiceProxy.class);
    }

    public Video rateVideo(Context context, long id, float rating) {

        Video video = mVideoServiceProxy.getVideo(id);
        if (video != null) {
            video.addRating(rating);

            Video updateVideo = mVideoServiceProxy.updateVideo(id, video);

            if (updateVideo != null) {
                // Video successfully uploaded.
                return video;
            }
        }

        // Error occured while raing the video.
        return null;
    }

    /**
     * Get the List of Videos from Video Service.
     *
     * @return the List of Videos from Server or null if there is
     *         failure in getting the Videos.
     */
    public List<Video> getVideoList() {
        return (ArrayList<Video>) mVideoServiceProxy.getVideos();
    }

    public Video getVideoMetadata(long id) {
        return mVideoServiceProxy.getVideo(id);
    }
}
