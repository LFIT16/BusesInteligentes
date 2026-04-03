package com.lfit.ms_security.Repositories;

import com.lfit.ms_security.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository  extends MongoRepository<User, String> {

    @Query("{'email': ?0}")
    public User getUserByEmail(String email);
    Optional<User> findByGithubUsername(String githubUsername);
}
