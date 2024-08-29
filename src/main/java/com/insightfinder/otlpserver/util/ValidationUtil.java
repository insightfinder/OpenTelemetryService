package com.insightfinder.otlpserver.util;

import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.LogData;
import io.grpc.Metadata;

import java.util.logging.Logger;

public class ValidationUtil {
  public static boolean ValidLogData(LogData logData) {

    Logger LOG = Logger.getLogger(ValidationUtil.class.getName());

    var userInMetadata = logData.metadata.get(Metadata.Key.of("ifuser", Metadata.ASCII_STRING_MARSHALLER));
    var licenseKeyInMetadata = logData.metadata.get(Metadata.Key.of("iflicenseKey", Metadata.ASCII_STRING_MARSHALLER));
    if (licenseKeyInMetadata == null || licenseKeyInMetadata.isEmpty()) {
      LOG.severe("No iflicenseKey found in header");
      return false;
    }
    if (userInMetadata == null || userInMetadata.isEmpty()){
      LOG.severe("No ifuser found in header");
      return false;
    }

    var userInConfig = Config.getDataConfig().users.get(userInMetadata);
    if (userInConfig == null) {
      LOG.severe("User '%s' doesn't found in config".formatted(userInMetadata));
      return false;
    }

    var licenseKeyInConfig = Config.getDataConfig().users.get(userInMetadata).licenseKey;
    if (licenseKeyInConfig == null || licenseKeyInConfig.isEmpty()) {
      LOG.severe("User '%s' doesn't have licenseKey defined in config file.".formatted(userInConfig));
      return false;
    }

    if(!licenseKeyInConfig.equals(licenseKeyInMetadata)){
      LOG.severe("User '%s' in data header has different licenseKey defined in config file.");
    }

    return true;
  }
}
