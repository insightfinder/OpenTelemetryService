package com.insightfinder.otlpserver.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

public class Config {

    public static Logger LOG = Logger.getLogger(Config.class.getName());
    private static ServerConfig serverConfig;
    private static DataConfig dataConfig;
    private static final String WorkDir = System.getProperty("user.dir");

    public static ServerConfig getServerConfig(){

      if (serverConfig == null) {
        serverConfig = new ServerConfig();

        try{
          InputStream inputStream = new FileInputStream(WorkDir + "/server.yaml");
          serverConfig = new Yaml().loadAs(inputStream,ServerConfig.class);
        }catch (Exception e){
          LOG.severe(e.toString());
          System.exit(1);
        }
      }

      return serverConfig;
    }

    public static DataConfig getDataConfig(){
      if (dataConfig == null) {
        dataConfig = new DataConfig();
        try{
          InputStream inputStream = new FileInputStream(WorkDir + "/data.yaml");
          dataConfig = new Yaml().loadAs(inputStream, DataConfig.class);
        }catch (Exception e){
          LOG.severe(e.toString());
          System.exit(1);
        }
      }
      return dataConfig;
    }
}
