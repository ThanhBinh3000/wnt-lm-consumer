package vn.com.gsoft.consumer.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.gsoft.consumer.model.system.BaseRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class HangHoaRep extends BaseRequest {
    private Long id ;
    private Long thuocId;
    private Integer nhomThuocId;
    private Integer nhomDuocLyId;
    private Integer nhomHoatChatId;
    private Integer nhomNganhHangId;
    private Integer hangThayTheId;
}
