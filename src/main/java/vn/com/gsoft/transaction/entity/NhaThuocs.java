package vn.com.gsoft.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NhaThuocs")
public class NhaThuocs extends BaseEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "MaNhaThuoc")
    private String maNhaThuoc;
    @Column(name = "TenNhaThuoc")
    private String tenNhaThuoc;
    @Column(name = "DiaChi")
    private String diaChi;
    @Column(name = "DienThoai")
    private String dienThoai;
    @Column(name = "NguoiDaiDien")
    private String nguoiDaiDien;
    @Column(name = "Email")
    private String email;
    @Column(name = "Mobile")
    private String mobile;
    @Column(name = "DuocSy")
    private String duocSy;
    @Column(name = "HoatDong")
    private Boolean hoatDong;
    @Column(name = "TinhThanhId")
    private Long tinhThanhId;
    @Column(name = "IsConnectivity")
    private Boolean isConnectivity;
    @Column(name = "Description")
    private String description;
    @Column(name = "RegionId")
    private Long regionId;
    @Column(name = "CityId")
    private Long cityId;
    @Column(name = "WardId")
    private Long wardId;
    @Column(name = "EntityId")
    private Integer entityId;
    @Column(name = "LevelId")
    private Integer levelId;
    @Transient
    private String role;
    @Transient
    private String level;
}

