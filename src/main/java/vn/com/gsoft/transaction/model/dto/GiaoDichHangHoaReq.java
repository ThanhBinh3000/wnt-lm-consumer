package vn.com.gsoft.transaction.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.gsoft.transaction.model.system.BaseRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class GiaoDichHangHoaReq extends BaseRequest {
    private Boolean dongBang;
    private String maCoSo;
    private Integer nhomThuocId;
    private Integer nhomDuocLyId;
    private Integer nhomNganhHangId;
    private Integer thuocId;
    private Integer nhomHoatChatId;
    private Integer hangThayTheId;
    private Long[] thuocIds;
    private Integer loaiGiaoDich;
}
