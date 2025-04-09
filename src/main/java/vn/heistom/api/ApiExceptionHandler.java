package vn.heistom.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.MimeType;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@AllArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ApiCallException.class)
    public ResponseEntity<?> handleApiException(ApiCallException e) {
        return responseEntity(e.getHttpStatus().value(), e.getHttpStatus(), e.getMessage(), e);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        String msg = ex.getConstraintViolations().stream()
                .map(constraintViolation -> String.format("%s:%s:%s", constraintViolation.getRootBeanClass().getSimpleName(), constraintViolation.getPropertyPath(), constraintViolation.getMessage()))
                .collect(Collectors.joining(","));
        return responseEntity(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, msg, ex);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        return responseEntity(ErrorCode.INTERNAL_ERR, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String supportedMethods = ex.getSupportedMethods() == null ? null : String.join(",", ex.getSupportedMethods());

        String msg = String.format("Method not supported: %s, only support %s", ex.getMethod(), supportedMethods);

        return responseEntity(ErrorCode.METHOD_NOT_ALLOWED, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String supportedContentTypes = ex.getSupportedMediaTypes().stream().map(MimeType::toString)
                .collect(Collectors.joining(", "));

        String msg = String.format("MediaType not supported: %s, only support %s", ex.getContentType(),
                supportedContentTypes);

        return responseEntity(ErrorCode.UNSUPPORTED_MEDIA_TYPE, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "406", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String supportedContentTypes = ex.getSupportedMediaTypes().stream().map(MimeType::toString)
                .collect(Collectors.joining(", "));

        String msg = String.format("MediaType not acceptable: only support %s", supportedContentTypes);

        return responseEntity(ErrorCode.NOT_ACCEPTABLE, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ex.printStackTrace();

        String msg = String.format("MissingPathVariable: variable name %s, parameter %s", ex.getVariableName(),
                ex.getParameter().getParameterName());

        return responseEntity(ErrorCode.INTERNAL_ERR, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = String.format("MissingServletRequestParameter: parameter name %s", ex.getParameterName());
        return responseEntity(ErrorCode.BAD_REQUEST, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = String.format("MissingServletRequestPart: request part name %s", ex.getRequestPartName());
        return responseEntity(ErrorCode.BAD_REQUEST, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = String.format("ServletRequestBinding: detail message code %s", ex.getDetailMessageCode());
        return responseEntity(ErrorCode.BAD_REQUEST, status, msg, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String fieldErrors = ex.getFieldErrors().stream()
                .map(fieldError -> String.format("%s:%s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining(","));

        String glObjectErrors = ex.getGlobalErrors().stream().map(ObjectError::getObjectName)
                .collect(Collectors.joining(","));

        String msg = String.format("MethodArgumentNotValid field errors: %s, global errors: %s", fieldErrors,
                glObjectErrors);

        return responseEntity(ErrorCode.BAD_REQUEST, status, msg, ex);
    }

    // @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String msg = String.format("NoHandlerFound: method %s, url %s", ex.getHttpMethod(), ex.getRequestURL());
        return responseEntity(ErrorCode.NOT_FOUND, status, msg, ex);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ex.printStackTrace();
        return responseEntity(ErrorCode.SERVICE_UNAVAILABLE, status, "AsyncRequestTimeout", ex);
    }

    @Override
    protected ResponseEntity<Object> handleErrorResponseException(ErrorResponseException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return responseEntity(status.value(), status, ex.getDetailMessageCode(), ex);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ex.printStackTrace();
        String requiredType = ex.getRequiredType() == null ? null : ex.getRequiredType().getSimpleName();
        String msg = String.format("ConversionNotSupported: required type %s", requiredType);
        return responseEntity(ErrorCode.INTERNAL_ERR, status, msg, ex);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String requiredType = ex.getRequiredType() == null ? null : ex.getRequiredType().getSimpleName();
        String msg = String.format("ConversionNotSupported: property %s, required type %s", ex.getPropertyName(),
                requiredType);
        return responseEntity(ErrorCode.BAD_REQUEST, status, msg, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String errorMessage = "Malformed JSON request";
        if (ex.getCause() instanceof InvalidFormatException) {
            errorMessage = "Invalid format in JSON request";
        }
        return responseEntity(ErrorCode.BAD_REQUEST, status, errorMessage, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ex.printStackTrace();
        return responseEntity(ErrorCode.INTERNAL_ERR, status, "HttpMessageNotWritable", ex);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<?> handleAuthException(AuthenticationException e) {
        // e.printStackTrace();
        return responseEntity(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, e.getMessage(), e);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleUnknownException(Exception e) {
        e.printStackTrace();
        return responseEntity(ErrorCode.INTERNAL_ERR, HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e);
    }

    private ResponseEntity<Object> responseEntity(Integer errorCode, HttpStatusCode statusCode, String msg, Exception e) {
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .meta(ApiMeta.builder().code(errorCode).message(msg).build())
                        .build(),
                statusCode);
    }

}