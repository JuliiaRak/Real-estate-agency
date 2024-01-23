package com.solvd.service;

import com.solvd.domain.RealEstate;

public interface RealEstateService {
    void create(RealEstate realEstate, long clientId, long addressId);
    void deleteById(long realEstateId);
}
