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
@RequestMapping("/api/public/security")
public class SecurityController {

    @Autowired
    private SecurityService theSecurityService;

    @PostMapping("login")
    public HashMap<String, Object> login(@RequestBody User theNewUser,
                                         final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = this.theSecurityService.startTwoFactorLogin(theNewUser);

        if (theResponse == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Email o contraseña incorrectos");
            return null;
        }

        return theResponse;
    }
    @PostMapping("2fa/verify")
    public HashMap<String, Object> verify2FA(@RequestBody HashMap<String, String> body,
                                             final HttpServletResponse response) throws IOException {
        HashMap<String, Object> result = this.theSecurityService.verifyTwoFactorCode(
                body.get("challengeId"),
                body.get("code")
        );

        Boolean success = (Boolean) result.get("success");
        if (Boolean.FALSE.equals(success)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return result;
    }
    @PostMapping("2fa/resend")
    public HashMap<String, Object> resend2FA(@RequestBody HashMap<String, String> body,
                                             final HttpServletResponse response) throws IOException {
        HashMap<String, Object> result = this.theSecurityService.resendTwoFactorCode(body.get("challengeId"));

        Boolean success = (Boolean) result.get("success");
        if (Boolean.FALSE.equals(success)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return result;
    }
    @PostMapping("2fa/cancel")
    public void cancel2FA(@RequestBody HashMap<String, String> body) {
        this.theSecurityService.cancelTwoFactorSession(body.get("challengeId"));
    }
    @PostMapping("register")
    public HashMap<String, Object> register(@RequestBody User theNewUser,
                                            final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();

        String result = this.theSecurityService.register(theNewUser);

        if ("Usuario registrado correctamente".equals(result)) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            theResponse.put("success", true);
            theResponse.put("message", result);
            return theResponse;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        theResponse.put("success", false);
        theResponse.put("message", result);
        return theResponse;
    }

}