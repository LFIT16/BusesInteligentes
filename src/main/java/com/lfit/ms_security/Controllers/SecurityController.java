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

    // LOGIN
    @PostMapping("login")
    public HashMap<String, Object> login(@RequestBody User user,
                                         HttpServletResponse response) throws IOException {

        HashMap<String, Object> result = theSecurityService.startTwoFactorLogin(user);

        if (result == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Email o contraseña incorrectos");
            return null;
        }

        return result;
    }

    // VERIFY 2FA
    @PostMapping("2fa/verify")
    public HashMap<String, Object> verify2FA(@RequestBody HashMap<String, String> body,
                                             HttpServletResponse response) {

        HashMap<String, Object> result =
                theSecurityService.verifyTwoFactorCode(
                        body.get("challengeId"),
                        body.get("code")
                );

        if (Boolean.FALSE.equals(result.get("success"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return result;
    }

    // RESEND 2FA
    @PostMapping("2fa/resend")
    public HashMap<String, Object> resend2FA(@RequestBody HashMap<String, String> body,
                                             HttpServletResponse response) {

        HashMap<String, Object> result =
                theSecurityService.resendTwoFactorCode(body.get("challengeId"));

        if (Boolean.FALSE.equals(result.get("success"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return result;
    }

    // CANCEL 2FA
    @PostMapping("2fa/cancel")
    public void cancel2FA(@RequestBody HashMap<String, String> body) {
        theSecurityService.cancelTwoFactorSession(body.get("challengeId"));
    }

    // REGISTER (IMPORTANTE)
    @PostMapping("register")
    public HashMap<String, Object> register(@RequestBody User user,
                                            HttpServletResponse response) {

        HashMap<String, Object> res = new HashMap<>();

        try {
            theSecurityService.register(user);

            response.setStatus(HttpServletResponse.SC_CREATED);
            res.put("success", true);
            res.put("message", "Usuario registrado correctamente");
            return res;

        } catch (RuntimeException e) {

            if ("EMAIL_ALREADY_EXISTS".equals(e.getMessage())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                res.put("success", false);
                res.put("message", "El correo ya está registrado.");
                return res;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.put("success", false);
            res.put("message", "Error en el registro");
            return res;
        }
    }
}