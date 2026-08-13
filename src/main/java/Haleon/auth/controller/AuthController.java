package Haleon.auth.controller;

import Haleon.auth.role.Role;
import Haleon.auth.role.RoleName;
import Haleon.auth.role.RoleService;
import Haleon.auth.role.RoleServiceImpl;
import Haleon.auth.user.User;
import Haleon.auth.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

//    temp
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

//    temp end


    @Autowired
    public AuthController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @GetMapping("/createTempUser")
    public User createTempUser(){
        User user = new User();
        user.setName("prajyot");
        user.setPassword(passwordEncoder.encode("prajyot"));
        user.setEnabled(true);
        user.setPhoneNumber("123123123");
        user.setDob(LocalDate.now());
        user.setAddress("prajyot");
        RoleName roleName = RoleName.ROLE_CUSTOMER;
        Role role =  roleService.addRole(1, roleName);
        user.setRole(role);
        return user;
    }

}
