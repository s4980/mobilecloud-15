package video.mooc.coursera.videodownloader.service;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import video.mooc.coursera.videodownloader.api.webdata.Video;
import video.mooc.coursera.videodownloader.api.webdata.VideoStatus;
import video.mooc.coursera.videodownloader.service.filemanager.VideoFileManager;
import video.mooc.coursera.videodownloader.service.repository.VideoRepository;

@Service
public class VideoServiceImpl implements VideoService {

    public static final int _404 = HttpStatus.NOT_FOUND.value();

    // The manager of video data
    @Autowired
    private VideoRepository metadataRepository;

    @Autowired
    private VideoFileManager videoRepository;

    @Override
    public Collection<Video> getVideosMetadata() {
        return Lists.newArrayList(metadataRepository.findAll());
    }

    @Override
    public Video addVideoMetadata(Video video) {

        return metadataRepository.save(video);
    }

    @Override
    public Video getVideoMetadata(long id) {

        return metadataRepository.findOne(id);
    }

    @Override
    public Video updateVideoMetadata(long id, Video video) {

        return metadataRepository.update(id, video);
    }

    @Override
    public boolean deleteVideo(long id) throws IOException {

        Video video = metadataRepository.findOne(id);

        if (video != null) {
            videoRepository.deleteVideoData(video);
            metadataRepository.remove(id);

            return true;
        }

        return false;
    }

    @Override
    public void deleteVideos() throws IOException {
        for(Video video : metadataRepository.findAll()){
            videoRepository.deleteVideoData(video);
        }

        metadataRepository.clearAll();
    }

    @Override
    public VideoStatus addVideo(Long id, InputStream inputStream) throws Exception {

        VideoStatus videoStatus;
        Video video = metadataRepository.findOne(id);

        if (video != null) {
            videoRepository.saveVideoData(video, inputStream);
            videoStatus = new VideoStatus(VideoStatus.VideoState.READY);
        } else {
            videoStatus = new VideoStatus(VideoStatus.VideoState.ERROR);
        }
        return videoStatus;
    }

    @Override
    public void getVideo(Long id, HttpServletResponse response) throws IOException {

        Video video = metadataRepository.findOne(id);
        if (video == null) {
            response.sendError(_404, "Video not found");
        }

        response.setContentType(video.getContentType());

        if (videoRepository.hasVideoData(video)) {
            videoRepository.copyVideoData(video, response.getOutputStream());
        }
    }

}
