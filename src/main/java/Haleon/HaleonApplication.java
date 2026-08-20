package Haleon;

import Haleon.auth.role.Role;
import Haleon.auth.role.RoleName;
import Haleon.auth.role.RoleService;
import Haleon.auth.user.User;
import Haleon.auth.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@ConfigurationPropertiesScan
@SpringBootApplication
public class HaleonApplication {

	public static void main(String[] args) {
		SpringApplication.run(HaleonApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder){
		return runner ->{
			addRoles(roleService);
			createTempUser(roleService, userService, passwordEncoder);
		};
	}

	private void createTempUser(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder) {
		User user = new User();
		user.setName("prajyot");
		user.setPassword(passwordEncoder.encode("prajyot"));
		user.setEnabled(true);
		user.setPhoneNumber("1234567890");
		user.setDob(LocalDate.now());
		user.setAddress("prajyot");
		user.setEmail("prajyot@gmail.com");
		Role role =  roleService.checkRoleById(1);
		user.setRole(role);
		userService.createUser(user);
		System.out.println("added user-prajyot as a admin");
	}

	private void addRoles(RoleService roleService) {
		Role role = new Role();
		role.setRoleName(RoleName.ROLE_ADMIN);
		roleService.addRole(role);
		role.setRoleName(RoleName.ROLE_CUSTOMER);
		roleService.addRole(role);
		role.setRoleName(RoleName.ROLE_EMPLOYEE);
		roleService.addRole(role);
		System.out.println("Successfully added all the roles "+ roleService.getRoles());
	}


}
