package com.nagpal.shivam.workout_manager_user;

import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.verticles.MainVerticle;
import io.vertx.core.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    MainVerticle mainVerticle = new MainVerticle();
    vertx.deployVerticle(mainVerticle).onSuccess(result -> {
      logger.log(Level.INFO, MessageConstants.SUCCESSFULLY_DEPLOYED_THE_VERTICLES);
    }).onFailure(throwable -> {
      logger.log(Level.SEVERE, throwable.getMessage(), throwable);
      vertx.close();
    });
  }
}
