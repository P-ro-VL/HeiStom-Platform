package vn.heistom.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

@Getter
public class ApiCallResult<T> {
  HttpStatus status;
  T data;

  public ApiCallResult(final T data, final HttpStatus status) {
    this.status = status;
    this.data = data;
  }

  public ApiCallResult(final T data) {
    if (Objects.isNull(data)) {
      this.status = HttpStatus.NOT_FOUND;
    } else {
      this.status = HttpStatus.OK;
    }

    this.data = data;
  }
}