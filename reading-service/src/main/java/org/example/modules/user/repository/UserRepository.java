package org.example.modules.user.repository;

import org.example.modules.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);
}
