package com.solvd.persistence.mybatisImpl;

import com.solvd.domain.RealEstate;
import com.solvd.persistence.Config;
import com.solvd.persistence.repositories.RealEstateRepository;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class RealEstateRepositoryMybatisImpl implements RealEstateRepository {
    @Override
    public void create(RealEstate realEstate, long clientId, long addressId) {
        try (SqlSession sqlSession = Config.getSessionFactory().openSession(true)) {
            RealEstateRepository realEstateRepository = sqlSession.getMapper(RealEstateRepository.class);
            realEstateRepository.create(realEstate, clientId, addressId);
        }
    }

    @Override
    public void deleteById(long realEstateId) {
        try (SqlSession sqlSession = Config.getSessionFactory().openSession(true)) {
            RealEstateRepository realEstateRepository = sqlSession.getMapper(RealEstateRepository.class);
            realEstateRepository.deleteById(realEstateId);
        }
    }

    @Override
    public RealEstate findById(long realEstateId) {
        try (SqlSession sqlSession = Config.getSessionFactory().openSession(true)) {
            RealEstateRepository realEstateRepository = sqlSession.getMapper(RealEstateRepository.class);
            return realEstateRepository.findById(realEstateId);
        }
    }

    @Override
    public List<RealEstate> getAll() {
        try (SqlSession sqlSession = Config.getSessionFactory().openSession(true)) {
            RealEstateRepository realEstateRepository = sqlSession.getMapper(RealEstateRepository.class);
            return realEstateRepository.getAll();
        }
    }
}
