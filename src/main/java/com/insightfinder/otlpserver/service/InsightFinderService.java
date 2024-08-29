package com.insightfinder.otlpserver.service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.insightfinder.otlpserver.config.Config;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class InsightFinderService {

  private static Logger LOG = LoggerFactory.getLogger(InsightFinderService.class.getName());

  public static void createProjectIfNotExist(String projectName, String user ,String licenseKey) {
    MultivaluedHashMap<String, String> bodyValues = new MultivaluedHashMap<>();

    bodyValues.add("userName", user);
    bodyValues.add("licenseKey", licenseKey);
    bodyValues.add("projectName", projectName);
    bodyValues.add("operation", "check");

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(Config.getServerConfig().insightFinderUrl +"/api/v1/check-and-add-custom-project"))
      .header("Content-Type", "application/json").
       POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(bodyValues)))
      .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      JsonObject resObject = new Gson().fromJson(response.body(), JsonObject.class);
      LOG.info("Response from InsightFinder: " + resObject);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    createProjectIfNotExist("maoyu-test-otlpserver", "maoyuwang", "test");
  }
}
