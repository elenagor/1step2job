@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String email;

    private String resume;
    
    // standard constructors / setters / getters / toString
}