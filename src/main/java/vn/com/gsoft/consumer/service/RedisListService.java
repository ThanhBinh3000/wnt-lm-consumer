package vn.com.gsoft.consumer.service;

import vn.com.gsoft.consumer.entity.GiaoDichHangHoa;
import vn.com.gsoft.consumer.model.dto.GiaoDichHangHoaReq;

import java.util.List;

public interface RedisListService {
    void pushTransactionDataRedis(List<GiaoDichHangHoa> giaoDichHangHoas);
    void deleteTransactions(List<GiaoDichHangHoa> data);
}
