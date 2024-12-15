package neltia.bloguide.api.repository;

import neltia.bloguide.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByIdx(Long idx);

    User findUserByUserId(String userId);
}
