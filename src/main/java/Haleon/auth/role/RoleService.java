package Haleon.auth.role;

import java.util.List;

public interface RoleService {
    List<Role> getRoles();
    Role checkRoleById(int id);
    void deleteRoleById(int id);
    Role addRole(Role role);
}
