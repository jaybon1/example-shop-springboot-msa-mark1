package com.example.shop.gateway.presentation.advice;

import com.example.shop.gateway.infrastructure.constants.Constants;
import com.example.shop.gateway.presentation.dto.ApiDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MissingRequestValueException.class)
//    public Mono<ResponseEntity<ApiDto<Object>>> handleMissingRequestValueException(MissingRequestValueException exception) {
//        return Mono.just(buildResponse(
//                Constants.ApiCode.MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION.toString(),
//                exception.getReason() != null ? exception.getReason() : "필수 요청 값이 누락되었습니다.",
//                null,
//                HttpStatus.BAD_REQUEST
//        ));
//    }
//
//    @ExceptionHandler(WebExchangeBindException.class)
//    public Mono<ResponseEntity<ApiDto<Object>>> handleWebExchangeBindException(WebExchangeBindException exception) {
//        Map<String, String> errorMap = new HashMap<>();
//        List<FieldError> fieldErrors = exception.getFieldErrors();
//        if (fieldErrors != null) {
//            fieldErrors.forEach(fieldError -> errorMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
//        }
//        return Mono.just(buildResponse(
//                Constants.ApiCode.BIND_EXCEPTION.toString(),
//                "요청한 데이터가 유효하지 않습니다.",
//                errorMap,
//                HttpStatus.BAD_REQUEST
//        ));
//    }
//
//    @ExceptionHandler(ConstraintViolationException.class)
//    public Mono<ResponseEntity<ApiDto<Object>>> handleConstraintViolationException(ConstraintViolationException exception) {
//        Map<String, String> errorMap = new HashMap<>();
//        exception.getConstraintViolations().forEach(constraintViolation -> {
//            List<Path.Node> pathNodeList = StreamSupport
//                    .stream(constraintViolation.getPropertyPath().spliterator(), false)
//                    .toList();
//            if (!pathNodeList.isEmpty()) {
//                errorMap.put(pathNodeList.get(pathNodeList.size() - 1).getName(), constraintViolation.getMessage());
//            }
//        });
//        return Mono.just(buildResponse(
//                Constants.ApiCode.CONSTRAINT_VIOLATION_EXCEPTION.toString(),
//                "요청한 데이터가 유효하지 않습니다.",
//                errorMap,
//                HttpStatus.BAD_REQUEST
//        ));
//    }
//
//    @ExceptionHandler(ServerWebInputException.class)
//    public Mono<ResponseEntity<ApiDto<Object>>> handleServerWebInputException(ServerWebInputException exception) {
//        String reason = Objects.requireNonNullElse(exception.getReason(), "");
//        if (reason.contains("Required request body is missing")) {
//            return Mono.just(buildResponse(
//                    Constants.ApiCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION.toString(),
//                    "RequestBody가 없습니다.",
//                    null,
//                    HttpStatus.BAD_REQUEST
//            ));
//        }
//        if (reason.contains("Enum class: ")) {
//            return Mono.just(buildResponse(
//                    Constants.ApiCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION.toString(),
//                    "Type 매개변수를 확인하세요.",
//                    null,
//                    HttpStatus.BAD_REQUEST
//            ));
//        }
//        return Mono.just(buildResponse(
//                Constants.ApiCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION.toString(),
//                "RequestBody를 형식에 맞추어 주세요.",
//                null,
//                HttpStatus.BAD_REQUEST
//        ));
//    }
//
//    @ExceptionHandler(MethodNotAllowedException.class)
//    public Mono<ResponseEntity<ApiDto<Object>>> handleMethodNotAllowedException(MethodNotAllowedException exception) {
//        return Mono.just(buildResponse(
//                Constants.ApiCode.HTTP_REQUEST_METHOD_NOT_SUPPORT_EXCEPTION.toString(),
//                "엔드포인트가 요청하신 메소드에 대해 지원하지 않습니다. 메소드, 엔드포인트, PathVariable을 확인하세요.",
//                null,
//                HttpStatus.BAD_REQUEST
//        ));
//    }
//
//    @ExceptionHandler(ConversionFailedException.class)
//    public Mono<ResponseEntity<ApiDto<Object>>> handleConversionFailedException(ConversionFailedException exception) {
//        String message = exception.getMessage() != null && exception.getMessage().contains("persistence.Enumerated")
//                ? "status를 정확히 입력해주세요."
//                : "PathVariable, QueryString, ResponseBody를 확인하세요.";
//        return Mono.just(buildResponse(
//                Constants.ApiCode.CONVERSION_FAILED_EXCEPTION.toString(),
//                message,
//                null,
//                HttpStatus.BAD_REQUEST
//        ));
//    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiDto<Object>>> handleException(Exception exception) {
        exception.printStackTrace();
        return Mono.just(buildResponse(
                Constants.ApiCode.EXCEPTION.toString(),
                exception.getMessage(),
                null,
                HttpStatus.INTERNAL_SERVER_ERROR
        ));
    }

    private ResponseEntity<ApiDto<Object>> buildResponse(String code, String message, Object data, HttpStatus status) {
        ApiDto.ApiDtoBuilder<Object> builder = ApiDto.builder()
                .code(code)
                .message(message);
        if (data != null && (!(data instanceof Map<?, ?> map) || !map.isEmpty())) {
            builder.data(data);
        }
        return ResponseEntity.status(status).body(builder.build());
    }
}
