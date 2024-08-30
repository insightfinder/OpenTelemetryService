package com.insightfinder.otlpserver.util;

import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValidationUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class.getName());


  public static boolean ValidLogData(LogData logData) {

    var userInMetadata = ParseUtil.getIfUserFromMetadata(logData.metadata);
    var licenseKeyInMetadata = ParseUtil.getLicenseKeyFromMedata(logData.metadata);
    if (licenseKeyInMetadata == null || licenseKeyInMetadata.isEmpty()) {
      LOG.error("No iflicenseKey found in header");
      return false;
    }
    if (userInMetadata == null || userInMetadata.isEmpty()){
      LOG.error("No ifuser found in header");
      return false;
    }

    var userInConfig = Config.getDataConfig().users.get(userInMetadata);
    if (userInConfig == null) {
      LOG.error("User '{}' doesn't found in config",userInMetadata);
      return false;
    }

    var licenseKeyInConfig = Config.getDataConfig().users.get(userInMetadata).licenseKey;
    if (licenseKeyInConfig == null || licenseKeyInConfig.isEmpty()) {
      LOG.error("User '{}' doesn't have licenseKey defined in config file.",userInMetadata);
      return false;
    }

    if(!licenseKeyInConfig.equals(licenseKeyInMetadata)){
      LOG.error("User '{}' in data header has different licenseKey defined in config file.",userInMetadata);
    }

    return true;
  }

  public static boolean ValidSpanData(SpanData spanData) {

    var userInMetadata = ParseUtil.getIfUserFromMetadata(spanData.metadata);
    var licenseKeyInMetadata = ParseUtil.getLicenseKeyFromMedata(spanData.metadata);
    if (licenseKeyInMetadata == null || licenseKeyInMetadata.isEmpty()) {
      LOG.error("No 'iflicenseKey' found in header");
      return false;
    }
    if (userInMetadata == null || userInMetadata.isEmpty()){
      LOG.error("No 'ifuser' found in header");
      return false;
    }

    var userInConfig = Config.getDataConfig().users.get(userInMetadata);
    if (userInConfig == null) {
      LOG.error("User '{}' doesn't found in config",userInMetadata);
      return false;
    }

    var licenseKeyInConfig = Config.getDataConfig().users.get(userInMetadata).licenseKey;
    if(!licenseKeyInConfig.equals(licenseKeyInMetadata)){
      LOG.error("User '{}' in data header has different licenseKey defined in config file.",userInMetadata);
    }

    return true;
  }

  public static boolean ValidateDataConfig(){
    var config = Config.getDataConfig();
    for(var user: config.users.keySet()){
      var userConfig = config.users.get(user);
      var licenseKey = userConfig.licenseKey;
      if(licenseKey == null || licenseKey.isEmpty()){
        LOG.error("User {} doesn't have licenseKey in the config file.",user);
        return false;
      }
      if(userConfig.log == null && userConfig.trace == null){
        LOG.error("User {} doesn't any data extraction rules in the config file.",user);
        return false;
      }

      // Check log Rules
      if ( userConfig.log != null){
        if( userConfig.log.extraction == null) {
          LOG.error("Log extraction rules are missing for user '{}'",user);
          return false;
        }
        var userLogExtractionRules = userConfig.log.extraction;
        if(userLogExtractionRules.instanceFrom == null || userLogExtractionRules.instanceFrom.isEmpty()){
          LOG.error("'instanceFrom' rule is missing in Log extraction rules for user '{}'",user);
          return false;
        }
        if(userLogExtractionRules.timestampFrom == null || userLogExtractionRules.timestampFrom.isEmpty()){
          LOG.error("'timestampFrom' rule is missing in Log extraction rules for user '{}'",user);
          return false;
        }
        if(userLogExtractionRules.projectFrom == null || userLogExtractionRules.projectFrom.isEmpty()){
          LOG.error("'projectFrom' rule is missing in Log extraction rules for user '{}'",user);
          return false;
        }
      }

      // Check trace Rules
      if ( userConfig.trace != null){
        if( userConfig.trace.extraction == null) {
          LOG.error("Trace extraction rules are missing for user '{}'",user);
          return false;
        }
        var traceLogExtractionRules = userConfig.trace.extraction;
        if(traceLogExtractionRules.instanceFrom == null || traceLogExtractionRules.instanceFrom.isEmpty()){
          LOG.error("'instanceFrom' rule is missing in trace extraction rules for user '{}'",user);
          return false;
        }
        if(traceLogExtractionRules.projectFrom == null || traceLogExtractionRules.projectFrom.isEmpty()){
          LOG.error("'projectFrom' rule is missing in trace extraction rules for user '{}'",user);
          return false;
        }
      }
    }

    return true;
  }


}
