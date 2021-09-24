package com.example.VideoAPI;

import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VideoApiApplication {

    public static void main(String[] args) {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCV.loadShared();
        SpringApplication.run(VideoApiApplication.class, args);
    }

}
