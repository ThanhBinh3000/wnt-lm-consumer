package vn.com.gsoft.consumer.service;

import vn.com.gsoft.consumer.model.system.Profile;

import java.util.Optional;

public interface UserService {
    Optional<Profile> findUserByToken(String token);
}
