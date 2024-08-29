package vn.com.gsoft.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.com.gsoft.transaction.constant.CachingConstant;
import vn.com.gsoft.transaction.entity.GiaoDichHangHoa;
import vn.com.gsoft.transaction.service.RedisListService;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RedisListServiceImpl implements RedisListService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void pushTransactionDataRedis(List<GiaoDichHangHoa> giaoDichHangHoas) {
        giaoDichHangHoas.forEach( x->{
                    saveTransaction(x);
                    pushDataRedis(x);
        } );
    }

    public void deleteTransactions(List<GiaoDichHangHoa> data) {
        data.forEach(x->{
            deleteTransaction(x);
            deleteDataThongKe(x);
        });
    }

    private void saveTransaction(GiaoDichHangHoa data) {
        double timestamp = data.getNgayGiaoDich().getTime() / 1000.0;
        // 1. Lưu thông tin giao dịch vào Redis Hash
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("id", data.getMaPhieuChiTiet());
        transactionData.put("thuocId", data.getThuocId());
        transactionData.put("ngayGiaoDich", data.getNgayGiaoDich().getTime()); // Unix timestamp
        transactionData.put("soLuong", data.getSoLuong());
        transactionData.put("giaNhap", data.getGiaNhap());
        transactionData.put("giaBan", data.getGiaBan());
        transactionData.put("tenDonVi", data.getTenDonVi());
        transactionData.put("maCoSo", data.getMaCoSo());

        redisTemplate.opsForHash().putAll(CachingConstant.GIAO_DICH_HANG_HOA+":" + data.getMaPhieuChiTiet(), transactionData);

        redisTemplate.opsForZSet().add(CachingConstant.GIAO_DICH_HANG_HOA_THEO_NGAY, data.getMaPhieuChiTiet(), timestamp);

        redisTemplate.opsForSet().add(CachingConstant.GIAO_DICH_HANG_HOA_THEO_THUOC_ID + ":" + data.getThuocId(), data.getMaPhieuChiTiet());
    }

    private void pushDataRedis(GiaoDichHangHoa data){
        var timestamp = Double.parseDouble(new SimpleDateFormat("yyyyMMdd").format(data.getNgayGiaoDich()));
        redisTemplate.opsForZSet().add("transactions", data, timestamp);
    }

    private void deleteDataThongKe(GiaoDichHangHoa data){
        redisTemplate.opsForZSet().remove("transactions", data);
    }


    private void deleteTransaction(GiaoDichHangHoa data) {
        redisTemplate.delete(CachingConstant.GIAO_DICH_HANG_HOA + ":" + data.getMaPhieuChiTiet());

        double timestamp = data.getNgayGiaoDich().getTime() / 1000.0;
        redisTemplate.opsForZSet().remove(CachingConstant.GIAO_DICH_HANG_HOA_THEO_NGAY, data.getMaPhieuChiTiet());
        redisTemplate.opsForSet().remove(CachingConstant.GIAO_DICH_HANG_HOA_THEO_THUOC_ID + ":" + data.getThuocId(), data.getMaPhieuChiTiet());
    }
}
