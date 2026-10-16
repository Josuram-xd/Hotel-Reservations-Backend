package com.hotel.reservations.repository;

import com.hotel.reservations.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void storesAUserAndFindsItByEmailAndId() {
        User ada = User.create("Ada Lovelace", "ada@example.com", "hash");

        assertThat(repository.saveIfEmailFree(ada)).isTrue();

        assertThat(repository.findByEmail("ada@example.com")).contains(ada);
        assertThat(repository.findById(ada.id())).contains(ada);
    }

    @Test
    void rejectsADuplicateEmailIgnoringCaseAndSpaces() {
        repository.saveIfEmailFree(User.create("Ada", "ada@example.com", "hash"));

        boolean stored = repository.saveIfEmailFree(User.create("Other Ada", "  ADA@Example.com ", "hash2"));

        assertThat(stored).isFalse();
        assertThat(repository.findByEmail("Ada@example.com")).get()
                .extracting(User::fullName).isEqualTo("Ada");
    }

    @Test
    void returnsEmptyForUnknownUsers() {
        assertThat(repository.findByEmail("nobody@example.com")).isEmpty();
        assertThat(repository.findById("missing")).isEmpty();
    }

    @Test
    void createNormalizesTheEmailAndGeneratesAnId() {
        User user = User.create("  Ada  ", "  ADA@Example.COM ", "hash");

        assertThat(user.email()).isEqualTo("ada@example.com");
        assertThat(user.fullName()).isEqualTo("Ada");
        assertThat(user.id()).isNotBlank();
    }

    @Test
    void deleteAllEmptiesTheStore() {
        repository.saveIfEmailFree(User.create("Ada", "ada@example.com", "hash"));

        repository.deleteAll();

        assertThat(repository.findByEmail("ada@example.com")).isEmpty();
    }
}
