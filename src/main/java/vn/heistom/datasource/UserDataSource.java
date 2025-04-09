package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.UserModel;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDataSource extends JpaRepository<UserModel, String> {
    
    Optional<UserModel> findByEmailAndPassword(String email, String password);
    
    Optional<UserModel> findByUuid(UUID uuid);
    
}
