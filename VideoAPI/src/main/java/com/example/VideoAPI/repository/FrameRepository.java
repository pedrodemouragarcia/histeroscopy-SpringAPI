package com.example.VideoAPI.repository;

import com.example.VideoAPI.model.Frame;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.util.List;

public interface FrameRepository extends MongoRepository<Frame, String> {

    List<Frame> findByPosicaoXAndPosicaoYAndPosicaoZAndAnguloXAndAnguloY(BigDecimal posicaoX, BigDecimal posicaoY, BigDecimal posicaoZ, BigDecimal anguloX, BigDecimal anguloY);

    List<Frame> findByTime(Integer time);

    List<Frame> findByPath(String path);

}
