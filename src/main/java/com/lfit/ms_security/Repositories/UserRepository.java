package com.lfit.ms_security.Repositories;

import com.lfit.ms_security.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository  extends MongoRepository<User, String> {

    boolean existsByEmail(String email);

    @Query("{'email': ?0}")
    public User getUserByEmail(String email);
    @Query("{ $or: [ "
            + "{ 'name': { $regex: ?0, $options: 'i' } }, "
            + "{ 'email': { $regex: ?0, $options: 'i' } } "
            + "] }")
    List<User> searchByNameOrEmail(String query);

    Optional<User> findByGithubUsername(String githubUsername);


}
