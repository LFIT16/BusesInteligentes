package com.lfit.ms_security.Services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.lfit.ms_security.Models.DTOs.RegisterRequest;
import com.lfit.ms_security.Models.Profile;
import com.lfit.ms_security.Models.Session;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Repositories.ProfileRepository;
import com.lfit.ms_security.Repositories.SessionRepository;
import com.lfit.ms_security.Repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private ProfileRepository theProfileRepository;

    @Autowired
    private SessionRepository theSessionRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @Autowired
    private EmailService theEmailService;

    public List<User> find() {
        return this.theUserRepository.findAll();
    }

    public User findById(String id) {
        return this.theUserRepository.findById(id).orElse(null);
    }

    public User create(User newUser) {
        newUser.setPassword(theEncryptionService.convertSHA256(newUser.getPassword()));
        return this.theUserRepository.save(newUser);
    }

    public User update(String id, User newUser) {
        User actualUser = this.theUserRepository.findById(id).orElse(null);
        if (actualUser != null) {
            actualUser.setName(newUser.getName());
            actualUser.setEmail(newUser.getEmail());
            actualUser.setPassword(theEncryptionService.convertSHA256(newUser.getPassword()));
            this.theUserRepository.save(actualUser);
            return actualUser;
        }
        return null;
    }

    public void delete(String id) {
        User theUser = this.theUserRepository.findById(id).orElse(null);
        if (theUser != null) {
            this.theUserRepository.delete(theUser);
        }
    }

    public boolean addProfile(String userId, String profileId) {
        User theUser = this.theUserRepository.findById(userId).orElse(null);
        Profile theProfile = this.theProfileRepository.findById(profileId).orElse(null);
        if (theUser != null && theProfile != null) {
            theProfile.setUser(theUser);
            this.theProfileRepository.save(theProfile);
            return true;
        }
        return false;
    }

    public boolean removeProfile(String userId, String profileId) {
        User theUser = this.theUserRepository.findById(userId).orElse(null);
        Profile theProfile = this.theProfileRepository.findById(profileId).orElse(null);
        if (theUser != null && theProfile != null) {
            theProfile.setUser(null);
            this.theProfileRepository.save(theProfile);
            return true;
        }
        return false;
    }

    public boolean unlinkGithub(String userId) {
        User user = theUserRepository.findById(userId).orElse(null);
        if (user == null) return false;
        user.setGithubUsername(null);
        theUserRepository.save(user);
        return true;
    }

    public boolean addSession(String userId, String sessionId) {
        User theUser = this.theUserRepository.findById(userId).orElse(null);
        Session theSession = this.theSessionRepository.findById(sessionId).orElse(null);
        if (theUser != null && theSession != null) {
            theSession.setUser(theUser);
            this.theSessionRepository.save(theSession);
            return true;
        }
        return false;
    }

    public boolean removeSession(String userId, String sessionId) {
        User theUser = this.theUserRepository.findById(userId).orElse(null);
        Session theSession = this.theSessionRepository.findById(sessionId).orElse(null);
        if (theUser != null && theSession != null) {
            theSession.setUser(null);
            this.theSessionRepository.save(theSession);
            return true;
        }
        return false;
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Las contraseñas no coinciden"));
        }

        User existing = theUserRepository.getUserByEmail(request.getEmail());
        if (existing != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El email ya está registrado"));
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(theEncryptionService.encodeBCrypt(request.getPassword()));
        newUser.setEmailConfirmed(false);

        User savedUser = theUserRepository.save(newUser);

        theEmailService.sendConfirmationEmail(savedUser.getEmail(), savedUser.getName());

        // Crear respuesta sin contraseña
        Map<String, Object> userResponse = Map.of(
                "id", savedUser.getId(),
                "name", savedUser.getName(),
                "lastName", savedUser.getLastName(),
                "email", savedUser.getEmail(),
                "emailConfirmed", savedUser.isEmailConfirmed()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}