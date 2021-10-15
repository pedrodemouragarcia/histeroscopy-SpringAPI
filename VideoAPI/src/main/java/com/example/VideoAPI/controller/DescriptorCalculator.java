package com.example.VideoAPI.controller;

import com.example.VideoAPI.model.Frame;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;

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
            //System.out.println("Recuperou descriptors do cache");
        }

        if (matchesMap.containsKey(key) || matchesMap.containsKey(reversekey)) {
            totalmatches = matchesMap.get(key);
            //System.out.println("Recuperou matches do cache");
        } else {
            try {
//                SIFT featureDetector = SIFT.create();
//                String image = MAIN_FOLDER.concat(frame.getPath()).concat(".png");
//                Mat objectImage = Imgcodecs.imread(image, Imgcodecs.IMREAD_COLOR);
//                MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
//                featureDetector.detect(objectImage, objectKeyPoints);
//                MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
//                featureDetector.compute(objectImage, objectKeyPoints, centralFrameDescriptors);
//                frame.setKeyPointList(objectKeyPoints.toList());
//                frame.setDescriptorsList(centralFrameDescriptors.toList());

                if (!Objects.nonNull(otherFrameDescriptors)) {
                    String imageOtherFrame = MAIN_FOLDER.concat(otherFrame.getPath()).concat(".png");
                    Mat objectImageOtherFrame = Imgcodecs.imread(imageOtherFrame, Imgcodecs.IMREAD_COLOR);
                    MatOfKeyPoint objectKeyPointsOtherFrame = new MatOfKeyPoint();
                    featureDetector.detect(objectImageOtherFrame, objectKeyPointsOtherFrame);
                    otherFrameDescriptors = new MatOfKeyPoint();
                    featureDetector.compute(objectImageOtherFrame, objectKeyPointsOtherFrame, otherFrameDescriptors);
                    descriptorsMap.put(otherFrame.getPath(), otherFrameDescriptors);
                }

//                MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
//                MatOfKeyPoint objectDescriptorsOtherFrame = new MatOfKeyPoint();
//                objectDescriptors.fromList(frame.getDescriptorsList());
//                objectDescriptorsOtherFrame.fromList(otherFrame.getDescriptorsList());


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
                //System.out.println(frame.getPath() + " | " + otherFrame.getPath() + " Matches: " + totalmatches);
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

//        if (goodMatchesList.size() >= 7) {
//            System.out.println("Object Found!!!");
//            LinkedList<Point> objectPoints = new LinkedList<>();
//            LinkedList<Point> scenePoints = new LinkedList<>();
//
//            for (int i = 0; i < goodMatchesList.size(); i++) {
//                objectPoints.addLast(objectKeyPoints.get(goodMatchesList.get(i).queryIdx).pt);
//                scenePoints.addLast(sceneKeyPoints.get(goodMatchesList.get(i).trainIdx).pt);
//            }
//
//            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
//            objMatOfPoint2f.fromList(objectPoints);
//            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
//            scnMatOfPoint2f.fromList(scenePoints);
//            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

//            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
//            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);
//
//            obj_corners.put(0, 0, new double[]{0, 0});
//            obj_corners.put(1, 0, new double[]{objectImage.cols(), 0});
//            obj_corners.put(2, 0, new double[]{objectImage.cols(), objectImage.rows()});
//            obj_corners.put(3, 0, new double[]{0, objectImage.rows()});
//
//            System.out.println("Transforming object corners to scene corners...");
//            Core.perspectiveTransform(obj_corners, scene_corners, homography);
//
//            Mat img = Highgui.imread(bookScene, Highgui.CV_LOAD_IMAGE_COLOR);
//
//            Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
//            Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
//            Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
//            Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);
//
//            System.out.println("Drawing matches image...");
//            MatOfDMatch goodMatches = new MatOfDMatch();
//            goodMatches.fromList(goodMatchesList);
//
//            Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);
//
//            Highgui.imwrite("output//outputImage.jpg", outputImage);
//            Highgui.imwrite("output//matchoutput.jpg", matchoutput);
//            Highgui.imwrite("output//img.jpg", img);
//        } else {
//            System.out.println("Object Not Found");
//        }

}
