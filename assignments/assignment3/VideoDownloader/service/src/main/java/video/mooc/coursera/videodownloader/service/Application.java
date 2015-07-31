package video.mooc.coursera.videodownloader.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import javax.servlet.MultipartConfigElement;

import video.mooc.coursera.videodownloader.service.filemanager.VideoFileManager;
import video.mooc.coursera.videodownloader.service.repository.InMemoryVideoRepository;
import video.mooc.coursera.videodownloader.service.repository.VideoRepository;

import static video.mooc.coursera.videodownloader.api.constants.Constants.MAX_SIZE_MEGA_BYTE;

// This annotation tells Spring to auto-wire your application
@EnableAutoConfiguration
// This annotation tells Spring to look for controllers, etc.
// starting in the current package
@ComponentScan
// This annotation tells Spring that this class contains configuration
// information
// for the application.
@Configuration
public class Application {

    private static final String MAX_REQUEST_SIZE = MAX_SIZE_MEGA_BYTE + "MB";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        final MultipartConfigFactory factory = new MultipartConfigFactory();

        factory.setMaxFileSize(MAX_REQUEST_SIZE);
        factory.setMaxRequestSize(MAX_REQUEST_SIZE);

        return factory.createMultipartConfig();
    }

    @Bean
    public VideoFileManager videoFileManager() throws IOException {
        return VideoFileManager.get();
    }

    @Bean
    public VideoRepository videoRepository() {
        return new InMemoryVideoRepository();
    }
}
