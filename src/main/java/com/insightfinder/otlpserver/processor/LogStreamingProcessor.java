package com.insightfinder.otlpserver.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStreamingProcessor implements Runnable {

  private Logger LOG = LoggerFactory.getLogger(LogStreamingProcessor.class.getName());

  public LogStreamingProcessor(int threadNum){
    LOG.info("LogStreamingProcessor thread " + threadNum + " started.");
  }

  @Override
  public void run() {

  }

}
