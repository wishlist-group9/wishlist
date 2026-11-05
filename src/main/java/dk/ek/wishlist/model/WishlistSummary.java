package dk.ek.wishlist.model;

/**
 * A wishlist as shown on the overview page: its title, who owns it and how many wishes it has.
 * Read-only; built by {@code WishlistRepository} from one SQL query.
 */
public class WishlistSummary {
    private final int id;
    private final String title;
    private final String ownerName;
    private final int wishCount;

    public WishlistSummary(int id, String title, String ownerName, int wishCount) {
        this.id = id;
        this.title = title;
        this.ownerName = ownerName;
        this.wishCount = wishCount;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getWishCount() {
        return wishCount;
    }
}
