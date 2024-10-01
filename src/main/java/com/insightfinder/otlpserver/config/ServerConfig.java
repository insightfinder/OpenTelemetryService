package com.insightfinder.otlpserver.config;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ServerConfig {
  public String insightFinderUrl;
  public int port;
  public TLS tls;
  public Worker worker;

  public static class TLS {
    public boolean enabled;
    public String certificateFile;
    public String privateKeyFile;
  }

  public static class Worker{
    public int processThreads;
    public int streamingThreads;
    public int streamingBatchSize = 100;
  }

  @Override
  public String toString() {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
    Yaml yaml = new Yaml(options);
    return yaml.dump(this);
  }
}
