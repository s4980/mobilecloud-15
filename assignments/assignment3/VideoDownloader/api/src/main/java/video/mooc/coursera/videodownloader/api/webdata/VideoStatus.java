package video.mooc.coursera.videodownloader.api.webdata;

public class VideoStatus {

    public enum VideoState {
        READY,
        PROCESSING,
        ERROR;
    }

    private VideoState state;

    public VideoStatus(VideoState state) {
        super();
        this.state = state;
    }

    public VideoState getState() {
        return state;
    }

    public void setState(VideoState state) {
        this.state = state;
    }
}
