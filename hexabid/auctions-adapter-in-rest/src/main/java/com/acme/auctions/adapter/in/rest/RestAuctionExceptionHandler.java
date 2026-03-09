package com.acme.auctions.adapter.in.rest;

import com.acme.auctions.core.auctioning.exception.AuctionBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestAuctionExceptionHandler {

    @ExceptionHandler(RestRequestRejectedException.class)
    ProblemDetail handleRejectedRequest(RestRequestRejectedException exception) {
        return problem(exception.status(), exception);
    }

    @ExceptionHandler(AuctionBusinessException.class)
    ProblemDetail handleAuctionBusiness(AuctionBusinessException exception) {
        return problem(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIllegalState(IllegalStateException exception) {
        return problem(HttpStatus.CONFLICT, exception);
    }

    private ProblemDetail problem(HttpStatus status, RuntimeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }
}
