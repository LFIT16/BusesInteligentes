package com.lfit.ms_security.Repositories;

import com.lfit.ms_security.Models.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SessionRepository extends MongoRepository<Session, String> {
    Optional<Session> findByToken(String token);
}
