package vn.com.gsoft.transaction.service;

import vn.com.gsoft.transaction.model.dto.GiaoDichHangHoaRes;

import java.util.List;

public interface GiaoDichHangHoaService {

    void saveData(String playLoad);
    void deleteData(String payload);
}