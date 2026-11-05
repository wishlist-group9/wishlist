package dk.ek.wishlist.model;

/**
 * Represents a wish belonging to a specific wishlist
 */
public class Wish {

    private int id;
    private int wishlistId;
    private String title;
    private String description;
    private String link;
    private Double price;

    /**
     * Creates an empty wish
     * Values can be added later using setters
     */
    public Wish(){
    }

    /**
     * Creates a wish with the values
     *
     * @param id the unique wish ID
     * @param wishlistId the ID of the wishlist which this wish belongs to
     * @param title the title of the wish
     * @param description the description, or null if not provided
     * @param link the link to the product, or null if not provided
     * @param price the price, or null if not provided
     * Double allows the price to be null
     */
    public Wish(int id, int wishlistId, String title, String description, String link, Double price){
        this.id = id;
        this.wishlistId = wishlistId;
        this.title = title;
        this.description = description;
        this.link = link;
        this.price = price;
    }

    /*
     * Getters and setters to provide access to the wish's private fields
     * Getters return values, and setters update them
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}