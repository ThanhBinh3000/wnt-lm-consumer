package vn.com.gsoft.transaction.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.gsoft.transaction.model.system.BaseRequest;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class LichSuCapNhatThanhVienReq extends BaseRequest {
    private Long id;
    private Date ngayCapNhat;
    private String ghiChu;
    private String maThanhVien;
    private Integer statusId;
}
