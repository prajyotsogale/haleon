package Haleon.auth.controller;

import Haleon.auth.dto.LoginDto;
import Haleon.auth.service.AuthService;
import Haleon.auth.user.User;
import Haleon.auth.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto loginDto){
        String message = authService.login(loginDto.getEmail(), loginDto.getPassword());
        return message;
    }


}
