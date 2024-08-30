package com.insightfinder.otlpserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;

public class Config {

    public static Logger LOG = LoggerFactory.getLogger(Config.class);
    private static ServerConfig serverConfig;
    private static DataConfig dataConfig;
    private static final String WorkDir = System.getProperty("user.dir");

    public static ServerConfig getServerConfig(){
      if (serverConfig == null) {
          try{
              serverConfig = new Yaml().loadAs(new FileInputStream(WorkDir + "/server.yaml"), ServerConfig.class);
              return serverConfig;
          }catch (Exception e){
              LOG.error(e.getMessage());
              System.exit(1);
          }
      }
      return serverConfig;
    }

    public static DataConfig getDataConfig(){
        if (dataConfig == null) {
            try{
                dataConfig = new Yaml().loadAs(new FileInputStream(WorkDir + "/data.yaml"), DataConfig.class);
                return dataConfig;
            }catch (Exception e) {
                LOG.error(e.getMessage());
                System.exit(1);
            }
        }
        return dataConfig;
    }

}
