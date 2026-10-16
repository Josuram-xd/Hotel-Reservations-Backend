package com.hotel.reservations.dto;

import com.hotel.reservations.model.User;

/** Public data of a user: never includes the password hash. */
public record UserResponse(String id, String fullName, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.fullName(), user.email());
    }
}
