package video.mooc.coursera.videodownloader.service.repository;

import video.mooc.coursera.videodownloader.api.webdata.Video;

public interface VideoRepository {

    /**
     * Add video metadata to repository
     *
     * @param video
     * @return
     */
    Video save(Video video);

    /**
     * Updates video metadata in repository
     *
     * @param video
     * @return
     */
    Video update(long id, Video video);

    /**
     * Returns video metadata from repository
     *
     * @param id
     * @return
     */
    Video findOne(long id);

    /**
     * Returns all video's metadata from repository
     *
     * @return
     */
    Iterable<Video> findAll();

    /**
     * Removes video's metadata from repository
     *
     * @param id
     * @return
     */
    boolean remove(long id);

    /**
     * Clears all metadata repository
     *
     * @return
     */
    void clearAll();
}
