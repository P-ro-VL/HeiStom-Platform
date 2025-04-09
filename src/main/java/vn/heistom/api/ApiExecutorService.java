package vn.heistom.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ApiExecutorService {

  public <T> ResponseEntity<ApiResponse<T>> execute(HttpServletRequest request, ApiCallExecutor<T> executor)  {
    var response = new ApiResponse<T>();

    HttpStatus status;

    response.requestId = UUID.randomUUID();

    try {
      var result = executor.call();
      status = result.getStatus();
      response.meta = new ApiMeta(result.getStatus().value(), "success");
      response.data = result.getData();
    } catch (ApiCallException ex) {
      status = ex.getHttpStatus();
      response.meta = new ApiMeta(ex.getHttpStatus().value(), ex.getMessage());
      response.data = null;
    }

    return response.toResponseEntity(status);
  }
}