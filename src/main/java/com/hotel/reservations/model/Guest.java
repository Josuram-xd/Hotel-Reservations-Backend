package com.hotel.reservations.model;

/** Person who books and stays at the hotel. Value object. */
public record Guest(String fullName, String email, String phone, String document) {

    public Guest {
        fullName = requireText(fullName, "fullName");
        email = requireText(email, "email");
        phone = requireText(phone, "phone");
        document = requireText(document, "document");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
