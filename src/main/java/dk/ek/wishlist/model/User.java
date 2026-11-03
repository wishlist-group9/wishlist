package dk.ek.wishlist.model;

/** A user who can log in and own wishlists. Matches one row in the app_user table. */
public class User {
    // Attributes
    private Integer id;
    private String name;
    private String email;
    private String passwordHash;

    // Empty constructor
    public User() {}

    // Constructor
    public User(Integer id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // No toString() that can leak the passwordHash by accident
}
