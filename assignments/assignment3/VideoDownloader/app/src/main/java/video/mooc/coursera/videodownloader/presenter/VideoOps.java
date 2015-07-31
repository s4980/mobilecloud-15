package video.mooc.coursera.videodownloader.presenter;

import android.net.Uri;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import video.mooc.coursera.videodownloader.api.webdata.Video;
import video.mooc.coursera.videodownloader.common.ConfigurableOps;
import video.mooc.coursera.videodownloader.common.ContextView;
import video.mooc.coursera.videodownloader.common.GenericAsyncTask;
import video.mooc.coursera.videodownloader.common.GenericAsyncTaskOps;
import video.mooc.coursera.videodownloader.common.Utils;
import video.mooc.coursera.videodownloader.model.mediator.VideoDataMediator;
import video.mooc.coursera.videodownloader.model.services.DownloadVideoService;
import video.mooc.coursera.videodownloader.model.services.RateVideoService;
import video.mooc.coursera.videodownloader.model.services.UploadVideoService;
import video.mooc.coursera.videodownloader.view.ui.VideoAdapter;

/**
 * Provides all the Video-related operations.  It implements
 * ConfigurableOps so it can be created/managed by the GenericActivity
 * framework.  It extends GenericAsyncTaskOps so its doInBackground()
 * method runs in a background task.  It plays the role of the
 * "Abstraction" in Bridge pattern and the role of the "Presenter" in
 * the Model-View-Presenter pattern.
 */
public class VideoOps
       implements GenericAsyncTaskOps<Void, Void, List<Video>>,
                  ConfigurableOps<VideoOps.View> {
    /**
     * Debugging tag used by the Android logger.
     */
    private static final String TAG =
        VideoOps.class.getSimpleName();
    
    /**
     * This interface defines the minimum interface needed by the
     * VideoOps class in the "Presenter" layer to interact with the
     * VideoListActivity in the "View" layer.
     */
    public interface View extends ContextView {
        /**
         * Finishes the Activity the VideoOps is
         * associated with.
         */
        void finish();

        /**
         * Sets the Adapter that contains List of Videos.
         */
        void setAdapter(VideoAdapter videoAdapter);

        void setListener(VideoAdapter videoAdapter);
    }
        
    /**
     * Used to enable garbage collection.
     */
    private WeakReference<VideoOps.View> mVideoView;
    
    /**
     * The GenericAsyncTask used to expand an Video in a background
     * thread via the Video web service.
     */
    private GenericAsyncTask<Void,
                             Void,
                             List<Video>,
                             VideoOps> mAsyncTask;
    
    /**
     * VideoDataMediator mediates the communication between Video
     * Service and local storage on the Android device.
     */
    VideoDataMediator mVideoMediator;
    
    /**
     * The Adapter that is needed by ListView to show the list of
     * Videos.
     */
    private VideoAdapter mAdapter;
    
    /**
     * Default constructor that's needed by the GenericActivity
     * framework.
     */
    public VideoOps() {
    }
    
    /**
     * Called after a runtime configuration change occurs to finish
     * the initialisation steps.
     */
    public void onConfiguration(VideoOps.View view,
                                boolean firstTimeIn) {
        final String time =
            firstTimeIn 
            ? "first time" 
            : "second+ time";
        
        Log.d(TAG,
              "onConfiguration() called the "
              + time
              + " with view = "
              + view);

        // (Re)set the mVideoView WeakReference.
        mVideoView =
            new WeakReference<>(view);
        
        if (firstTimeIn) {
            // Create VideoDataMediator that will mediate the
            // communication between Server and Android Storage.
            mVideoMediator =
                new VideoDataMediator();
            
            // Create a local instance of our custom Adapter for our
            // ListView.
            mAdapter = 
                 new VideoAdapter(mVideoView.get().getApplicationContext());

            // Get the VideoList from Server. 
            getVideoList();
        }
        
        // Set the adapter to the ListView.
        mVideoView.get().setAdapter(mAdapter);
        mVideoView.get().setListener(mAdapter);
    }

    /**
     * Start a service that Uploads the Video having given Id.
     *   
     * @param videoUri
     */
    public void uploadVideo(Uri videoUri){
        // Sends an Intent command to the UploadVideoService.
        mVideoView.get().getApplicationContext().startService
                (UploadVideoService.makeIntent
                        (mVideoView.get().getApplicationContext(),
                                videoUri));
    }

    public void downloadVideo(long id){
        // Sends an Intent command to the UploadVideoService.
        mVideoView.get().getApplicationContext().startService
                (DownloadVideoService.makeIntent
                        (mVideoView.get().getApplicationContext(), id));
    }

    /**
     * Gets the VideoList from Server by executing the AsyncTask to
     * expand the acronym without blocking the caller.
     */
    public void getVideoList(){
        mAsyncTask = new GenericAsyncTask<>(this);
        mAsyncTask.execute();
    }
    
    /**
     * Retrieve the List of Videos by help of VideoDataMediator via a
     * synchronous two-way method call, which runs in a background
     * thread to avoid blocking the UI thread.
     */
    @Override
    public List<Video> doInBackground(Void... params) {
        return mVideoMediator.getVideoList();
    }

    /**
     * Display the results in the UI Thread.
     */
    @Override
    public void onPostExecute(List<Video> videos) {
        displayVideoList(videos);
    }

    /**
     * Display the Videos in ListView.
     * 
     * @param videos
     */
    public void displayVideoList(List<Video> videos) {
        if (videos != null) {
            // Update the adapter with the List of Videos.
            mAdapter.setVideos(videos);

            Utils.showToast(mVideoView.get().getActivityContext(),
                            "Videos available from the Video Service");
        } else {
            Utils.showToast(mVideoView.get().getActivityContext(),
                           "Please connect to the Video Service");

            // Close down the Activity.
            mVideoView.get().finish();
        }
    }
}
