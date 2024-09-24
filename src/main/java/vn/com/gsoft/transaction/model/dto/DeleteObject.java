package vn.com.gsoft.transaction.model.dto;

import lombok.Data;

@Data
public class DeleteObject {
    private Integer type ;
    private Integer[] ids;
}
