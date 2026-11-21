package Haleon.auth.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService{

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role checkRoleById(int id) {
        Optional<Role> theRole = roleRepository.findById(id);
        if(theRole.isEmpty()){
            throw new RuntimeException("No Role exists of id: "+id);
        }
        return theRole.get();
    }

    @Override
    public void deleteRoleById(int id) {
        roleRepository.delete(checkRoleById(id));
    }

    @Override
    public Role addRole(Role role) {
        role.setId(null);
        return roleRepository.save(role);
    }
}
