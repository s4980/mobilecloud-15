/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package video.mooc.coursera.videodownloader.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import video.mooc.coursera.videodownloader.api.webdata.Video;
import video.mooc.coursera.videodownloader.api.webdata.VideoStatus;
import video.mooc.coursera.videodownloader.service.VideoService;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy.DATA_PARAMETER;
import static video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy.ID_PARAMETER;
import static video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy.VIDEO_DATA_PATH;
import static video.mooc.coursera.videodownloader.api.proxy.VideoServiceProxy.VIDEO_SVC_PATH;

@Controller
public class VideoController {

    @Autowired
    private VideoService videoService;

    public static final String VIDEO_ID_PATH = VIDEO_SVC_PATH + "/{id}";

    /**
     * This method returns a collection of all video
     * meta data stored by the service.
     *
     * @return
     */
    @RequestMapping(value = VIDEO_SVC_PATH,
                    method = GET)
    public
    @ResponseBody
    Collection<Video> getVideosMetadata() {
        return videoService.getVideosMetadata();
    }

    /**
     * This method grabs the meta data for a new Video from the body, storing it in memory.
     * It returns a unique ID to the client for use when uploading the actual video.
     *
     * @param video
     * @return
     */
    @RequestMapping(value = VIDEO_SVC_PATH,
                    method = POST)
    public
    @ResponseBody
    Video addVideoMetadata(@RequestBody Video video,
                           HttpServletResponse response) {
        Video v = videoService.addVideoMetadata(video);
        if (v == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return v;
    }

    /**
     * This method grabs the meta data for the video with the id for
     * the @param v, updating its representation in memory if found.
     * It returns the updated video.
     *
     * @param video
     * @return
     */
    @RequestMapping(value = VIDEO_ID_PATH,
                    method = PUT)
    public
    @ResponseBody
    Video updateVideoMetadata(@PathVariable(ID_PARAMETER) long id,
                              @RequestBody Video video,
                              HttpServletResponse response) {
        Video v = null;
        if (id == video.getId()) {
            v = videoService.updateVideoMetadata(id, video);
        }

        if (v == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return v;
    }

    /**
     * Returns the video meta data specified by the @param id if found.
     *
     * @param id
     * @return
     */
    @RequestMapping(value = VIDEO_ID_PATH,
                    method = GET)
    public
    @ResponseBody
    Video getVideoMetadata(@PathVariable(ID_PARAMETER) long id,
                           HttpServletResponse response) {
        return videoService.getVideoMetadata(id);
    }


    /**
     * This method grabs the encoded video from the multi part body, writing it to disk.
     * It returns the VideoStatus to indicate success or 400 for failure.
     *
     * @param id
     * @return
     */
    @RequestMapping(value = VIDEO_DATA_PATH,
                    method = POST)
    public
    @ResponseBody
    VideoStatus addVideo(@PathVariable(ID_PARAMETER) long id,
                         @RequestParam(DATA_PARAMETER) MultipartFile videoFile,
                         HttpServletResponse response) throws Exception {

        final VideoStatus videoStatus = videoService.addVideo(id, videoFile.getInputStream());

        if (videoStatus.getState() == VideoStatus.VideoState.ERROR) {
            response.sendError(404, "Video not found");
        }

        return videoStatus;
    }

    @RequestMapping(method = GET,
                    value = VIDEO_DATA_PATH)
    public void getVideo(@PathVariable(ID_PARAMETER) Long id,
                         HttpServletResponse response) throws Exception {
        videoService.getVideo(id, response);
    }

    /**
     * This method deletes the video data and the video meta data
     * for the given @param id if found.
     *
     * @param id
     */
    @RequestMapping(value = VIDEO_ID_PATH,
                    method = DELETE)
    public void deleteVideo(@PathVariable(ID_PARAMETER) long id,
                            HttpServletResponse response) throws IOException {
        videoService.deleteVideo(id);
    }

    /**
     * This method deletes all the video data and the video meta data
     * stored by the service.
     */
    @RequestMapping(value=VIDEO_SVC_PATH,
                    method=DELETE)
    public void deleteVideos() throws IOException {
        videoService.deleteVideos();
    }

}
