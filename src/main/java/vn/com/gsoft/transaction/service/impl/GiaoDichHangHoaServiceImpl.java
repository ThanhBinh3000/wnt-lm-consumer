package vn.com.gsoft.transaction.service.impl;


import com.google.gson.Gson;
import jakarta.persistence.Tuple;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.protocol.types.Field;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.gsoft.transaction.constant.ENoteType;
import vn.com.gsoft.transaction.entity.GiaoDichHangHoa;
import vn.com.gsoft.transaction.entity.HangHoa;
import vn.com.gsoft.transaction.entity.HangHoaRepository;
import vn.com.gsoft.transaction.model.dto.GiaoDichHangHoaRes;
import vn.com.gsoft.transaction.model.dto.WrapData;
import vn.com.gsoft.transaction.repository.GiaoDichHangHoaRepository;
import vn.com.gsoft.transaction.service.GiaoDichHangHoaService;
import vn.com.gsoft.transaction.service.RedisListService;

import java.math.BigDecimal;
import java.text.DateFormat;
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
                    gd.setTenThuoc(item.getTenThuoc());
                    gd.setTongBan(x.getTongBan());
                    gd.setGiaBan(x.getGiaBan());
                    gd.setGiaNhap(x.getGiaNhap());
                    BigDecimal giaCu = BigDecimal.ZERO;
                    BigDecimal slCu = BigDecimal.ZERO;
                    if(x.getIsModified()){
                        //kiểm tra phiếu đó và update
                        var gddb = giaoDichHangHoaRepository.findAllByMaPhieuChiTietAndLoaiGiaoDich(x.getMaPhieuChiTiet(), x.getNoteType());
                        if(gddb != null){
                            gd.setId(gddb.getId());
                            if(x.getNoteType() == ENoteType.Delivery){
                                giaCu = gddb.getTongBan();
                                slCu = gddb.getSoLuong();
                            }
                        }
                    }
                    giaoDichHangHoaRepository.save(gd);
                    //kiểm tra dữ liệu hợp lệ không
                    if(x.getGiaNhap().compareTo(BigDecimal.ZERO) > 0 && x.getGiaBan().compareTo(BigDecimal.ZERO) > 0
                    && x.getGiaBan().divide(x.getGiaNhap()).compareTo(BigDecimal.valueOf(10)) <= 0)
                    {
                        if(!x.getIsModified() && x.getNoteType() == ENoteType.Delivery){
                            var entityNameDate = "GiaoDichHangHoa_T" + dateTime.getMonthValue()+"_"+ dateTime.getYear();
                            Optional<Tuple> table = giaoDichHangHoaRepository.checkTableExit(entityNameDate);
                            //cộng dồn các ngày
                            if(table.isEmpty()){
                                giaoDichHangHoaRepository.createTable(createTable_T_ANY(entityNameDate));
                                giaoDichHangHoaRepository.createTable(createIndex_T_ANY(entityNameDate));
                                //insert
                                giaoDichHangHoaRepository.updateData(insert_T_ANY(entityNameDate, gd, dateTime));
                            }else {
                                //kiểm tra tồn tại hàng hoá đó không
                                var findByThuocIdByDateQuery = "Select * from "+ entityNameDate + " where thuocId =" + x.getThuocId();
                                List<Tuple> itemByDate = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByDateQuery);
                                if(itemByDate != null){
                                    var query = "Update "+ entityNameDate + " set tongBan = tongBan + " + gd.getTongBan().subtract(giaCu) +
                                            " , tongSoLuong = " + gd.getSoLuong().subtract(slCu) +
                                            " Where id = " + gd.getThuocId() + " AND ngayGiaoDich = " + dateTime ;
                                    giaoDichHangHoaRepository.updateData(query);
                                }
                            }


                            var entityNameByMonth = "GiaoDichHangHoa_T0_"+ dateTime.getYear();
                            //cộng dồn các tháng
                            if(table.isEmpty()){
                                giaoDichHangHoaRepository.createTable(createTable_T_ANY(entityNameByMonth));
                                giaoDichHangHoaRepository.createTable(createIndex_T_ANY(entityNameByMonth));
                            }else {
                                //kiểm tra tồn tại hàng hoá đó không
                                var findByThuocIdByMonthQuery = "Select * from "+ entityNameByMonth + " where thuocId =" + x.getThuocId() +
                                        " AND type in (" +dateTime.getMonthValue()+", 0)";
                                List<Tuple> itemByMonth = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByMonthQuery);
                                if(itemByMonth != null){
                                    var query = "Update "+ entityNameByMonth + " set tongBan = tongBan + " + gd.getTongBan().subtract(giaCu) +
                                            " , tongSoLuong = " + gd.getSoLuong().subtract(slCu) +
                                            " Where thuocId = " + gd.getThuocId() +  " AND type in (" +dateTime.getMonthValue()+", 0)";
                                    giaoDichHangHoaRepository.updateData(query);
                                }
                            }

                            var entityNameByYear = "GiaoDichHangHoa_T0_0";
                            //cộng dồn các tháng
                            if(table.isEmpty()){
                                giaoDichHangHoaRepository.createTable(createTable_T_ANY(entityNameByYear));
                                giaoDichHangHoaRepository.createTable(createIndex_T_ANY(entityNameByYear));
                            }else {
                                var findByThuocIdByYearQuery = "Select * from "+ entityNameByMonth + " where thuocId =" + x.getThuocId() +
                                        " AND type in (" +dateTime.getYear()+", 0)";
                                List<Tuple> itemByYear = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByYearQuery);
                                if(itemByYear != null){
                                    var query = "Update "+ entityNameByMonth + " set tongBan = tongBan + " + gd.getTongBan().subtract(giaCu) +
                                            " , tongSoLuong = " + gd.getSoLuong().subtract(slCu) +
                                            " Where thuocId = " + gd.getThuocId() +  " AND type in (" +dateTime.getYear()+", 0)";
                                    giaoDichHangHoaRepository.updateData(query);
                                }
                            }
                            //tính giá tb
                    }
                    }
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
        });
        //lưu db

    }

    private String createTable_T_ANY(String entityName){
        return "CREATE TABLE " + entityName + " (" +
                "[Id] [bigint]  primary key IDENTITY(1,1) NOT NULL, " +
                "[ThuocId] [bigint] NULL, " +
                "[TenThuoc] [nvarchar](2048) NULL, " +
                "[NhomDuocLyId] [int] NULL, " +
                "[TongSoLuong] [decimal](18, 2) NULL, " +
                "[TongNhap] [decimal](18, 2) NULL, " +
                "[TongBan] [decimal](18, 2) NULL, " +
                "[TenDonVi] [nvarchar](1024) NULL, " +
                "[NgayGiaoDich] [datetime] NULL, " +
                "[Created] [datetime] NULL, " +
                "[CreatedBy_UserId] [bigint] NULL, " +
                "[Modified] [datetime] NULL, " +
                "[ModifiedBy_UserId] [nchar](10) NULL, " +
                "[RecordStatusId] [bigint] NULL, " +
                "[NhomHoatChatId] [int] NULL, " +
                "[NhomNganhHangId] [int] NULL, " +
                "[TenNhomNganhHang] [nvarchar](1024) NULL, " +
                "[TSLN] [decimal](18, 0) NULL, " +
                "[TongBanVoiGiaNhap] [decimal](18, 0) NULL, " +
                "[GiaBanCS] [decimal](18, 0) NULL, " +
                "[GiaNhapCS] [decimal](18, 0) NULL, " +
                "[GNMin] [decimal](18, 0) NULL, " +
                "[GNMax] [decimal](18, 0) NULL, " +
                "[GBMin] [decimal](18, 0) NULL, " +
                "[GBMax] [decimal](18, 0) NULL )";
    }

    private String createTable_T_0(String entityName){
        return "CREATE TABLE " + entityName + " (" +
                "[Id] [bigint]  primary key IDENTITY(1,1) NOT NULL, " +
                "[ThuocId] [bigint] NULL, " +
                "[TenThuoc] [nvarchar](2048) NULL, " +
                "[NhomDuocLyId] [int] NULL, " +
                "[TongSoLuong] [decimal](18, 2) NULL, " +
                "[TongNhap] [decimal](18, 2) NULL, " +
                "[TongBan] [decimal](18, 2) NULL, " +
                "[TenDonVi] [nvarchar](1024) NULL, " +
                "[Created] [datetime] NULL, " +
                "[CreatedBy_UserId] [bigint] NULL, " +
                "[Modified] [datetime] NULL, " +
                "[ModifiedBy_UserId] [nchar](10) NULL, " +
                "[RecordStatusId] [bigint] NULL, " +
                "[NhomHoatChatId] [int] NULL, " +
                "[NhomNganhHangId] [int] NULL, " +
                "[TenNhomNganhHang] [nvarchar](1024) NULL, " +
                "[TSLN] [decimal](18, 0) NULL, " +
                "[TongBanVoiGiaNhap] [decimal](18, 0) NULL, " +
                "[GiaBanCS] [decimal](18, 0) NULL, " +
                "[GiaNhapCS] [decimal](18, 0) NULL, " +
                "[GNMin] [decimal](18, 0) NULL, " +
                "[GNMax] [decimal](18, 0) NULL, " +
                "[GBMin] [decimal](18, 0) NULL, " +
                "[GBMax] [decimal](18, 0) NULL )";
    }

    private String createIndex_T_ANY(String entityName){
        return "CREATE NONCLUSTERED INDEX [ThuocId_NgayIndex-20240911-231623] ON " + entityName +
                " ("+
        "[ThuocId] ASC, "+
                "[NgayGiaoDich] ASC "+
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]";
    }

    private String createIndex_T_0(String entityName){
        return "CREATE NONCLUSTERED INDEX [Type_Thuoc_IdIndex-20240912-160604] ON " + entityName +
                " ( "+
                "[ThuocId] ASC," +
                "[Type] ASC "+
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]";


    }

    private String insert_T_ANY(String entityName, GiaoDichHangHoa gd , LocalDateTime date){
        return "INSERT INTO  "+ entityName +
                " ([ThuocId] "+
                ",[TenThuoc] "+
                ",[NhomDuocLyId] "+
                ",[TongSoLuong] "+
                ",[TongBan] "+
                ",[TenDonVi] "+
                ",[NgayGiaoDich] "+
                ",[NhomHoatChatId] "+
                ",[NhomNganhHangId] "+
                ",[TenNhomNganhHang] "+
//                ",[GiaBanCS] "+
//                ",[GiaNhapCS] "+
//                ",[GNMin] "+
//                ",[GNMax] "+
//                ",[GBMin] "+
//                ",[GBMax])"+
                "VALUES ("+gd.getThuocId()+", "+gd.getTenThuoc()+", "+gd.getNhomDuocLyId()+", "+gd.getSoLuong()+", "+gd.getTongBan() +
                "," + gd.getTenDonVi() + "," + date + "," + gd.getNhomHoatChatId() + "," + gd.getNhomNganhHangId() +
                "," + gd.getTenNhomNganhHang()
                        +")";
    }

    private String insert_T_0(String entityName, GiaoDichHangHoa gd){
        return "INSERT INTO  "+ entityName +
                " ([ThuocId] "+
                ",[TenThuoc] "+
                ",[NhomDuocLyId] "+
                ",[TongSoLuong] "+
                ",[TongBan] "+
                ",[TenDonVi] "+
                ",[NhomHoatChatId] "+
                ",[NhomNganhHangId] "+
                ",[TenNhomNganhHang] "+
//                ",[GiaBanCS] "+
//                ",[GiaNhapCS] "+
//                ",[GNMin] "+
//                ",[GNMax] "+
//                ",[GBMin] "+
//                ",[GBMax])"+
                "VALUES ("+gd.getThuocId()+", "+gd.getTenThuoc()+", "+gd.getNhomDuocLyId()+", "+gd.getSoLuong()+", "+gd.getTongBan() +
                "," + gd.getTenDonVi() + "," + gd.getNhomHoatChatId() + "," + gd.getNhomNganhHangId() +
                "," + gd.getTenNhomNganhHang()
                +")";
    }
}
