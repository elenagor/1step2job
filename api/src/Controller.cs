import "User.cs"

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class Controller {
    List<User> users = new ArrayList();
    // standard constructors

    @GetMapping("/user")
    public String getUserProfile(String resume) {
        return "lol";
    }

    @PostMapping("/match")
    void addUser(@RequestBody User user) {
        users.add(user);
    }
}