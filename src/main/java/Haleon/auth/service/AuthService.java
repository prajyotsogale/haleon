package Haleon.auth.service;

import Haleon.auth.dto.LoginDto;
import Haleon.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginDto loginDto);
}
