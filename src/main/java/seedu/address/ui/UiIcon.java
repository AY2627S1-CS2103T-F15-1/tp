package seedu.address.ui;

import javafx.scene.shape.SVGPath;

/** Small monochrome icons shared by the command-assist controls. */
final class UiIcon {
    private static final String PENCIL =
            "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41"
                    + "l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private static final String ARCHIVE =
            "M20.54 5.23l-1.39-1.68A1.5 1.5 0 0 0 18 3H6c-.47 0-.88.21-1.16.55L3.46 5.23"
                    + "A2 2 0 0 0 3 6.5V19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6.5c0-.5-.17-.96-.46-1.27z"
                    + "M12 17.5 6.5 12H10v-2h4v2h3.5L12 17.5zM5.12 5l.81-1h12l.94 1H5.12z";
    private static final String TRASH =
            "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    private static final String PLUS = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z";

    private UiIcon() {
    }

    static SVGPath pencil() {
        return icon(PENCIL);
    }

    static SVGPath archive() {
        return icon(ARCHIVE);
    }

    static SVGPath trash() {
        return icon(TRASH);
    }

    static SVGPath plus() {
        return icon(PLUS);
    }

    private static SVGPath icon(String content) {
        SVGPath icon = new SVGPath();
        icon.setContent(content);
        icon.setScaleX(0.65);
        icon.setScaleY(0.65);
        icon.getStyleClass().add("action-icon");
        return icon;
    }
}
