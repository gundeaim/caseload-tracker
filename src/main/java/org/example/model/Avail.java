package org.example.model;

public enum Avail {
    AM,
    MID,
    PM,
    AM_MID,
    AM_PM,
    MID_PM,
    AM_MID_PM;


    // Helper to convert strings like "AM/MID" to Avail enum
    public static Avail fromString(String input) {
        if (input == null || input.isEmpty()) return null;
        String normalized = input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();


        switch (normalized) {
            case "AM": return Avail.AM;
            case "MID": return Avail.MID;
            case "PM": return Avail.PM;
            case "AMMID": return Avail.AM_MID;
            case "AMPM": return Avail.AM_PM;
            case "MIDPM": return Avail.MID_PM;
            case "AMMIDPM": return Avail.AM_MID_PM;
            default: return null;
        }
    }
}
