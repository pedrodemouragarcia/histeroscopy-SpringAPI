package com.example.VideoAPI.controller;


import com.example.VideoAPI.model.Frame;
import com.example.VideoAPI.repository.FrameRepository;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class FrameController {

    @Autowired
    FrameRepository frameRepository;

    DescriptorCalculator calculator = new DescriptorCalculator();

    @PostMapping("/frames")
    public ResponseEntity<Frame> createFrame(@RequestBody Frame frame) {
        try {
            Frame _frame = frameRepository.save(frame);
            return new ResponseEntity<>(_frame, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/framesByTime/{time}")
    public ResponseEntity<Frame> getFrameByTime(@PathVariable("time") String id) {
        List<Frame> frames = frameRepository.findByTime(Integer.valueOf(id));
        if (!frames.isEmpty()) {
            Frame frame = frames.iterator().next();
            return new ResponseEntity<>(frame, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/framesByPosition")
    public ResponseEntity<Frame> findByPositions(@RequestBody Frame frame) {
        try {
            List<Frame> frames = frameRepository.findByPosicaoXAndPosicaoYAndPosicaoZAndAnguloXAndAnguloY(frame.getPosicaoX(), frame.getPosicaoY(), frame.getPosicaoZ(), frame.getAnguloX(), frame.getAnguloY());
            if (!frames.isEmpty()) {
                Frame firstframe = frames.iterator().next();
                return new ResponseEntity<>(firstframe, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/framesByTimeDescriptor/{time}")
    public ResponseEntity<Frame> getFrameByTimeDescriptor(@PathVariable("time") String id) {
        List<Frame> frames = frameRepository.findByTime(Integer.valueOf(id));
        if (!frames.isEmpty()) {
            Frame frame = frames.iterator().next();
            new DescriptorCalculator().calculateDescriptor(frame);
            frame = frameRepository.save(frame);
            return new ResponseEntity<>(frame, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/framesByPath/{path}")
    public ResponseEntity<Frame> getFramesByPath(@PathVariable("path") String path) {
        List<Frame> frames = frameRepository.findByPath(path);
        if (!frames.isEmpty()) {
            Frame frame = frames.iterator().next();
            return new ResponseEntity<>(frame, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/framesByPath")
    public ResponseEntity<Frame> getFramesByPath(@RequestBody Frame frame) {
        List<Frame> frames = frameRepository.findByPath(frame.getPath());
        if (!frames.isEmpty()) {
            Frame result = frames.iterator().next();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/calculateDescriptors")
    public ResponseEntity<Frame> getDescriptors() {
        List<Frame> frames = frameRepository.findAll();
        if (Objects.nonNull(frames) && !frames.isEmpty()) {
            for (Frame frame : frames) {
                if (Objects.nonNull(frame)) {
                    new DescriptorCalculator().calculateDescriptor(frame);
                    frameRepository.save(frame);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @GetMapping("/calculateNeighbors")
    public ResponseEntity<Frame> getNeighbors() {
        List<Frame> frames = frameRepository.findAll();
        if (Objects.nonNull(frames) && !frames.isEmpty()) {
            List<Frame> framesCopy = new CopyOnWriteArrayList<>(frames);
            for (Frame frame : framesCopy) {
                if (Objects.nonNull(frame)) {
                    defineNeighbors(frame, frames);
                    frameRepository.save(frame);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @GetMapping("/calculateNeighborsByMatch")
    public ResponseEntity<Frame> getNeighborsByMatch() {
        List<Frame> frames = frameRepository.findAll();
        if (Objects.nonNull(frames) && !frames.isEmpty()) {
            List<Frame> framesCopy = new CopyOnWriteArrayList<>(frames);
            Iterator<Frame> iterator = framesCopy.iterator();
            Frame frame;
            while (iterator.hasNext()) {
                frame = iterator.next();
                if (Objects.nonNull(frame)) {
                    defineNeighborsByMatches(frame, frames);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @GetMapping("/calculateNextTimeFrames")
    public ResponseEntity<Frame> getNeighborsByNextTime() {
        List<Frame> frames = frameRepository.findAll();
        if (Objects.nonNull(frames) && !frames.isEmpty()) {
            List<Frame> framesCopy = new CopyOnWriteArrayList<>(frames);
            Iterator<Frame> iterator = framesCopy.iterator();
            Frame frame;
            while (iterator.hasNext()) {
                frame = iterator.next();
                if (Objects.nonNull(frame)) {
                    System.out.println("Iniciando frame: " + frame.getPath() + " ...");
                    Instant start = Instant.now();
                    calculateNextTimeFrame(frame);
                    frameRepository.save(frame);
                    Instant endCircle = Instant.now();
                    Duration timeElapsed = Duration.between(start, endCircle);
                    System.out.println("Finalizado frame: " + frame.getPath() + " - Tempo:  " + timeElapsed.toSeconds() + " segundos");

                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/calculateNextTimeFrame")
    public ResponseEntity<Frame> getNeighborByNextTime(@RequestBody Frame frame) {
        List<Frame> frames = frameRepository.findByPath(frame.getPath());
        if (!frames.isEmpty()) {
            Frame result = frames.iterator().next();
        if (Objects.nonNull(result)) {

                    System.out.println("Iniciando frame: " + frame.getPath() + " ...");
                    Instant start = Instant.now();
                    calculateNextTimeFrame(result);
                    frameRepository.save(result);
                    Instant endCircle = Instant.now();
                    Duration timeElapsed = Duration.between(start, endCircle);
                    System.out.println("Finalizado frame: " + frame.getPath() + " - Tempo:  " + timeElapsed.toSeconds() + " segundos");
                }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/calculateNeighborsByMatchSingle")
    public ResponseEntity<Frame> getNeighborsByMatchSingle(@RequestBody Frame request) {
        List<Frame> frames = frameRepository.findByPath(request.getPath());
        if (Objects.nonNull(frames) && !frames.isEmpty()) {
            Frame frame = frames.iterator().next();
                if (Objects.nonNull(frame)) {
                    List<Frame> neighbors = frameRepository.findAll();
                    defineNeighborsByMatches(frame, neighbors);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
    }

    public void defineNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        defineCircleNeighbors(centralFrame, allNeighbors);
        defineXSpaceNeighbors(centralFrame, allNeighbors);
        defineYSpaceNeighbors(centralFrame, allNeighbors);
        defineZSpaceNeighbors(centralFrame, allNeighbors);
    }


    public void defineNeighborsByMatches(Frame centralFrame, List<Frame> allNeighbors) {

        SIFT featureDetector = SIFT.create();
        String image = DescriptorCalculator.MAIN_FOLDER.concat(centralFrame.getPath()).concat(".png");
        Mat objectImage = Imgcodecs.imread(image, Imgcodecs.IMREAD_COLOR);
        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        featureDetector.detect(objectImage, objectKeyPoints);
        MatOfKeyPoint centralFrameDescriptors = new MatOfKeyPoint();
        featureDetector.compute(objectImage, objectKeyPoints, centralFrameDescriptors);

        System.out.println("Iniciando frame: " + centralFrame.getPath() + " ...");
        Instant start = Instant.now();
        defineCircleNeighborsByMatch(centralFrame, allNeighbors, featureDetector, centralFrameDescriptors);
        Instant endCircle = Instant.now();
        Duration timeElapsed = Duration.between(start, endCircle);
        System.out.println(centralFrame.getPath() + " - Tempo para calcular vizinhos circular " + timeElapsed.toSeconds() + " segundos");
        Instant startX = Instant.now();
        defineXSpaceNeighborsByMatches(centralFrame, allNeighbors, featureDetector, centralFrameDescriptors);
        Instant endX = Instant.now();
        timeElapsed = Duration.between(startX, endX);
        System.out.println(centralFrame.getPath() + " - Tempo para calcular vizinhos X " + timeElapsed.toSeconds() + " segundos");
        Instant startY = Instant.now();
        defineYSpaceNeighborsByMatches(centralFrame, allNeighbors, featureDetector, centralFrameDescriptors);
        Instant endY = Instant.now();
        timeElapsed = Duration.between(startY, endY);
        System.out.println(centralFrame.getPath() + " - Tempo para calcular vizinhos Y " + timeElapsed.toSeconds() + " segundos");
        Instant startZ = Instant.now();
        defineZSpaceNeighborsByMatches(centralFrame, allNeighbors, featureDetector, centralFrameDescriptors);
        Instant endZ = Instant.now();
        timeElapsed = Duration.between(startZ, endZ);
        System.out.println(centralFrame.getPath() + " - Tempo para calcular vizinhos Z " + timeElapsed.toSeconds() + " segundos");
        Instant end = Instant.now();
        timeElapsed = Duration.between(start, end);
        System.out.println(centralFrame.getPath() + " - Tempo para calcular todos vizinhos " + timeElapsed.toSeconds() + " segundos");
        frameRepository.save(centralFrame);
        System.out.println("Finalizou frame: " + centralFrame.getPath());
    }

    private void defineCircleNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        List<Frame> possibleCircleNeighbors = allNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == 0//
                        && centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == 0//
                        && centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == 0)//
                .collect(Collectors.toList());

        Frame rightNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, -1, 0, Comparator.comparing(Frame::getAnguloY));
        Frame leftNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, 1, 0, Comparator.comparing(Frame::getAnguloX).reversed());
        Frame upperNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, 0, -1, Comparator.comparing(Frame::getAnguloX).reversed());
        Frame lowerNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, 0, 1, Comparator.comparing(Frame::getAnguloY).reversed());
        Frame rightUpperNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, -1, -1, Comparator.comparing(Frame::getAnguloY).thenComparing(Frame::getAnguloX));
        Frame rightLowerNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, -1, 1, Comparator.comparing(Frame::getAnguloY).reversed().thenComparing(Frame::getAnguloX));
        Frame leftUpperNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, 1, -1, Comparator.comparing(Frame::getAnguloY).reversed().thenComparing(Frame::getAnguloX).reversed());
        Frame leftLowerNeighbor = getCircleNeighborClosestTime(centralFrame,//
                possibleCircleNeighbors, 1, 1, Comparator.comparing(Frame::getAnguloY).thenComparing(Frame::getAnguloX).reversed());


        centralFrame.setRightNeighbor(rightNeighbor.getPath());
        centralFrame.setLeftNeighbor(leftNeighbor.getPath());
        centralFrame.setUpperNeighbor(upperNeighbor.getPath());
        centralFrame.setLowerNeighbor(lowerNeighbor.getPath());
        centralFrame.setRightUpperNeighbor(rightUpperNeighbor.getPath());
        centralFrame.setRightLowerNeighbor(rightLowerNeighbor.getPath());
        centralFrame.setLeftUpperNeighbor(leftUpperNeighbor.getPath());
        centralFrame.setLeftLowerNeighbor(leftLowerNeighbor.getPath());
    }

    private void defineCircleNeighborsByMatch(Frame centralFrame, List<Frame> allNeighbors, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> possibleCircleNeighbors = allNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == 0//
                        && centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == 0//
                        && centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == 0)//
                .collect(Collectors.toList());

        Frame rightNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, -1, 0, Comparator.comparing(Frame::getAnguloY),//
                featureDetector, centralFrameDescriptors);
        Frame leftNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, 1, 0, Comparator.comparing(Frame::getAnguloX).reversed(),//
                featureDetector, centralFrameDescriptors);
        Frame upperNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, 0, -1, Comparator.comparing(Frame::getAnguloX).reversed(),//
                featureDetector, centralFrameDescriptors);
        Frame lowerNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, 0, 1, Comparator.comparing(Frame::getAnguloY).reversed(),//
                featureDetector, centralFrameDescriptors);
        Frame rightUpperNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, -1, -1, Comparator.comparing(Frame::getAnguloY).thenComparing(Frame::getAnguloX),//
                featureDetector, centralFrameDescriptors);
        Frame rightLowerNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, -1, 1, Comparator.comparing(Frame::getAnguloY).reversed().thenComparing(Frame::getAnguloX),//
                featureDetector, centralFrameDescriptors);
        Frame leftUpperNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, 1, -1, Comparator.comparing(Frame::getAnguloY).reversed().thenComparing(Frame::getAnguloX).reversed(),//
                featureDetector, centralFrameDescriptors);
        Frame leftLowerNeighbor = getCircleNeighborClosestMatch(centralFrame,//
                possibleCircleNeighbors, 1, 1, Comparator.comparing(Frame::getAnguloY).thenComparing(Frame::getAnguloX).reversed(),//
                featureDetector, centralFrameDescriptors);

        if (Objects.nonNull(rightNeighbor)) {
            centralFrame.setRightNeighbor(rightNeighbor.getPath());
        }
        if (Objects.nonNull(leftNeighbor)) {
            centralFrame.setLeftNeighbor(leftNeighbor.getPath());
        }
        if (Objects.nonNull(upperNeighbor)) {
            centralFrame.setUpperNeighbor(upperNeighbor.getPath());
        }
        if (Objects.nonNull(lowerNeighbor)) {
            centralFrame.setLowerNeighbor(lowerNeighbor.getPath());
        }
        if (Objects.nonNull(rightUpperNeighbor)) {
            centralFrame.setRightUpperNeighbor(rightUpperNeighbor.getPath());
        }
        if (Objects.nonNull(rightLowerNeighbor)) {
            centralFrame.setRightLowerNeighbor(rightLowerNeighbor.getPath());
        }
        if (Objects.nonNull(leftUpperNeighbor)) {
            centralFrame.setLeftUpperNeighbor(leftUpperNeighbor.getPath());
        }
        if (Objects.nonNull(leftLowerNeighbor)) {
            centralFrame.setLeftLowerNeighbor(leftLowerNeighbor.getPath());
        }
    }

    private Frame getCircleNeighborClosestTime(Frame centralFrame, List<Frame> possibleNeighbors, int aX, int aY, Comparator<Frame> comparing) {
        List<Frame> neighbors = getPossibleCircleNeighbors(centralFrame, possibleNeighbors, aX, aY);
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            Frame neighbor = neighbors.iterator().next();
            if (Objects.nonNull(neighbor)) {
                List<Frame> rightAngleNeighbors = getPossibleCircleNeighborsFinalAngle(neighbors, neighbor);
                return rightAngleNeighbors.stream()//
                        .min(Comparator.comparingInt(f -> Math.abs(f.getTime() - centralFrame.getTime())))//
                        .stream().findFirst().orElse(null);
            }
        }
        return null;
    }

    private Frame getCircleNeighborClosestMatch(Frame centralFrame, List<Frame> possibleNeighbors, int aX, int aY, Comparator<Frame> comparing, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> neighbors = getPossibleCircleNeighbors(centralFrame, possibleNeighbors, aX, aY);
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            Frame neighbor = neighbors.iterator().next();
            if (Objects.nonNull(neighbor)) {
                List<Frame> rightAngleNeighbors = getPossibleCircleNeighborsFinalAngle(neighbors, neighbor);
                if (!rightAngleNeighbors.isEmpty()) {
                    return getFrameBestMatch(centralFrame, rightAngleNeighbors, featureDetector, centralFrameDescriptors);
                }
            }
        }
        return null;
    }


    private void defineXSpaceNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        List<Frame> possibleXSpaceNeighbors = getPossibleXSpaceNeighbors(centralFrame, allNeighbors);

        Frame rightSpaceNeighbor = getSpaceXNeighborClosestTime(possibleXSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoX), centralFrame, -1);
        Frame leftSpaceNeighbor = getSpaceXNeighborClosestTime(possibleXSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoX).reversed(), centralFrame, 1);

        if (Objects.nonNull(rightSpaceNeighbor)) {
            centralFrame.setRightSpaceNeighbor(rightSpaceNeighbor.getPath());
        }
        if (Objects.nonNull(leftSpaceNeighbor)) {
            centralFrame.setLeftSpaceNeighbor(leftSpaceNeighbor.getPath());
        }
    }

    private void defineXSpaceNeighborsByMatches(Frame centralFrame, List<Frame> allNeighbors, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> possibleXSpaceNeighbors = getPossibleXSpaceNeighbors(centralFrame, allNeighbors);

        Frame rightSpaceNeighbor = getSpaceXNeighborClosestMatch(possibleXSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoX), centralFrame, -1, featureDetector, centralFrameDescriptors);
        Frame leftSpaceNeighbor = getSpaceXNeighborClosestMatch(possibleXSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoX).reversed(), centralFrame, 1, featureDetector, centralFrameDescriptors);

        if (Objects.nonNull(rightSpaceNeighbor)) {
            centralFrame.setRightSpaceNeighbor(rightSpaceNeighbor.getPath());
        }
        if (Objects.nonNull(leftSpaceNeighbor)) {
            centralFrame.setLeftSpaceNeighbor(leftSpaceNeighbor.getPath());
        }
    }


    private Frame getSpaceXNeighborClosestTime(List<Frame> possibleNeighbors, Comparator<Frame> comparing, Frame centralFrame, int nextPrevious) {
        List<Frame> neighbors = possibleNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == nextPrevious)//
                .collect(Collectors.toList());
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            return neighbors.stream()//
                    .min(Comparator.comparingInt(f -> Math.abs(f.getTime() - centralFrame.getTime())))//
                    .stream().findFirst().orElse(null);
        }
        return null;
    }

    private Frame getSpaceXNeighborClosestMatch(List<Frame> possibleNeighbors, Comparator<Frame> comparing, Frame centralFrame, int nextPrevious, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> neighbors = possibleNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == nextPrevious)//
                .collect(Collectors.toList());
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            return getFrameBestMatch(centralFrame, neighbors, featureDetector, centralFrameDescriptors);
        }
        return null;
    }

    private Frame getFrameBestMatch(Frame centralFrame, List<Frame> neighbors, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        Iterator<Frame> iterator = neighbors.iterator();
        while (iterator.hasNext()) {
            calculator.setMatchesBetweenFrames(centralFrame, iterator.next(), featureDetector, centralFrameDescriptors);
        }
        neighbors.sort(Comparator.comparing(Frame::getMatches));
        return neighbors.iterator().next();
    }

    private void defineYSpaceNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        List<Frame> possibleYSpaceNeighbors = getPossibleYSpaceNeighbors(centralFrame, allNeighbors);

        Frame upperSpaceNeighbor = getSpaceYNeighborClosestTime(possibleYSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoY), centralFrame, -1);
        Frame lowerSpaceNeighbor = getSpaceYNeighborClosestTime(possibleYSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoY).reversed(), centralFrame, 1);

        if (Objects.nonNull(upperSpaceNeighbor)) {
            centralFrame.setUpperSpaceNeighbor(upperSpaceNeighbor.getPath());
        }
        if (Objects.nonNull(lowerSpaceNeighbor)) {
            centralFrame.setLowerSpaceNeighbor(lowerSpaceNeighbor.getPath());
        }
    }

    private void defineYSpaceNeighborsByMatches(Frame centralFrame, List<Frame> allNeighbors, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> possibleYSpaceNeighbors = getPossibleYSpaceNeighbors(centralFrame, allNeighbors);

        Frame upperSpaceNeighbor = getSpaceYNeighborClosestMatch(possibleYSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoY), centralFrame, -1, featureDetector, centralFrameDescriptors);
        Frame lowerSpaceNeighbor = getSpaceYNeighborClosestMatch(possibleYSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoY).reversed(), centralFrame, 1, featureDetector, centralFrameDescriptors);

        if (Objects.nonNull(upperSpaceNeighbor)) {
            centralFrame.setUpperSpaceNeighbor(upperSpaceNeighbor.getPath());
        }
        if (Objects.nonNull(lowerSpaceNeighbor)) {
            centralFrame.setLowerSpaceNeighbor(lowerSpaceNeighbor.getPath());
        }
    }


    private Frame getSpaceYNeighborClosestTime(List<Frame> possibleNeighbors, Comparator<Frame> comparing, Frame centralFrame, int nextPrevious) {
        List<Frame> neighbors = possibleNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == nextPrevious)//
                .collect(Collectors.toList());
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            return neighbors.stream()//
                    .min(Comparator.comparingInt(f -> Math.abs(f.getTime() - centralFrame.getTime())))//
                    .stream().findFirst().orElse(null);
        }
        return null;
    }

    private Frame getSpaceYNeighborClosestMatch(List<Frame> possibleNeighbors, Comparator<Frame> comparing, Frame centralFrame, int nextPrevious, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> neighbors = possibleNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == nextPrevious)//
                .collect(Collectors.toList());
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            return getFrameBestMatch(centralFrame, neighbors, featureDetector, centralFrameDescriptors);
        }
        return null;
    }

    private void defineZSpaceNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        List<Frame> possibleZSpaceNeighbors = getPossibleZSpaceNeighbors(centralFrame, allNeighbors);

        Frame frontSpaceNeighbor = getSpaceZNeighborClosestTime(possibleZSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoZ), centralFrame, -1);
        Frame backSpaceNeighbor = getSpaceZNeighborClosestTime(possibleZSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoZ).reversed(), centralFrame, 1);

        if (Objects.nonNull(frontSpaceNeighbor)) {
            centralFrame.setFrontSpaceNeighbor(frontSpaceNeighbor.getPath());
        }
        if (Objects.nonNull(backSpaceNeighbor)) {
            centralFrame.setBackSpaceNeighbor(backSpaceNeighbor.getPath());
        }
    }

    private void defineZSpaceNeighborsByMatches(Frame centralFrame, List<Frame> allNeighbors, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> possibleZSpaceNeighbors = getPossibleZSpaceNeighbors(centralFrame, allNeighbors);

        Frame frontSpaceNeighbor = getSpaceZNeighborClosestMatch(possibleZSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoZ), centralFrame, -1, featureDetector, centralFrameDescriptors);
        Frame backSpaceNeighbor = getSpaceZNeighborClosestMatch(possibleZSpaceNeighbors,//
                Comparator.comparing(Frame::getPosicaoZ).reversed(), centralFrame, 1, featureDetector, centralFrameDescriptors);

        if (Objects.nonNull(frontSpaceNeighbor)) {
            centralFrame.setFrontSpaceNeighbor(frontSpaceNeighbor.getPath());
        }
        if (Objects.nonNull(backSpaceNeighbor)) {
            centralFrame.setBackSpaceNeighbor(backSpaceNeighbor.getPath());
        }
    }

    private Frame getSpaceZNeighborClosestTime(List<Frame> possibleNeighbors, Comparator<Frame> comparing, Frame centralFrame, int nextPrevious) {
        List<Frame> neighbors = possibleNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == nextPrevious)//
                .collect(Collectors.toList());
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            return neighbors.stream()//
                    .min(Comparator.comparingInt(f -> Math.abs(f.getTime() - centralFrame.getTime())))//
                    .stream().findFirst().orElse(null);
        }
        return null;
    }

    private Frame getSpaceZNeighborClosestMatch(List<Frame> possibleNeighbors, Comparator<Frame> comparing, Frame centralFrame, int nextPrevious, SIFT featureDetector, MatOfKeyPoint centralFrameDescriptors) {
        List<Frame> neighbors = possibleNeighbors.stream()//
                .filter(f -> centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == nextPrevious)//
                .collect(Collectors.toList());
        neighbors.sort(comparing);
        if (!neighbors.isEmpty()) {
            return getFrameBestMatch(centralFrame, neighbors, featureDetector, centralFrameDescriptors);
        }
        return null;
    }

    private List<Frame> getPossibleCircleNeighbors(Frame centralFrame, List<Frame> possibleNeighbors, int aX, int aY) {
        return possibleNeighbors.stream()//
                .filter(f -> centralFrame.getAnguloX().compareTo(f.getAnguloX()) == aX//
                        && centralFrame.getAnguloY().compareTo(f.getAnguloY()) == aY)//
                .collect(Collectors.toList());
    }


    private List<Frame> getPossibleCircleNeighborsFinalAngle(List<Frame> neighbors, Frame neighbor) {
        return new CopyOnWriteArrayList<>(neighbors).stream()
                .filter(f -> (neighbor.getAnguloX().compareTo(f.getAnguloX()) == 0)//
                        && (neighbor.getAnguloY().compareTo(f.getAnguloY()) == 0))//
                .collect(Collectors.toList());
    }

    private List<Frame> getPossibleXSpaceNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        return allNeighbors.stream()//
                .filter(f -> !(centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == 0)//
                        && (centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == 0)//
                        && (centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == 0)//
                        && centralFrame.getAnguloX().compareTo(f.getAnguloX()) == 0//
                        && centralFrame.getAnguloY().compareTo(f.getAnguloY()) == 0)//
                .collect(Collectors.toList());
    }

    private List<Frame> getPossibleYSpaceNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        return allNeighbors.stream()//
                .filter(f -> (centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == 0)//
                        && !(centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == 0)//
                        && (centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == 0)//
                        && centralFrame.getAnguloX().compareTo(f.getAnguloX()) == 0//
                        && centralFrame.getAnguloY().compareTo(f.getAnguloY()) == 0)//
                .collect(Collectors.toList());
    }

    private List<Frame> getPossibleZSpaceNeighbors(Frame centralFrame, List<Frame> allNeighbors) {
        return allNeighbors.stream()//
                .filter(f -> (centralFrame.getPosicaoX().compareTo(f.getPosicaoX()) == 0)//
                        && (centralFrame.getPosicaoY().compareTo(f.getPosicaoY()) == 0)//
                        && !(centralFrame.getPosicaoZ().compareTo(f.getPosicaoZ()) == 0)//
                        && centralFrame.getAnguloX().compareTo(f.getAnguloX()) == 0//
                        && centralFrame.getAnguloY().compareTo(f.getAnguloY()) == 0)//
                .collect(Collectors.toList());
    }

    public void calculateNextTimeFrame(Frame frame){
        List<Frame> framesMesmaPosicao = frameRepository.findByPosicaoXAndPosicaoYAndPosicaoZAndAnguloXAndAnguloY(frame.getPosicaoX(),frame.getPosicaoY(),frame.getPosicaoZ(),frame.getAnguloX(),frame.getAnguloY());
        framesMesmaPosicao.sort(Comparator.comparing(Frame::getTime));

        for(Frame fraMP : framesMesmaPosicao){
            System.out.println(fraMP.getTime() + " - " +  fraMP.getPath());
        }

        Frame next = null;
        Iterator<Frame> frameIterator = framesMesmaPosicao.iterator();
        while (frameIterator.hasNext()){
            Frame control = frameIterator.next();
            if(control.getPath().equalsIgnoreCase(frame.getPath()) && frameIterator.hasNext()){
             next = frameIterator.next();
             break;
            }
        }

        if(Objects.isNull(next) || !( next.getTime() - frame.getTime() == 1)){

            List<Frame> frames = framesMesmaPosicao.stream()//
             .filter(f -> (f.getTime() > frame.getTime()))//
                      //&& !(f.getTime() - frame.getTime() == -1))//
             .collect(Collectors.toList());


            SIFT featureDetector = SIFT.create();
            String image = DescriptorCalculator.MAIN_FOLDER.concat(frame.getPath()).concat(".png");
            Mat objectImage = Imgcodecs.imread(image, Imgcodecs.IMREAD_COLOR);
            MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
            featureDetector.detect(objectImage, objectKeyPoints);
            MatOfKeyPoint centralFrameDescriptors = new MatOfKeyPoint();
            featureDetector.compute(objectImage, objectKeyPoints, centralFrameDescriptors);


            if(!frames.isEmpty()) {
                next = getFrameBestMatch(frame, frames, featureDetector, centralFrameDescriptors);
            }else {
                next = getFrameBestMatch(frame, framesMesmaPosicao, featureDetector, centralFrameDescriptors);
            }
        }

        if(Objects.nonNull(next)) {
            frame.setNextTimeFrame(next.getPath());
        }

    }
}
