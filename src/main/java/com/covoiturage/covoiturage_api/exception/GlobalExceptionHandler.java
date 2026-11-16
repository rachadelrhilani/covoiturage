package com.covoiturage.covoiturage_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Ressource introuvable [{}] : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Règle métier refusée [{}] : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedActionException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedActionException ex, HttpServletRequest request) {
        log.warn("Action non autorisée [{}] : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Requête invalide [{}] : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Erreur de validation [{}] : {}", request.getRequestURI(), errors);
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errors, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Contrainte d'unicité violée [{}]", request.getRequestURI());
        return buildProblemDetail(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "Une donnée unique (email, numéro de permis ou immatriculation) existe déjà.", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erreur interne sur [{}] : ", request.getRequestURI(), ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Une erreur interne inattendue s'est produite.", request);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String errorType, String message, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setType(URI.create("https://api.covoiturage.com/errors/" + errorType.toLowerCase()));
        problem.setTitle(errorType);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}