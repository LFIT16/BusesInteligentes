package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Services.SecurityService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;

@CrossOrigin
@RestController
@RequestMapping("/security")
public class SecurityController {

    @Autowired
    private SecurityService theSecurityService;

    @PostMapping("login")
    public HashMap<String,Object> login(@RequestBody User theNewUser,
                                        final HttpServletResponse response)throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        String token = this.theSecurityService.login(theNewUser);

        if (token != null) {
            User user = this.theSecurityService.getUserByEmail(theNewUser.getEmail());
            theResponse.put("token", token);
            theResponse.put("user", user);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Email o contraseña incorrectos");
        }
        return theResponse;
    }
}