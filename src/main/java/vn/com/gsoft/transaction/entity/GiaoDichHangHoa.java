package vn.com.gsoft.transaction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GiaoDichHangHoa")

public class GiaoDichHangHoa extends BaseEntity {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id ;
    @Column(name = "ThuocId")
    private Long thuocId;
    @Column(name = "TenThuoc")
    private String tenThuoc;
    @Column(name = "NhomThuocId")
    private Integer nhomThuocId;
    @Column(name = "TenNhomThuoc")
    private String tenNhomThuoc;
    @Column(name = "NhomDuocLyId")
    private Integer nhomDuocLyId;
    @Column(name = "TenNhomDuocLy")
    private String tenNhomDuocLy;
    @Column(name = "SoLuong")
    private BigDecimal soLuong;
    @Column(name = "GiaNhap")
    private BigDecimal giaNhap;
    @Column(name = "GiaBan")
    private BigDecimal giaBan;
    @Column(name = "TenDonVi")
    private String tenDonVi;
    @Column(name = "TenHoatChat")
    private String tenHoatChat;
    @Column(name = "NgayGiaoDich")
    private Date ngayGiaoDich;
    @Column(name = "DongBang")
    private Boolean dongBang;
    @Column(name = "LoaiGiaoDich")
    private Integer loaiGiaoDich;
    @Column(name = "MaCoSo")
    private String maCoSo;
    @Column(name = "SoLuongQuyDoi")
    private BigDecimal soLuongQuyDoi;
    @Column(name = "ThuocIdCs")
    private Integer thuocIdCs;
    @Column(name = "NhomHoatChatId")
    private Integer nhomHoatChatId;
    @Column(name = "NhomNganhHangId")
    private Integer nhomNganhHangId;
    @Column(name = "MaPhieuChiTiet")
    private Integer maPhieuChiTiet;
    @Column(name = "TenNhomNganhHang")
    private String tenNhomNganhHang;
    @Column(name = "TongBanVoiGiaNhap")
    private BigDecimal tongBanVoiGiaNhap;
    @Column(name = "TongBan")
    private BigDecimal tongBan;
    @Column(name = "TSLN")
    private BigDecimal tsln;
    @Column(name = "GiaNhapCS")
    private BigDecimal giaNhapCS;
    @Column(name = "GiaBanCS")
    private BigDecimal giaBanCS;
    @Transient
    @JsonIgnore
    private Boolean isModified;
}
