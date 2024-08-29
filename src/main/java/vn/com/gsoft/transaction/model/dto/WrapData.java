package vn.com.gsoft.transaction.model.dto;

import lombok.Data;
import vn.com.gsoft.transaction.model.system.Profile;

@Data
public class WrapData<T> {
    private String code;
    private String sendDate;
    private T data;
    private String bathKey;
    private Integer index;
    private Integer total;
    private Profile profile;
}
