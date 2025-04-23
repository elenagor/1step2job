import "User.cs"

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class Controller {
    List<User> users = new ArrayList();
    // standard constructors

    @GetMapping("/user")
    public User getUserProfile(String resume) {
        return new User();
    }

    @PostMapping("/match")
    void addUser(@RequestBody User user) {
        users.add(user);
    }
}