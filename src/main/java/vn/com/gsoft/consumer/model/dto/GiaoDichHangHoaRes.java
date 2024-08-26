package vn.com.gsoft.consumer.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.gsoft.consumer.model.system.BaseRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

@Data
public class GiaoDichHangHoaRes {
    private Long thuocId;
    private Integer nhomThuocId;
    private Integer nhomDuocLyId;
    private Integer nhomHoatChatId;
    private Integer nhomNganhHangId;
    private BigDecimal soLuong;
    private BigDecimal gia;
    private Integer noteType;
    private String ngayGiaoDich;
    private String maCoSo;
    private Integer maPhieuChiTiet;
    private Integer thuocIDCoSo;
    private String tenDonVi;
    private Boolean isModified;
}
