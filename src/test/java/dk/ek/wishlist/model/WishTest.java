package dk.ek.wishlist.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests how a wish's reservation looks to different users. */
class WishTest {

    @Test
    void statusFor_notReserved_isOpen() {
        Wish wish = new Wish(1, 5, "Bog", null, null, null);

        assertEquals("open", wish.statusFor(2));
    }

    @Test
    void statusFor_reservedByViewer_isMine() {
        Wish wish = new Wish(1, 5, "Bog", null, null, null);
        wish.setReservedBy(2);

        assertEquals("mine", wish.statusFor(2));
    }

    @Test
    void statusFor_reservedBySomeoneElse_isTaken() {
        Wish wish = new Wish(1, 5, "Bog", null, null, null);
        wish.setReservedBy(3);

        assertEquals("taken", wish.statusFor(2));
    }
}