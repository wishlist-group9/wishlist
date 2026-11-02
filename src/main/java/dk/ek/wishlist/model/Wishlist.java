package dk.ek.wishlist.model;

/** A wishlist (ønskeseddel) owned by one user. Matches one row in the wishlist table. */
public class Wishlist {
    private final int id;
    private final int userId;
    private final String title;
    private final String shareToken;

    public Wishlist(int id, int userId, String title, String shareToken) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.shareToken = shareToken;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getShareToken() {
        return shareToken;
    }
}
