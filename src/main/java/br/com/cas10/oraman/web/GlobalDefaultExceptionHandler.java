package br.com.cas10.oraman.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class GlobalDefaultExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

  @ExceptionHandler
  public void handleException(Exception e) throws Exception {
    LOGGER.error("Error", e);
    throw e;
  }
}
