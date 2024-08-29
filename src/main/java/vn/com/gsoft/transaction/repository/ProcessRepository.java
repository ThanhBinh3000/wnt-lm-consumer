package vn.com.gsoft.transaction.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.com.gsoft.transaction.entity.Process;
import java.util.Optional;

@Repository
public interface ProcessRepository extends CrudRepository<Process, Long> {
  Optional<Process> findByBatchKey(String batchKey);
}
