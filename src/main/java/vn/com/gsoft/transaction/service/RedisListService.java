package vn.com.gsoft.transaction.service;

import vn.com.gsoft.transaction.entity.GiaoDichHangHoa;

import java.util.List;

public interface RedisListService {
    void pushTransactionDataRedis(List<GiaoDichHangHoa> giaoDichHangHoas, String key);
    void deleteTransactions(List<GiaoDichHangHoa> data);
}
