package vn.com.gsoft.transaction.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.gsoft.transaction.entity.GiaoDichHangHoa;
import vn.com.gsoft.transaction.model.dto.GiaoDichHangHoaReq;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiaoDichHangHoaRepository extends BaseRepository<GiaoDichHangHoa, GiaoDichHangHoaReq, Long> {
    @Query(value = "SELECT * FROM GiaoDichHangHoa c "
            + "WHERE 1=1 "
            + " AND ((:#{#param.loaiGiaoDich} IS NULL) OR (c.LoaiGiaoDich = :#{#param.loaiGiaoDich})) "
            //+ " AND (:#{#param.fromDate} IS NULL OR c.NgayGiaoDich <= :#{#param.fromDate})"
            //+ " AND (:#{#param.toDate} IS NULL OR c.NgayGiaoDich <= :#{#param.toDate})"
            //+ " AND ((:#{#param.dongBang} IS NULL) OR (c.DongBang = :#{#param.dongBang})) "
            + " ORDER BY c.ThuocId", nativeQuery = true
    )
    Page<GiaoDichHangHoa> searchPage(@Param("param") GiaoDichHangHoaReq param, Pageable pageable);

    @Query(value = "SELECT * FROM GiaoDichHangHoa c " +
            "WHERE 1=1 "
            + " AND ((:#{#param.loaiGiaoDich} IS NULL) OR (c.LoaiGiaoDich = :#{#param.loaiGiaoDich})) "
            + " AND (:#{#param.fromDate} IS NULL OR c.NgayGiaoDich >= :#{#param.fromDate})"
            + " AND (:#{#param.toDate} IS NULL OR c.NgayGiaoDich <= :#{#param.toDate})"
            + " AND ((:#{#param.dongBang} IS NULL) OR (c.DongBang = :#{#param.dongBang})) "
            + " AND ((:#{#param.nhomDuocLyId} IS NULL) OR (c.nhomDuocLyId = :#{#param.nhomDuocLyId})) "
            + " ORDER BY c.NgayGiaoDich desc", nativeQuery = true
    )
    List<GiaoDichHangHoa> searchList(@Param("param") GiaoDichHangHoaReq param);

    GiaoDichHangHoa findAllByMaPhieuChiTietAndLoaiGiaoDich(Integer maPhieuChiTiet, Integer loaiGiaoDich);
    @Query(value = "DECLARE @query nvarchar(1024) =:query " +
            "exec sp_executesql @query"
            , nativeQuery = true
    )
    void updateData(@Param("query") String query);

    @Query(value = "DECLARE @query nvarchar(1024) =:query " +
            "exec sp_executesql @query"
            , nativeQuery = true
    )
    List<Tuple> finByThuocId(@Param("query") String query);
    //endregion

    //check table có tồn tại không
    @Query(value = "select c.name from LMNTDB.sys.tables c where" +
            " 1=1 AND" +
            " c.name = :tableName", nativeQuery = true)
    Optional<Tuple> checkTableExit(@Param("tableName") String tableName);

    @Query(value = "DECLARE @query nvarchar(1024) =:query " +
            "exec sp_executesql @query"
            , nativeQuery = true
    )
    void createTable(@Param("query") String query);
}
