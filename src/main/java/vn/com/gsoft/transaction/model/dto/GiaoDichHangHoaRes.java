package vn.com.gsoft.transaction.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GiaoDichHangHoaRes {
    private Long thuocId;
    private Integer nhomThuocId;
    private Integer nhomDuocLyId;
    private Integer nhomHoatChatId;
    private Integer nhomNganhHangId;
    private BigDecimal soLuong;
    private BigDecimal gia;
    private BigDecimal giaBan;
    private BigDecimal giaNhap;
    private Integer noteType;
    private String ngayGiaoDich;
    private String maCoSo;
    private Integer maPhieuChiTiet;
    private Integer thuocIDCoSo;
    private String tenDonVi;
    private Boolean isModified;
    private BigDecimal tongBan;
}
