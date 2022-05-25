package com.ht.testtool.core;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PropertyLoader {
  private static final PropertyLoader INSTANCE = new PropertyLoader();
  private final Map<String, String> propertyMap = new ConcurrentHashMap<>();

  private PropertyLoader() {
    this.getDefaultProperty();
  }

  public static PropertyLoader getInstance() {
    return INSTANCE;
  }

  private Map<String, String> getDefaultProperty() {
    propertyMap.put("WEBSOCKET_MAX_CONNECTION", "1");

    log.info("load default property success");
    return propertyMap;
  }

  public String getProperty(final String key) {
    return this.propertyMap.get(key);
  }
}
