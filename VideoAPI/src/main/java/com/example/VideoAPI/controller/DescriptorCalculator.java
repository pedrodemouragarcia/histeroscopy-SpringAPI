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
        System.out.println(frame2.getPath() + "-  Difference - Max: " + mmr.maxVal + " - Min: " + mmr.minVal);
        frame2.setPercentual(mmr.maxVal);
//        Point matchLoc = mmr.minLoc;
////        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
////            matchLoc = mmr.minLoc;
////        } else {
////            matchLoc = mmr.maxLoc;
////        }
//        Imgproc.rectangle(img_display, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
//                new Scalar(0, 0, 0), 2, 8, 0);
//        Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
//                new Scalar(0, 0, 0), 2, 8, 0);
//        Image tmpImg = HighGui.toBufferedImage(img_display);
//        ImageIcon icon = new ImageIcon(tmpImg);
        //imgDisplay.setIcon(icon);
//        result.convertTo(result, CvType.CV_8UC1, 255.0);
//        tmpImg = HighGui.toBufferedImage(result);
//        icon = new ImageIcon(tmpImg);
        //resultDisplay.setIcon(icon);

//        String imageFrame1 = MAIN_FOLDER.concat(frame1.getPath()).concat(".png");
//        String imageFrame2 = MAIN_FOLDER.concat(frame2.getPath()).concat(".png");
//        BufferedImage img1 = ImageIO.read(new File(imageFrame1));
//        BufferedImage img2 = ImageIO.read(new File(imageFrame2));
//        int w1 = img1.getWidth();
//        int w2 = img2.getWidth();
//        int h1 = img1.getHeight();
//        int h2 = img2.getHeight();
//        if ((w1!=w2)||(h1!=h2)) {
//            System.out.println("Both images should have same dimwnsions");
//        } else {
//            long diff = 0;
//            for (int j = 0; j < h1; j++) {
//                for (int i = 0; i < w1; i++) {
//                    //Getting the RGB values of a pixel
//                    int pixel1 = img1.getRGB(i, j);
//                    Color color1 = new Color(pixel1, true);
//                    int r1 = color1.getRed();
//                    int g1 = color1.getGreen();
//                    int b1 = color1.getBlue();
//                    int pixel2 = img2.getRGB(i, j);
//                    Color color2 = new Color(pixel2, true);
//                    int r2 = color2.getRed();
//                    int g2 = color2.getGreen();
//                    int b2= color2.getBlue();
//                    //sum of differences of RGB values of the two images
//                    long data = Math.abs(r1-r2)+Math.abs(g1-g2)+ Math.abs(b1-b2);
//                    diff = diff+data;
//                }
//            }
//            double avg = diff/(w1*h1*3);
//            double percentage = (avg/255)*100;
//            frame2.setPercentual(percentage);
//            System.out.println("Difference: "+percentage);
//        }
    }

}

