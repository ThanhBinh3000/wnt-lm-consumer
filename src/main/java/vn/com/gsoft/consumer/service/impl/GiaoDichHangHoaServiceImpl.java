package vn.com.gsoft.consumer.service.impl;


import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.gsoft.consumer.constant.ENoteType;
import vn.com.gsoft.consumer.entity.GiaoDichHangHoa;
import vn.com.gsoft.consumer.entity.HangHoa;
import vn.com.gsoft.consumer.entity.HangHoaRepository;
import vn.com.gsoft.consumer.model.dto.GiaoDichHangHoaRes;
import vn.com.gsoft.consumer.model.dto.WrapData;
import vn.com.gsoft.consumer.repository.GiaoDichHangHoaRepository;
import vn.com.gsoft.consumer.service.GiaoDichHangHoaService;
import vn.com.gsoft.consumer.service.RedisListService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Log4j2
public class GiaoDichHangHoaServiceImpl implements GiaoDichHangHoaService {
    @Autowired
    private GiaoDichHangHoaRepository giaoDichHangHoaRepository;
    @Autowired
    private HangHoaRepository hoaRepository;

    @Autowired
    RedisListService redisListService;

    @Override
    public void saveData(String payload) {
        Gson gson = new Gson();
        TypeToken<WrapData<List<GiaoDichHangHoaRes>>> typeToken = new TypeToken<WrapData<List<GiaoDichHangHoaRes>>>() {};
        WrapData<List<GiaoDichHangHoaRes>> data = gson.fromJson(payload, typeToken.getType());
        List<GiaoDichHangHoaRes> items = data.getData();
        saveAllData(items);
    }

    @Override
    public void saveAllData(List<GiaoDichHangHoaRes> items) {
        List<GiaoDichHangHoa> giaoDichHangHoas = new ArrayList<>();
        Calendar dateArchive = Calendar.getInstance();
        dateArchive.add(Calendar.YEAR, -1);

        items.forEach(x->{
            //kiểm tra xem đơn vị
            HangHoa item = hoaRepository.findByThuocId(x.getThuocId());
            GiaoDichHangHoa gd = new GiaoDichHangHoa();
            if(item != null){
                if(item.getTenDonViLe().toLowerCase().equals(x.getTenDonVi().toLowerCase())){
                    if(x.getNoteType().equals(ENoteType.Delivery)){
                        gd.setGiaBan(x.getGia());

                    }else {
                        gd.setGiaNhap(x.getGia());
                    }
                    gd.setSoLuong(x.getSoLuong());
                }else {
                    if(x.getNoteType().equals(ENoteType.Delivery)){
                        gd.setGiaBan(x.getGia().divide(item.getHeSo()));
                    }else {
                        gd.setGiaNhap(x.getGia().divide(item.getHeSo()));
                    }
                    gd.setSoLuong(x.getSoLuong().multiply(item.getHeSo()));
                }
                try{
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime dateTime = LocalDateTime.parse(x.getNgayGiaoDich(), formatter);
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());

                    // Chuyển đổi ZonedDateTime sang Instant
                    Date ngayGd = Date.from(zonedDateTime.toInstant());
                    gd.setNgayGiaoDich(ngayGd);
                    gd.setThuocId(x.getThuocId());
                    gd.setLoaiGiaoDich(x.getNoteType());
                    gd.setMaCoSo(x.getMaCoSo());
                    gd.setDongBang(ngayGd.before(dateArchive.getTime()));
                    gd.setTenDonVi(item.getTenDonViLe());
                    gd.setThuocIdCs(x.getThuocIDCoSo());
                    gd.setNhomDuocLyId(item.getNhomDuocLyId());
                    gd.setNhomHoatChatId(item.getNhomHoatChatId());
                    gd.setNhomNganhHangId(item.getNhomNganhHangId());
                    gd.setMaPhieuChiTiet(x.getMaPhieuChiTiet());
                    if(x.getIsModified()){
                        //kiểm tra phiếu đó và update
                        var gddb = giaoDichHangHoaRepository.findAllByMaPhieuChiTiet(x.getMaPhieuChiTiet());
                        if(gddb != null){
                            gd.setId(gddb.getId());
                        }
                    }
                    giaoDichHangHoas.add(gd);
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
        });
        //xoá giao dịch cũ redis đi
        if(giaoDichHangHoas.stream().filter(x->x.getIsModified() && !x.getDongBang()).isParallel()){
            redisListService.deleteTransactions(giaoDichHangHoas.stream().filter(x->x.getIsModified() && !x.getDongBang()).toList());
        }
        //lưu redis
        redisListService.pushTransactionDataRedis(giaoDichHangHoas.stream().filter(x->!x.getDongBang()).toList());
        //lưu db
        giaoDichHangHoaRepository.saveAll(giaoDichHangHoas);
    }
}
