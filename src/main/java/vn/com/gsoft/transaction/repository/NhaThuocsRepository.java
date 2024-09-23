package vn.com.gsoft.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.gsoft.transaction.entity.NhaThuocs;
import vn.com.gsoft.transaction.model.dto.NhaThuocsReq;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhaThuocsRepository extends BaseRepository<NhaThuocs, NhaThuocsReq, Long> {
    @Query(value = "SELECT * FROM NhaThuocs c " +
            "WHERE 1=1 "
            , nativeQuery = true
    )
    Page<NhaThuocs> searchPage(@Param("param") NhaThuocsReq param, Pageable pageable);

    @Query("SELECT c FROM NhaThuocs c " +
            "WHERE 1=1 "
    )
    List<NhaThuocs> searchList(@Param("param") NhaThuocsReq param);

    NhaThuocs findByMaNhaThuoc(String maNhaThuoc);
}
