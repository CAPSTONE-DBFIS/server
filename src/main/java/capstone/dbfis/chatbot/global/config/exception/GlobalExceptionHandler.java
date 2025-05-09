package capstone.dbfis.chatbot.global.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 Bad Request - 잘못된 인자
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        logger.error("Bad request error: {}", ex.getMessage(), ex);
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 Bad Request - 비즈니스 예외
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        logger.error("Illegal state: {}", ex.getMessage(), ex);
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ResponseStatusException에서 던진 상태·메시지 그대로 사용
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        var body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getReason(),
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    // 401 Unauthorized - 헤더 누락
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getHeaderName() + " 헤더가 필요합니다.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 400 Bad Request - 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                String.format("파라미터 '%s' 형식이 올바르지 않습니다.", ex.getName()),
                req.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // 400 Bad Request - 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException ex, HttpServletRequest req) {
        var violations = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                violations,
                req.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // WebClient 4xx·5xx 에러
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(WebClientResponseException ex, HttpServletRequest req) {
        int statusCode = ex.getStatusCode().value();
        String reason = ex.getStatusText();
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus != null) reason = httpStatus.getReasonPhrase();

        var body = new ErrorResponse(
                LocalDateTime.now(),
                statusCode,
                reason,
                "외부 API 호출 오류: " + ex.getResponseBodyAsString(),
                req.getRequestURI()
        );
        return ResponseEntity.status(statusCode).body(body);
    }

    // WebClient 통신 실패
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequest(WebClientRequestException ex, HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "외부 서비스와 통신할 수 없습니다.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    // 401 JWT 만료
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(io.jsonwebtoken.ExpiredJwtException ex, HttpServletRequest req) {
        logger.warn("만료된 JWT 토큰 사용 시도: {}", ex.getMessage());
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "토큰이 만료되었습니다.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 401 JWT 위조/손상
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(io.jsonwebtoken.JwtException ex, HttpServletRequest req) {
        logger.warn("잘못된 JWT 토큰: {}", ex.getMessage());
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "유효하지 않은 토큰입니다.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 그 외 모든 예외는 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "서버 처리 중 오류가 발생했습니다.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 404 잘못된 api 주소
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "존재하지 않는 API 경로입니다.",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 405 Method Not Allowed - 잘못된 HTTP 메서드 (예: GET만 지원하는 API에 POST)
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(org.springframework.web.HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                "지원하지 않는 요청 방식입니다. 허용된 방식: " + String.join(", ", ex.getSupportedMethods()),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    // 400 Bad Request - JSON 파싱 실패
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Body",
                "요청 본문(JSON 형식 등)을 읽을 수 없습니다. 형식을 확인하세요.",
                req.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // 400 Bad Request - 필수 @RequestParam 누락
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest req) {
        var body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Missing Parameter",
                String.format("필수 파라미터 '%s'가 누락되었습니다.", ex.getParameterName()),
                req.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }
}