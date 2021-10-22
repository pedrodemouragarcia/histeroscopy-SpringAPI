package com.example.VideoAPI.model;


import org.opencv.core.KeyPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "frames")
public class Frame {

    @Id
    private String id;

    private BigDecimal posicaoX;
    private BigDecimal posicaoY;
    private BigDecimal posicaoZ;
    private BigDecimal anguloX;
    private BigDecimal anguloY;
    private Integer time;
    private String path;
    List<KeyPoint> keyPointList = new ArrayList<>();
    List<KeyPoint> descriptorsList = new ArrayList<>();

    private String rightNeighbor;
    private String leftNeighbor;

    private String upperNeighbor;
    private String lowerNeighbor;

    private String rightUpperNeighbor;
    private String rightLowerNeighbor;

    private String leftUpperNeighbor;
    private String leftLowerNeighbor;

    private String rightSpaceNeighbor;
    private String leftSpaceNeighbor;

    private String upperSpaceNeighbor;
    private String lowerSpaceNeighbor;

    private String frontSpaceNeighbor;
    private String backSpaceNeighbor;

    private String nextTimeFrame;

    @Transient
    private Integer matches;

    @Transient
    private Double percentual;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getPosicaoX() {
        return posicaoX;
    }

    public void setPosicaoX(BigDecimal posicaoX) {
        this.posicaoX = posicaoX;
    }

    public BigDecimal getPosicaoY() {
        return posicaoY;
    }

    public void setPosicaoY(BigDecimal posicaoY) {
        this.posicaoY = posicaoY;
    }

    public BigDecimal getPosicaoZ() {
        return posicaoZ;
    }

    public void setPosicaoZ(BigDecimal posicaoZ) {
        this.posicaoZ = posicaoZ;
    }

    public BigDecimal getAnguloX() {
        return anguloX;
    }

    public void setAnguloX(BigDecimal anguloX) {
        this.anguloX = anguloX;
    }

    public BigDecimal getAnguloY() {
        return anguloY;
    }

    public void setAnguloY(BigDecimal anguloY) {
        this.anguloY = anguloY;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<KeyPoint> getKeyPointList() {
        return keyPointList;
    }

    public void setKeyPointList(List<KeyPoint> keyPointList) {
        this.keyPointList = keyPointList;
    }

    public List<KeyPoint> getDescriptorsList() {
        return descriptorsList;
    }

    public void setDescriptorsList(List<KeyPoint> descriptorsList) {
        this.descriptorsList = descriptorsList;
    }

    public Integer getMatches() {
        return matches;
    }

    public void setMatches(Integer matches) {
        this.matches = matches;
    }

    public String getRightNeighbor() {
        return rightNeighbor;
    }

    public void setRightNeighbor(String rightNeighbor) {
        this.rightNeighbor = rightNeighbor;
    }

    public String getLeftNeighbor() {
        return leftNeighbor;
    }

    public void setLeftNeighbor(String leftNeighbor) {
        this.leftNeighbor = leftNeighbor;
    }

    public String getUpperNeighbor() {
        return upperNeighbor;
    }

    public void setUpperNeighbor(String upperNeighbor) {
        this.upperNeighbor = upperNeighbor;
    }

    public String getLowerNeighbor() {
        return lowerNeighbor;
    }

    public void setLowerNeighbor(String lowerNeighbor) {
        this.lowerNeighbor = lowerNeighbor;
    }

    public String getRightUpperNeighbor() {
        return rightUpperNeighbor;
    }

    public void setRightUpperNeighbor(String rightUpperNeighbor) {
        this.rightUpperNeighbor = rightUpperNeighbor;
    }

    public String getRightLowerNeighbor() {
        return rightLowerNeighbor;
    }

    public void setRightLowerNeighbor(String rightLowerNeighbor) {
        this.rightLowerNeighbor = rightLowerNeighbor;
    }

    public String getLeftUpperNeighbor() {
        return leftUpperNeighbor;
    }

    public void setLeftUpperNeighbor(String leftUpperNeighbor) {
        this.leftUpperNeighbor = leftUpperNeighbor;
    }

    public String getLeftLowerNeighbor() {
        return leftLowerNeighbor;
    }

    public void setLeftLowerNeighbor(String leftLowerNeighbor) {
        this.leftLowerNeighbor = leftLowerNeighbor;
    }

    public String getRightSpaceNeighbor() {
        return rightSpaceNeighbor;
    }

    public void setRightSpaceNeighbor(String rightSpaceNeighbor) {
        this.rightSpaceNeighbor = rightSpaceNeighbor;
    }

    public String getLeftSpaceNeighbor() {
        return leftSpaceNeighbor;
    }

    public void setLeftSpaceNeighbor(String leftSpaceNeighbor) {
        this.leftSpaceNeighbor = leftSpaceNeighbor;
    }

    public String getUpperSpaceNeighbor() {
        return upperSpaceNeighbor;
    }

    public void setUpperSpaceNeighbor(String upperSpaceNeighbor) {
        this.upperSpaceNeighbor = upperSpaceNeighbor;
    }

    public String getLowerSpaceNeighbor() {
        return lowerSpaceNeighbor;
    }

    public void setLowerSpaceNeighbor(String lowerSpaceNeighbor) {
        this.lowerSpaceNeighbor = lowerSpaceNeighbor;
    }

    public String getFrontSpaceNeighbor() {
        return frontSpaceNeighbor;
    }

    public void setFrontSpaceNeighbor(String frontSpaceNeighbor) {
        this.frontSpaceNeighbor = frontSpaceNeighbor;
    }

    public String getBackSpaceNeighbor() {
        return backSpaceNeighbor;
    }

    public void setBackSpaceNeighbor(String backSpaceNeighbor) {
        this.backSpaceNeighbor = backSpaceNeighbor;
    }

    public String getNextTimeFrame() {
        return nextTimeFrame;
    }

    public void setNextTimeFrame(String nextTimeFrame) {
        this.nextTimeFrame = nextTimeFrame;
    }

    public Double getPercentual() {
        return  percentual == null ? 0.0 : percentual;
    }

    public void setPercentual(Double percentual) {
        this.percentual = percentual;
    }
}
