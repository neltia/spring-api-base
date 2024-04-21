package neltia.bloguide.api.repository;

import neltia.bloguide.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByIdx(int idx);

    User findUserByUserId(String userId);
}
