package com.example.VideoAPI;

import com.example.VideoAPI.controller.FrameController;
import com.example.VideoAPI.model.Frame;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class VideoApiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void neighbors() {
        List<Frame> allNeighbors = getFrames();
        FrameController controller = new FrameController();
        Frame centralFrame = allNeighbors.stream().filter(f -> f.getId().equals("000001")).findFirst().orElse(null);
        controller.defineNeighbors(centralFrame, allNeighbors);

        Assumptions.assumeTrue(centralFrame.getRightNeighbor().equals("000101"));
        Assumptions.assumeTrue(centralFrame.getLeftNeighbor().equals("000-101"));
        Assumptions.assumeTrue(centralFrame.getUpperNeighbor().equals("000011"));
        Assumptions.assumeTrue(centralFrame.getLowerNeighbor().equals("0000-11"));
        Assumptions.assumeTrue(centralFrame.getRightUpperNeighbor().equals("000111"));
        Assumptions.assumeTrue(centralFrame.getRightLowerNeighbor().equals("0001-11"));
        Assumptions.assumeTrue(centralFrame.getLeftUpperNeighbor().equals("000-111"));
        Assumptions.assumeTrue(centralFrame.getLeftLowerNeighbor().equals("000-1-11"));
        Assumptions.assumeTrue(centralFrame.getRightSpaceNeighbor().equals("100001"));
        Assumptions.assumeTrue(centralFrame.getLeftSpaceNeighbor().equals("-100001"));
        Assumptions.assumeTrue(centralFrame.getUpperSpaceNeighbor().equals("010001"));
        Assumptions.assumeTrue(centralFrame.getLowerSpaceNeighbor().equals("0-10001"));
        Assumptions.assumeTrue(centralFrame.getFrontSpaceNeighbor().equals("001001"));
        Assumptions.assumeTrue(centralFrame.getBackSpaceNeighbor().equals("00-1001"));
    }

    private Frame getFrame(BigDecimal pX, BigDecimal pY, BigDecimal pZ, BigDecimal anguloX, BigDecimal anguloY, String id, Integer time) {
        Frame frame = new Frame();
        frame.setPosicaoX(pX);
        frame.setPosicaoY(pY);
        frame.setPosicaoZ(pZ);
        frame.setAnguloX(anguloX);
        frame.setAnguloY(anguloY);
        frame.setId(id);
        frame.setPath(id);
        frame.setTime(time);
        return frame;
    }

    private List<Frame> getFrames() {
        List<Frame> frames = new ArrayList<>();
        for (int pX = -2; pX < 3; pX++) {
            for (int pY = -2; pY < 3; pY++) {
                for (int pZ = -2; pZ < 3; pZ++) {
                    for (int x = -2; x < 3; x++) {
                        for (int y = -2; y < 3; y++) {
                            for (int t = 0; t < 3; t++) {
                                StringBuilder builder = new StringBuilder();
                                builder.append(pX);
                                builder.append(pY);
                                builder.append(pZ);
                                builder.append(x);
                                builder.append(y);
                                builder.append(t);
                                frames.add(getFrame(BigDecimal.valueOf(pX),//
                                        BigDecimal.valueOf(pY),//
                                        BigDecimal.valueOf(pZ),//
                                        BigDecimal.valueOf(x),//
                                        BigDecimal.valueOf(y),//
                                        builder.toString(), t));
                            }
                        }
                    }
                }
            }
        }
        return frames;
    }

}
