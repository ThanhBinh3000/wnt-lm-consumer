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
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
                        gd.setGiaBan(x.getGiaBanCS());

                    }else {
                        gd.setGiaNhap(x.getGiaNhapCS());
                    }
                    gd.setSoLuong(x.getSoLuong());
                }else {
                    if(x.getNoteType().equals(ENoteType.Delivery)){
                        gd.setGiaBan(x.getGiaBanCS().divide(item.getHeSo()));
                    }else {
                        gd.setGiaNhap(x.getGiaNhapCS().divide(item.getHeSo()));
                    }
                    gd.setSoLuong(x.getSoLuong().multiply(item.getHeSo()));
                }
                try{
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime dateTime = LocalDateTime.parse(x.getNgayGiaoDich(), formatter);
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());

                    // Chuyển đổi ZonedDateTime sang Instant
                    Date ngayGd = Date.from(zonedDateTime.toInstant());

                    SimpleDateFormat formatPattern = new SimpleDateFormat("yyyy/MM/dd");
                    String formatDate = formatPattern.format(ngayGd);

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
                    if(x.getNoteType() == ENoteType.Delivery){
                        gd.setGiaBanCS(x.getGiaBanMinCS());
                        gd.setGiaNhapCS(x.getGiaNhapMinCS());
                    }
                    BigDecimal giaCu = BigDecimal.ZERO;
                    BigDecimal slCu = BigDecimal.ZERO;
                    BigDecimal gncsCu = BigDecimal.ZERO;
                    BigDecimal gbcsCu = BigDecimal.ZERO;
                    if(x.getIsModified()){
                        //kiểm tra phiếu đó và update
                        var gddb = giaoDichHangHoaRepository.findByMaPhieuChiTietAndLoaiGiaoDich(x.getMaPhieuChiTiet(), x.getNoteType());
                        if(gddb != null){
                            gd.setId(gddb.get().getId());
                            if(x.getNoteType() == ENoteType.Delivery){
                                giaCu = gddb.get().getTongBan();
                                slCu = gddb.get().getSoLuong();
                                gncsCu = gddb.get().getGiaNhapCS();
                                gbcsCu = gddb.get().getGiaBanCS();
                            }
                        }
                    }
                    giaoDichHangHoaRepository.save(gd);
                    //kiểm tra dữ liệu hợp lệ không
                    if(x.getNoteType() == ENoteType.Delivery && x.getGiaBanMinCS().compareTo(BigDecimal.ZERO) > 0
                            && x.getGiaNhapMinCS().compareTo(BigDecimal.ZERO) > 0
                            && x.getGiaBanMinCS().divide(x.getGiaNhapMinCS(), 2, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(10)) <= 0)
                    {
                        var entityNameDate = "GiaoDichHangHoa_T" + dateTime.getMonthValue()+"_"+ dateTime.getYear();
                        Optional<Tuple> table = giaoDichHangHoaRepository.checkTableExit(entityNameDate);
                        //cộng dồn các ngày
                        if(table.isEmpty()){
                            giaoDichHangHoaRepository.createTable(createTable_T_ANY(entityNameDate));
                            var index = createIndex_T_ANY(entityNameDate);
                            giaoDichHangHoaRepository.createTable(index);
                            //insert
                            giaoDichHangHoaRepository.updateData(insert_T_ANY(entityNameDate, gd, formatDate.replace("/","")));
                        }else {
                            //kiểm tra tồn tại hàng hoá đó không
                            var findByThuocIdByDateQuery = "Select * from "+ entityNameDate + " where thuocId =" + x.getThuocId() +
                                    " AND ngayGiaoDich = '" + formatDate.replace("/","") + "'";
                            List<Tuple> itemByDate = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByDateQuery);
                            if(itemByDate.size() > 0){
                                var query = "Update "+ entityNameDate + " set tongBan = tongBan + " + gd.getTongBan().subtract(giaCu) +
                                        " , tongSoLuong = " + gd.getSoLuong().subtract(slCu) +
                                        " , giaBanCS = ((giaBanCS - " + gbcsCu + ") +" + gd.getGiaBanCS() + ")/2" +
                                        " , giaNhapCS = ((giaNhapCS -" + gncsCu + ") +" + gd.getGiaNhapCS() + ")/2" +
                                        ",gbMin = (CASE" +
                                        " WHEN gbMin > "+gd.getGiaBan()+" THEN "+gd.getGiaBan() +
                                        " ELSE gbMin" +
                                        " END) "+
                                        ",gbMax = (CASE" +
                                        " WHEN gbMax < "+gd.getGiaBan()+" THEN "+gd.getGiaBan() +
                                        " ELSE gbMax" +
                                        " END)"+
                                        " Where thuocId = " + gd.getThuocId() +
                                        " AND ngayGiaoDich = '" + formatDate.replace("/","") + "'";
                                giaoDichHangHoaRepository.updateData(query);
                            }else{
                                var query = insert_T_ANY(entityNameDate, gd, formatDate.replace("/",""));
                                var result = giaoDichHangHoaRepository.updateData(query);
                            }
                        }


                        var entityNameByMonth = "GiaoDichHangHoa_T0_"+ dateTime.getYear();
                        Optional<Tuple> tableMonth = giaoDichHangHoaRepository.checkTableExit(entityNameByMonth);
                        //cộng dồn các tháng
                        if(tableMonth.isEmpty()){
                            giaoDichHangHoaRepository.createTable(createTable_T_0(entityNameByMonth));
                            giaoDichHangHoaRepository.createTable(createIndex_T_0(entityNameByMonth));
                            //insert
                            giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByMonth, gd, dateTime.getMonthValue()));
                            giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByMonth, gd, 0));
                        }else {
                            //kiểm tra tồn tại hàng hoá đó không
                            var findByThuocIdByMonthQuery = "Select * from "+ entityNameByMonth + " where thuocId =" + x.getThuocId() +
                                    " AND type in (" +dateTime.getMonthValue()+", 0)";
                            List<Tuple> itemByMonth = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByMonthQuery);
                            if(itemByMonth.size() > 0){
                                var query =  "Update "+ entityNameByMonth + " set tongBan = tongBan + " + gd.getTongBan().subtract(giaCu) +
                                        " , tongSoLuong = " + gd.getSoLuong().subtract(slCu) +
                                        " , giaBanCS = ((giaBanCS - " + gbcsCu + ") +" + gd.getGiaBanCS() + ")/2" +
                                        " , giaNhapCS = ((giaNhapCS -" + gncsCu + ") +" + gd.getGiaNhapCS() + ")/2" +
                                        ",gbMin = (CASE" +
                                        " WHEN gbMin > "+gd.getGiaBan()+" THEN "+gd.getGiaBan() +
                                        " ELSE gbMin" +
                                        " END) "+
                                        ",gbMax = (CASE" +
                                        " WHEN gbMax < "+gd.getGiaBan()+" THEN "+gd.getGiaBan() +
                                        " ELSE gbMax" +
                                        " END)"+
                                        " Where thuocId = " + gd.getThuocId() +  " AND type in (" +dateTime.getMonthValue()+", 0)";
                                var result = giaoDichHangHoaRepository.updateData(query);
                            }else {
                                giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByMonth, gd, dateTime.getMonthValue()));
                                giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByMonth, gd, 0));
                            }
                        }

                        var entityNameByYear = "GiaoDichHangHoa_T0_0";
                        Optional<Tuple> tableYear = giaoDichHangHoaRepository.checkTableExit(entityNameByYear);
                        //cộng dồn các tháng
                        if(tableYear.isEmpty()){
                            giaoDichHangHoaRepository.createTable(createTable_T_0(entityNameByYear));
                            giaoDichHangHoaRepository.createTable(createIndex_T_0(entityNameByYear));
                            //insert
                            giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByYear, gd, dateTime.getYear()));
                            giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByYear, gd, 0));
                        }else {
                            var findByThuocIdByYearQuery = "Select * from "+ entityNameByYear + " where thuocId =" + x.getThuocId() +
                                    " AND type in (" +dateTime.getYear()+", 0)";
                            List<Tuple> itemByYear = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByYearQuery);
                            if(itemByYear.size() > 0){
                                var query =  "Update "+ entityNameByYear + " set tongBan = tongBan + " + gd.getTongBan().subtract(giaCu) +
                                        " , tongSoLuong = " + gd.getSoLuong().subtract(slCu) +
                                        " , giaBanCS = ((giaBanCS - " + gbcsCu + ") +" + gd.getGiaBanCS() + ")/2" +
                                        " , giaNhapCS = ((giaNhapCS -" + gncsCu + ") +" + gd.getGiaNhapCS() + ")/2" +
                                        ",gbMin = (CASE" +
                                        " WHEN gbMin > "+gd.getGiaBan()+" THEN "+gd.getGiaBan() +
                                        " ELSE gbMin" +
                                        " END) "+
                                        ",gbMax = (CASE" +
                                        " WHEN gbMax < "+gd.getGiaBan()+" THEN "+gd.getGiaBan() +
                                        " ELSE gbMax" +
                                        " END)"+
                                        " Where thuocId = " + gd.getThuocId()  +  " AND type in (" +dateTime.getYear()+", 0)";
                                giaoDichHangHoaRepository.updateData(query);
                            }else {
                                giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByYear, gd, dateTime.getYear()));
                                giaoDichHangHoaRepository.updateData(insert_T_0(entityNameByYear, gd, 0));
                            }
                        }
                    }
                    if(x.getNoteType() == ENoteType.Receipt && x.getGiaNhapCS().compareTo(BigDecimal.ZERO) > 0){
                        var entityNameDate = "GiaoDichHangHoa_T" + dateTime.getMonthValue()+"_"+ dateTime.getYear();
                        Optional<Tuple> table = giaoDichHangHoaRepository.checkTableExit(entityNameDate);
                        //cộng dồn các ngày
                        if(table.isPresent()) {
                            //kiểm tra tồn tại hàng hoá đó không
                            var findByThuocIdByDateQuery = "Select * from " + entityNameDate + " where thuocId =" + x.getThuocId() +
                                    " AND ngayGiaoDich = '" + formatDate.replace("/", "") + "'";
                            List<Tuple> itemByDate = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByDateQuery);
                            if (itemByDate.size() > 0) {
                                var query = "Update "+ entityNameDate + " set "+
                                        "gnMin = (CASE" +
                                        " WHEN gnMin > "+gd.getGiaNhap()+" THEN "+gd.getGiaNhap() +
                                        " ELSE gnMin" +
                                        " END) "+
                                        ",gbMax = (CASE" +
                                        " WHEN gnMax < "+gd.getGiaNhap()+" THEN "+gd.getGiaNhap() +
                                        " ELSE gnMax" +
                                        " END)"+
                                        " Where thuocId = " + gd.getThuocId() +
                                        " AND ngayGiaoDich = '" + formatDate.replace("/", "") + "'";
                                giaoDichHangHoaRepository.updateData(query);
                            }
                        }


                        var entityNameByMonth = "GiaoDichHangHoa_T0_"+ dateTime.getYear();
                        Optional<Tuple> tableMonth = giaoDichHangHoaRepository.checkTableExit(entityNameByMonth);
                        //cộng dồn các tháng
                        if(tableMonth.isPresent()){
                            //kiểm tra tồn tại hàng hoá đó không
                            var findByThuocIdByMonthQuery = "Select * from "+ entityNameByMonth + " where thuocId =" + x.getThuocId() +
                                    " AND type in (" +dateTime.getMonthValue()+", 0)";
                            List<Tuple> itemByMonth = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByMonthQuery);
                            if(itemByMonth.size() > 0){
                                var query = "Update "+ entityNameByMonth + " set "+
                                        "gnMin = (CASE" +
                                        " WHEN gnMin > "+gd.getGiaNhap()+" THEN "+gd.getGiaNhap() +
                                        " ELSE gnMin" +
                                        " END) "+
                                        ",gbMax = (CASE" +
                                        " WHEN gnMax < "+gd.getGiaNhap()+" THEN "+gd.getGiaNhap() +
                                        " ELSE gnMax" +
                                        " END)"+
                                        " Where thuocId = " + gd.getThuocId() +  " AND type in (" +dateTime.getMonthValue()+", 0)";
                                var result = giaoDichHangHoaRepository.updateData(query);
                            }
                        }

                        var entityNameByYear = "GiaoDichHangHoa_T0_0";
                        Optional<Tuple> tableYear = giaoDichHangHoaRepository.checkTableExit(entityNameByYear);
                        //cộng dồn các tháng
                        if(tableYear.isPresent()){
                            var findByThuocIdByYearQuery = "Select * from "+ entityNameByMonth + " where thuocId =" + x.getThuocId() +
                                    " AND type in (" +dateTime.getYear()+", 0)";
                            List<Tuple> itemByYear = giaoDichHangHoaRepository.finByThuocId(findByThuocIdByYearQuery);
                            if(itemByYear != null){
                                var query =  "Update "+ entityNameByYear + " set "+
                                        "gnMin = (CASE" +
                                        " WHEN gnMin > "+gd.getGiaNhap()+" THEN "+gd.getGiaNhap() +
                                        " ELSE gnMin" +
                                        " END) "+
                                        ",gbMax = (CASE" +
                                        " WHEN gnMax < "+gd.getGiaNhap()+" THEN "+gd.getGiaNhap() +
                                        " ELSE gnMax" +
                                        " END)"+
                                        " Where thuocId = " + gd.getThuocId()  +  " AND type in (" +dateTime.getYear()+", 0)";
                                giaoDichHangHoaRepository.updateData(query);
                            }
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
                "[GBMax] [decimal](18, 0) NULL," +
                "type int NULL )";
    }

    private String createIndex_T_ANY(String entityName){
        return "CREATE NONCLUSTERED INDEX [ThuocId_NgayIndex] ON " + entityName +
                " ("+
        "[ThuocId] ASC, "+
                "[NgayGiaoDich] ASC "+
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]";
    }

    private String createIndex_T_0(String entityName){
        return "CREATE NONCLUSTERED INDEX [Type_Thuoc_IdIndex] ON " + entityName +
                " ( "+
                "[ThuocId] ASC," +
                "[Type] ASC "+
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]";


    }

    private String insert_T_ANY(String entityName, GiaoDichHangHoa gd , String date){
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
                ",[GiaBanCS] "+
                ",[GiaNhapCS] "+
                ",[GNMin] "+
                ",[GNMax] "+
                ",[GBMin] "+
                ",[GBMax])"+
                "VALUES ("+gd.getThuocId()+", N'"+gd.getTenThuoc()+"', "+gd.getNhomDuocLyId()+", "+gd.getSoLuong()+", "+gd.getTongBan() +
                ",N'" + gd.getTenDonVi() + "','" + date + "'," + gd.getNhomHoatChatId() + "," + gd.getNhomNganhHangId() +
                ",N'" + gd.getTenNhomNganhHang() + "'," + gd.getGiaBanCS() + "," + gd.getGiaNhapCS() + "," + gd.getGiaNhap()
                + "," + gd.getGiaNhap() + "," + gd.getGiaBan() + "," + gd.getGiaBan() +")";
    }

    private String insert_T_0(String entityName, GiaoDichHangHoa gd, int type){
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
                ",[GiaBanCS] "+
                ",[GiaNhapCS] "+
                ",[GNMin] "+
                ",[GNMax] "+
                ",[GBMin] "+
                ",[GBMax]," +
                "type)"+
                "VALUES ("+gd.getThuocId()+", N'"+gd.getTenThuoc()+"', "+gd.getNhomDuocLyId()+", "+gd.getSoLuong()+", "+gd.getTongBan() +
                ",N'" + gd.getTenDonVi() + "'," + gd.getNhomHoatChatId() + "," + gd.getNhomNganhHangId() +
                ",N'" + gd.getTenNhomNganhHang() + "'," + gd.getGiaBanCS() + "," + gd.getGiaNhapCS() + "," + gd.getGiaNhap()
                + "," + gd.getGiaNhap() + "," + gd.getGiaBan() + "," + gd.getGiaBan()
                +"," + type + ")";
    }
}
