package vn.com.gsoft.transaction.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GiaoDichHangHoaRes {
    private Long thuocId;
    private Integer nhomThuocId;
    private Integer nhomDuocLyId;
    private Integer nhomHoatChatId;
    private Integer nhomNganhHangId;
    private BigDecimal soLuong;
    private BigDecimal giaBanCS;
    private BigDecimal giaNhapCS;
    private Integer noteType;
    private String ngayGiaoDich;
    private String maCoSo;
    private Integer maPhieuChiTiet;
    private Integer thuocIDCoSo;
    private String tenDonVi;
    private Boolean isModified;
    private BigDecimal tongBan;
    private BigDecimal giaBanMinCS;
    private BigDecimal giaNhapMinCS;
}
