package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Models.Profile;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Services.ProfileService;
import com.lfit.ms_security.Services.ValidatorsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService theProfileService;

    @Autowired
    private ValidatorsService theValidatorsService;

    @GetMapping("")
    public List<Profile> find() {
        return this.theProfileService.find();
    }

    @GetMapping("{id}")
    public Profile findById(@PathVariable String id) {
        return this.theProfileService.findById(id);
    }

    @PostMapping
    public Profile create(@RequestBody Profile newProfile) {
        return this.theProfileService.create(newProfile);
    }

    @PutMapping("{id}")
    public Profile update(@PathVariable String id, @RequestBody Profile newProfile) {
        return this.theProfileService.update(id, newProfile);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        this.theProfileService.delete(id);
    }

    @GetMapping("/me")
    public ResponseEntity<Profile> myProfile(HttpServletRequest request) {
        User loggedUser = this.theValidatorsService.getUser(request);

        if (loggedUser == null) {
            return ResponseEntity.status(401).build();
        }

        Profile profile = this.theProfileService.findByUserId(loggedUser.getId());

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }

}
