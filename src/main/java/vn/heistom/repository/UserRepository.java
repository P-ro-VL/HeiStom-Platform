package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.heistom.api.ApiCallException;
import vn.heistom.datasource.UserDataSource;
import vn.heistom.dto.request.AuthenticationRequest;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.model.UserModel;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserRepository {

    final UserDataSource userDataSource;

    public UserResponse authenticate(AuthenticationRequest authenticationRequest) throws ApiCallException {
        Optional<UserModel> userOpt = userDataSource.findByEmailAndPassword(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        if(userOpt.isEmpty()) throw new ApiCallException("Authorization failed", HttpStatus.UNAUTHORIZED);

        UserModel user = userOpt.get();
        return UserResponse.builder()
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .phoneNumber(user.getPhoneNumber())
                .type(user.getType())
                .build();
    }

}
