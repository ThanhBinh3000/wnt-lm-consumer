package vn.com.gsoft.consumer.service;

import vn.com.gsoft.consumer.model.dto.GiaoDichHangHoaRes;

import java.util.List;

public interface GiaoDichHangHoaService {

    void saveData(String playLoad);
    void saveAllData(List<GiaoDichHangHoaRes> items);
}