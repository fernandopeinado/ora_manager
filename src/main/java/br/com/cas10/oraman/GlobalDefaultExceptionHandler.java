package br.com.cas10.oraman;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class GlobalDefaultExceptionHandler {

  private static final Logger LOGGER = Logger.getLogger(GlobalDefaultExceptionHandler.class);

  @ExceptionHandler
  public void handleException(Exception e) throws Exception {
    LOGGER.error("Error", e);
    throw e;
  }
}
