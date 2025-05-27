package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.heistom.api.ApiCallException;
import vn.heistom.datasource.UserDataSource;
import vn.heistom.dto.request.AuthenticationRequest;
import vn.heistom.dto.request.UpdateUserRequest;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.model.UserModel;

import java.util.Optional;
import java.util.UUID;

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

    public UserResponse update(UUID userId, UpdateUserRequest request) throws ApiCallException {
        Optional<UserModel> userModelOpt = userDataSource.findByUuid(userId);
        if(userModelOpt.isEmpty()) throw new ApiCallException("Cannot find user with id '" + userId + "'", HttpStatus.NOT_FOUND);
        UserModel userModel = userModelOpt.get();

        if(request.getEmail() != null) userModel.setEmail(request.getEmail());
        if(request.getName() != null) userModel.setName(request.getName());
        if(request.getAvatar() != null) userModel.setAvatar(request.getAvatar());
        if(request.getPhone() != null) userModel.setPhoneNumber(request.getPhone());
        if(request.getAddress() != null) userModel.setAddress(request.getAddress());

        userDataSource.save(userModel);

        return UserResponse.builder()
                .uuid(userModel.getUuid())
                .name(userModel.getName())
                .email(userModel.getEmail())
                .address(userModel.getAddress())
                .avatar(userModel.getAvatar())
                .phoneNumber(userModel.getPhoneNumber())
                .type(userModel.getType())
                .build();
    }
}
