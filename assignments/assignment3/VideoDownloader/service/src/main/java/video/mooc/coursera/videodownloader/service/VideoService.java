package video.mooc.coursera.videodownloader.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import video.mooc.coursera.videodownloader.api.webdata.Video;
import video.mooc.coursera.videodownloader.api.webdata.VideoStatus;

public interface VideoService {

    VideoStatus addVideo(Long id, InputStream inputStream) throws Exception;

    void getVideo(Long id, HttpServletResponse response) throws Exception;

    Video addVideoMetadata(Video video);

    Video getVideoMetadata(long id);

    Video updateVideoMetadata(Video video);

    Collection<Video> getVideosMetadata();

    boolean deleteVideo(long id) throws IOException;

    void deleteVideos() throws IOException;

}
