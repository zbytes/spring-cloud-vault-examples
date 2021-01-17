package io.github.zbytes.examples.orderservice.interfaces;

import io.github.zbytes.examples.orderservice.core.EntityNotFound;
import io.github.zbytes.examples.orderservice.core.ViolationError;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import javax.servlet.http.HttpServletRequest;

@Primary
@ControllerAdvice
public class RESTEndpointsExceptionTranslator implements ProblemHandling {

    @ExceptionHandler
    public ResponseEntity<Problem> handleEntityNotFound(ViolationError ex, NativeWebRequest request) {
        return create(ex, problem(ex, request, Status.BAD_REQUEST), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleEntityNotFound(EntityNotFound ex, NativeWebRequest request) {
        return create(ex, problem(ex, request, Status.NOT_FOUND), request);
    }

    private ThrowableProblem problem(Exception ex, NativeWebRequest request, Status status) {
        return Problem.builder().withTitle(status.getReasonPhrase()).withStatus(status).with("path", toURI(request)).withDetail(ex.getMessage()).build();
    }

    private String toURI(NativeWebRequest request) {
        return request.getNativeRequest(HttpServletRequest.class).getRequestURI();
    }

}
