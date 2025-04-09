package vn.heistom.api;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  ApiMeta meta;
  T data;
  UUID requestId;

  public ResponseEntity<ApiResponse<T>> toResponseEntity(HttpStatus status) {
      return new ResponseEntity<>(this, status);
  }
}