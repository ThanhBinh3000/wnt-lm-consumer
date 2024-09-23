package vn.com.gsoft.transaction.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.gsoft.transaction.entity.UserProfile;
import vn.com.gsoft.transaction.model.dto.UserProfileReq;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends BaseRepository<UserProfile, UserProfileReq, Long> {
    @Query("SELECT c FROM UserProfile c " +
            " WHERE 1=1 "
            + " ORDER BY c.id desc"
    )
    Page<UserProfile> searchPage(@Param("param") UserProfileReq param, Pageable pageable);


    @Query("SELECT c FROM UserProfile c " +
            " WHERE 1=1 "
            + " ORDER BY c.id desc"
    )
    List<UserProfile> searchList(@Param("param") UserProfileReq param);

    Optional<UserProfile> findByUserId(Integer userId);
}
