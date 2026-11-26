package com.thalesgroup.d1.templates.core.ui.base;

/** Display-only formatting; never exposes any additional card data. */
public final class CardPresentation {
    private CardPresentation() { }

    public static String pan(String value) {
        if (value == null || value.isEmpty()) {
            return "•••• •••• •••• ••••";
        }
        String compact = value.replace(" ", "").replace('*', '•');
        return compact.replaceAll("(.{4})(?=.)", "$1 ");
    }

    public static String expiry(String value) {
        if (value == null || value.isEmpty()) {
            return "MM/YY";
        }
        String compact = value.replace("/", "").replace(" ", "");
        if (compact.matches("\\d{3}")) {
            compact = "0" + compact;
        }
        if (compact.matches("\\d{4}")) {
            return compact.substring(0, 2) + "/" + compact.substring(2);
        }
        return value;
    }
}
