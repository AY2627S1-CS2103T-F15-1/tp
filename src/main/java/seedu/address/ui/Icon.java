package seedu.address.ui;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

/**
 * The small pictures on the buttons of the panels. Each is a vector shape on a 16 by 16 grid, drawn in the colour
 * of the text, so that the application needs no image files and the pictures stay sharp at any size.
 */
public enum Icon {
    /** A pencil, for changing a record. */
    EDIT("M11.5 1.5 L14.5 4.5 L5.5 13.5 L1.5 14.5 L2.5 10.5 Z"),
    /** A bin, for deleting a record. */
    DELETE("M5 1.5 H11 V3 H14.5 V4.5 H1.5 V3 H5 Z M3 6 H13 L12.3 14.5 H3.7 Z"),
    /** A box with a lid, for putting a student away. */
    ARCHIVE("M1.5 2 H14.5 V5 H1.5 Z M2.5 6 H13.5 V14.5 H2.5 Z M6 8 H10 V9.5 H6 Z"),
    /** An arrow out of a tray, for bringing a student back. */
    RESTORE("M8 1.5 L12.5 6.5 H9.5 V11 H6.5 V6.5 H3.5 Z M2 12.5 H14 V14.5 H2 Z"),
    /** A cross, for cancelling a lesson. */
    CANCEL("M3 4.5 L4.5 3 L8 6.5 L11.5 3 L13 4.5 L9.5 8 L13 11.5 L11.5 13 L8 9.5 L4.5 13 L3 11.5 L6.5 8 Z");

    public static final String STYLE_CLASS = "icon-shape";

    private final String path;

    Icon(String path) {
        this.path = path;
    }

    /** Returns a new shape of this icon, which has the style class {@value #STYLE_CLASS}. */
    public SVGPath shape() {
        SVGPath shape = new SVGPath();
        shape.setContent(path);
        shape.setFillRule(FillRule.EVEN_ODD);
        shape.getStyleClass().add(STYLE_CLASS);
        return shape;
    }
}
