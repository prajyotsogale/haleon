package Haleon.auth.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    private ObjectMapper objectMapper;

    @Autowired
    public UserController(UserService userService, ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @GetMapping("")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id){
        return userService.getUserById(id);
    }

    @PostMapping("")
    public User saveUser(@RequestBody User user){
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @RequestBody User user){
        user.setId(id);
        return userService.updateUser(user);
    }

    @PatchMapping("/{id}")
    public User partialUpdateUser(@PathVariable int id, @RequestBody Map<String, Object> patchPayload){
        User user = userService.getUserById(id);
        if(patchPayload.containsKey("id")){
            throw new RuntimeException("User id is not allowed in request body: "+id);
        }
        User updatedUser = applyPatchToUser(user, patchPayload);
        return userService.updateUser(updatedUser);
    }

    public User applyPatchToUser(User user, @RequestBody Map<String, Object> patchPayload){
        ObjectNode userNode = objectMapper.convertValue(user, ObjectNode.class);
        ObjectNode patchNode = objectMapper.convertValue(patchPayload, ObjectNode.class);
        userNode.setAll(patchNode);
        return objectMapper.convertValue(userNode, User.class);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable int id){
        userService.deleteUserById(id);
    }

}
