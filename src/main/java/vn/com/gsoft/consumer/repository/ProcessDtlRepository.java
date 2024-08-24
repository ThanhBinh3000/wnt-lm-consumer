package vn.com.gsoft.consumer.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.com.gsoft.consumer.entity.ProcessDtl;

import java.util.Optional;

@Repository
public interface ProcessDtlRepository extends CrudRepository<ProcessDtl, Long> {
    @Query(value = "SELECT pd from ProcessDtl pd " +
            "join Process p on p.id = pd.hdrId " +
            "where p.batchKey=?1 and pd.index=?2", nativeQuery = false)
    Optional<ProcessDtl> findByBatchKeyAndIndex(String batchKey, Integer index);
}
