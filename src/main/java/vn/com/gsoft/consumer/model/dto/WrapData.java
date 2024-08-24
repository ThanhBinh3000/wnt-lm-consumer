package vn.com.gsoft.consumer.model.dto;

import lombok.Data;
import vn.com.gsoft.consumer.model.system.Profile;

import java.util.Date;

@Data
public class WrapData<T> {
    private String code;
    private Date sendDate;
    private T data;
    private String bathKey;
    private Integer index;
    private Integer total;
    private Profile profile;
}
