package vn.com.gsoft.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "UserProfile")
public class UserProfile extends BaseEntity{
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name = "UserId")
    private Long userId;
    @Column(name = "UserName")
    private String userName;
    @Column(name = "Password")
    private String password;
    @Column(name = "TenDayDu")
    private String tenDayDu;
    @Column(name = "Email")
    private String email;
    @Column(name = "SoDienThoai")
    private String soDienThoai;
    @Column(name = "MaNhaThuoc")
    private String maNhaThuoc;
    @Column(name = "HoatDong")
    private Boolean hoatDong;
    @Column(name = "SoCMT")
    private String soCMT;
    @Column(name = "RegionId")
    private Long regionId;
    @Column(name = "CityId")
    private Long cityId;
    @Column(name = "WardId")
    private Long wardId;
    @Column(name = "Addresses")
    private String addresses;
    @Column(name = "TokenDevice")
    private String tokenDevice;
    @Column(name = "TokenBrowser")
    private String tokenBrowser;
    @Column(name = "IsVerificationAccount")
    private Boolean isVerificationAccount;
    @Column(name = "EntityId")
    private Long entityId;
}

