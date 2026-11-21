package Haleon.auth.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(int id) {
        Optional<User> theUser = userRepository.findById(id);
        if(theUser.isEmpty()){
            throw new RuntimeException("No User exists of id: "+id);
        }
        return theUser.get();
    }

    @Override
    public User createUser(User user) {
        user.setId(null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCifNumber("CIF" + "0001");
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        User theUser = getUserById(user.getId());
        user.setCreatedAt(theUser.getCreatedAt());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        Optional<User> theUser = userRepository.findByEmail(email);
        if(theUser.isEmpty()){
            throw new RuntimeException("No User exists of email: "+email);
        }
        return theUser.get();
    }
}
