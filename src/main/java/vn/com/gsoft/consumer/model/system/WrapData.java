package vn.com.gsoft.consumer.model.system;

import lombok.Data;

import java.util.Date;

@Data
public class WrapData {
    private String batchKey;
    private String code;
    private Date sendDate;
    private Object data;
    private Integer index;
    private Integer total;
}
