package video.mooc.coursera.videodownloader.service.repository;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import video.mooc.coursera.videodownloader.api.webdata.Video;

import static video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy.DATA_PARAMETER;
import static video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy.VIDEO_SVC_PATH;

public class InMemoryVideoRepository implements VideoRepository {

    private AtomicLong currentId = new AtomicLong(0L);
    private ConcurrentMap<Long, Video> videos = new ConcurrentHashMap<>();

    @Override
    public Video save(Video video) {
        Long id = initializeVideo(video);
        if (id == null) {
            return null;
        }

        videos.put(id, video);

        return video;
    }

    @Override
    public Video update(long id, Video video) {

        videos.replace(id, video);

        return videos.get(id);
    }

    @Override
    public Video findOne(long id) {
        return videos.get(id);
    }

    @Override
    public Iterable<Video> findAll() {
        return videos.values();
    }

    @Override
    public boolean remove(long id) {
        final Video remove = videos.remove(id);

        return remove != null;
    }

    @Override
    public void clearAll() {
        videos.clear();
    }

    /*
     * This method assigns a new ID and a url to a new video.
     * It returns the ID of the new video or Null if the
     * video was already in the collection.
     */
    private Long initializeVideo(Video v) {
        if (!videos.containsKey(v.getId())) {
            v.setId(checkAndSetId(v.getId()));
            v.setDataUrl(getDataUrl(v.getId()));
        } else {
            return null;
        }

        return v.getId();
    }

    private long checkAndSetId(long id) {
        if (id == 0) {
            return currentId.incrementAndGet();
        } else {
            return id;
        }
    }

    private String getDataUrl(Long id) {
        return String.format("%s%s/%d/%s", getUrlBaseForLocalServer(), VIDEO_SVC_PATH, id, DATA_PARAMETER);
    }

    /*
     * This method returns the url authority for the current request
     * prepended by the http scheme.
     */
    private String getUrlBaseForLocalServer() {
        HttpServletRequest request = getRequest();
        String base = String.format("http://%s%s", request.getServerName(), (request.getServerPort() != 80) ? ":" + request.getServerPort() : "");

        return base;
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }
}
