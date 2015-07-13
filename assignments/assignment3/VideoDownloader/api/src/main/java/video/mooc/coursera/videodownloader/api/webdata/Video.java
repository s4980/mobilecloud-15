package video.mooc.coursera.videodownloader.api.webdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Video {

    private long id;
    private long duration;
    private String title;
    private String location;
    private String subject;
    private String contentType;
    private float totalSumOfStars = 0;
    private float totalNumberOfStars = 0;

    @JsonIgnore
    private String dataUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonProperty
    public String getDataUrl() {
        return dataUrl;
    }

    @JsonIgnore
    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public float getTotalSumOfStars() {
        return totalSumOfStars;
    }

    public void setTotalSumOfStars(float totalSumOfStars) {
        this.totalSumOfStars = totalSumOfStars;
    }

    public float getTotalNumberOfStars() {
        return totalNumberOfStars;
    }

    public void setTotalNumberOfStars(float totalNumberOfStars) {
        this.totalNumberOfStars = totalNumberOfStars;
    }

    public void addRating(float numberOfStars) {
        setTotalSumOfStars(getTotalSumOfStars() + numberOfStars);
        setTotalNumberOfStars(getTotalNumberOfStars() + 1);
    }

    public float getAverageRating() {
        return getTotalNumberOfStars() > 0 ? getTotalSumOfStars() / getTotalNumberOfStars() : 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDuration());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Video)
                && Objects.equals(getTitle(), ((Video) obj).getTitle())
                && getDuration() == ((Video) obj).getDuration();
    }
}
