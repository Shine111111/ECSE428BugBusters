package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// users table: user_id is the primary key and email is unique (stored lowercased, see User)
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // highest user_id as a number (user IDs are zero-padded digits, e.g. "0001"); null when there are no users
    @Query("select max(cast(u.userId as Integer)) from User u")
    Integer findMaxNumericUserId();
}
