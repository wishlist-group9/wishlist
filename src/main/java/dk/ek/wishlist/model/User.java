package dk.ek.wishlist.model;

// A user of the app. Matches one row in the app_user table

/**
 * A user of the app. Matches one row in the app_user table.
 * Immutable: the fields are set once in the constructor and never change.
 */
public class User {
    private final int id;
    private final String name;
    private final String email;
    private final String passwordHash;


    /**
     * Creates a user.
     *
     * @param id            the id the database gave the user
     * @param name          the user's name
     * @param email         the user's email, unique and lower case
     * @param passwordHash  the BCrypt hash of the password, never the plain password
     */
    public User(Integer id, String name, String email, String passwordHash){
        this.id = id;
        this.name = name;
        this.email = email;

        // Stores the BCrypt hash, never the plain password
        this.passwordHash = passwordHash;
    }

    public Integer getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getEmail() {
        return email;
    }



    public String getPasswordHash() {
        return passwordHash;
    }
    
}
