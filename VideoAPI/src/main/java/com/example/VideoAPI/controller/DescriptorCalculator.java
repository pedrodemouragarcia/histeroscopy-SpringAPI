package com.example.VideoAPI.controller;

import com.example.VideoAPI.model.Frame;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class DescriptorCalculator {

    public static final String MAIN_FOLDER = "D:/GitHub/histeroscopy-Unity/Video/";
    private Map<String, Integer> matchesMap = new HashMap<>();
    private Map<String, MatOfKeyPoint> descriptorsMap = new HashMap<>();

    public void calculateDescriptor(Frame frame) {
        System.out.println("Started....");
        String image = MAIN_FOLDER.concat(frame.getPath()).concat(".png");
        System.out.println("Loading image...");
        Mat objectImage = Imgcodecs.imread(image, Imgcodecs.IMREAD_COLOR);
        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        System.out.println("Creating SIFT...");
        SIFT featureDetector = SIFT.create();
        System.out.println("Detecting key points...");
        featureDetector.detect(objectImage, objectKeyPoints);
        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        System.out.println("Computing descriptors...");
        featureDetector.compute(objectImage, objectKeyPoints, objectDescriptors);
        System.out.println("KeyPoints founded: " + frame.getKeyPointList().size());
        System.out.println("Descriptors founded: " + frame.getDescriptorsList().size());
        System.out.println("Ended....");
    }


    public void setMatchesBetweenFrames(Frame frame, Frame otherFrame, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        Integer totalmatches = 0;
        String key = frame.getPath().concat("|").concat(otherFrame.getPath());
        String reversekey = otherFrame.getPath().concat("|").concat(frame.getPath());
        MatOfKeyPoint otherFrameDescriptors = null;

        if (descriptorsMap.containsKey(otherFrame.getPath())) {
            otherFrameDescriptors = descriptorsMap.get(otherFrame.getPath());
        }

        if (matchesMap.containsKey(key) || matchesMap.containsKey(reversekey)) {
            totalmatches = matchesMap.get(key);
        } else {
            try {

                if (!Objects.nonNull(otherFrameDescriptors)) {
                    String imageOtherFrame = MAIN_FOLDER.concat(otherFrame.getPath()).concat(".png");
                    Mat objectImageOtherFrame = Imgcodecs.imread(imageOtherFrame, Imgcodecs.IMREAD_COLOR);
                    MatOfKeyPoint objectKeyPointsOtherFrame = new MatOfKeyPoint();
                    featureDetector.detect(objectImageOtherFrame, objectKeyPointsOtherFrame);
                    otherFrameDescriptors = new MatOfKeyPoint();
                    featureDetector.compute(objectImageOtherFrame, objectKeyPointsOtherFrame, otherFrameDescriptors);
                    descriptorsMap.put(otherFrame.getPath(), otherFrameDescriptors);
                }


                List<MatOfDMatch> matches = new LinkedList<>();
                DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
                descriptorMatcher.knnMatch(centralFrameDescriptors, otherFrameDescriptors, matches, 2);

                LinkedList<DMatch> goodMatchesList = new LinkedList<>();
                float nndrRatio = 0.7f;
                for (int i = 0; i < matches.size(); i++) {
                    MatOfDMatch matofDMatch = matches.get(i);
                    DMatch[] dmatcharray = matofDMatch.toArray();
                    DMatch m1 = dmatcharray[0];
                    DMatch m2 = dmatcharray[1];

                    if (m1.distance <= m2.distance * nndrRatio) {
                        goodMatchesList.addLast(m1);

                    }
                }
                totalmatches = Integer.valueOf(goodMatchesList.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!matchesMap.containsKey(key)) {
            matchesMap.put(key, totalmatches);
        }
        if (!matchesMap.containsKey(reversekey)) {
            matchesMap.put(reversekey, totalmatches);
        }
        otherFrame.setMatches(totalmatches);
    }

    public void checkSimilarity(Frame frame1, Frame frame2) throws Exception {

        String imageFrame = MAIN_FOLDER.concat(frame1.getPath()).concat(".png");
        Mat img = Imgcodecs.imread(imageFrame, Imgcodecs.IMREAD_COLOR);
        String imageOtherFrame = MAIN_FOLDER.concat(frame2.getPath()).concat(".png");
        Mat templ = Imgcodecs.imread(imageOtherFrame, Imgcodecs.IMREAD_COLOR);
        int match_method = Imgproc.TM_CCORR_NORMED;

        Mat result = new Mat();
        Mat img_display = new Mat();
        img.copyTo(img_display);
        int result_cols = img.cols() - img.cols() + 1;
        int result_rows = img.rows() - img.rows() + 1;
        result.create(result_rows, result_cols, CvType.CV_32FC1);

        Imgproc.matchTemplate(img, templ, result, match_method);
        Core.normalize(result, img_display, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        frame2.setPercentual(mmr.maxVal);
    }

}

