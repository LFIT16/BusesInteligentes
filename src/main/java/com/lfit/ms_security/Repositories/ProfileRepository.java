package com.lfit.ms_security.Repositories;

import com.lfit.ms_security.Models.Profile;
import com.lfit.ms_security.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    Optional<Profile> findByUser(User user);
    Optional<Profile> findByUser_Id(String userId);

}
