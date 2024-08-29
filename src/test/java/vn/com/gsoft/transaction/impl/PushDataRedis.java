package vn.com.gsoft.transaction.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.com.gsoft.transaction.constant.ENoteType;
import vn.com.gsoft.transaction.model.dto.GiaoDichHangHoaRes;
import vn.com.gsoft.transaction.service.GiaoDichHangHoaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
class PushDataRedis {
//    @Autowired
//    private GiaoDichHangHoaService giaoDichHangHoaService;
//    @Test
//    void saveData() throws Exception {
//        List<GiaoDichHangHoaRes> data = new ArrayList<>();
//        GiaoDichHangHoaRes data1 = new GiaoDichHangHoaRes();
//        data1.setThuocId(9739334L);
//        data1.setSoLuong(BigDecimal.valueOf(200));
//        data1.setGia(BigDecimal.valueOf(71000L));
//        data1.setIsModified(false);
//        data1.setMaPhieuChiTiet(117991349);
//        data1.setTenDonVi("hộp");
//        data1.setMaCoSo("9371");
//        data1.setNoteType(ENoteType.Delivery);
//        data1.setThuocIDCoSo(7568403);
//        data1.setNgayGiaoDich(String.valueOf(new Date()));
//        data.add(data1);
//       giaoDichHangHoaService.saveAllData(data);
//    }
}