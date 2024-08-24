package vn.com.gsoft.consumer.entity;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.gsoft.consumer.entity.HangHoa;
import vn.com.gsoft.consumer.model.dto.HangHoaRep;
import vn.com.gsoft.consumer.repository.BaseRepository;

import java.util.List;

@Repository
public interface HangHoaRepository extends BaseRepository<HangHoa, HangHoaRep, Long> {
    @Query(value = "SELECT * FROM HangHoa c "
            + "WHERE 1=1 "
            + " AND ((:#{#param.thuocId} IS NULL) OR (c.ThuocId = :#{#param.thuocId}))"
            + " AND (:#{#param.nhomThuocId} IS NULL OR c.NhomThuocId = :#{#param.nhomThuocId})"
            + " AND (:#{#param.nhomDuocLyId} IS NULL OR c.NhomDuocLyId = :#{#param.nhomDuocLyId})"
            + " AND (:#{#param.nhomHoatChatId} IS NULL OR c.NhomHoatChatId = :#{#param.nhomHoatChatId})"
            + " AND (:#{#param.nhomNganhHangId} IS NULL OR c.NhomNganhHangId = :#{#param.nhomNganhHangId})"
            + " AND (:#{#param.hangThayTheId} IS NULL OR c.NhomHoatChatId in (SELECT c1.NhomHoatChatId FROM HANGHOA c1 WHERE c1.ThuocId = :#{#param.hangThayTheId}))"
            + " ORDER BY c.TenThuoc", nativeQuery = true
    )
    Page<HangHoa> searchPage(@Param("param") HangHoaRep param, Pageable pageable);

    @Query(value = "SELECT * FROM HangHoa c "
            + "WHERE 1=1 "
            + " AND ((:#{#param.thuocId} IS NULL) OR (c.ThuocId = :#{#param.thuocId}))"
            + " AND (:#{#param.nhomThuocId} IS NULL OR c.NhomThuocId = :#{#param.nhomThuocId})"
            + " AND (:#{#param.nhomDuocLyId} IS NULL OR c.NhomDuocLyId = :#{#param.nhomDuocLyId})"
            + " AND (:#{#param.nhomHoatChatId} IS NULL OR c.NhomHoatChatId = :#{#param.nhomHoatChatId})"
            + " AND (:#{#param.nhomNganhHangId} IS NULL OR c.NhomNganhHangId = :#{#param.nhomNganhHangId})"
            + " ORDER BY c.TenThuoc", nativeQuery = true
    )
    List<HangHoa> searchList(@Param("param") HangHoaRep param);

    @Query(value = "SELECT c.ThuocId as thuocId," +
            " c.TenThuoc AS tenThuoc," +
            " c.TenDonVi AS tenDonVi," +
            " c.tenNhomThuoc AS tenNhomThuoc  FROM HangHoa c "
            + "WHERE 1=1 ", nativeQuery = true
    )
    List<Tuple> searchListHangHoa();

    HangHoa findByThuocId(long ThuocId);
}
