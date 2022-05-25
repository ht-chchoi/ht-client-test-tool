package com.ht.testtool.core;

import com.ht.testtool.core.websocket.WebsocketClient;
import com.ht.testtool.data.type.ContextType;
import com.ht.testtool.exception.ContextNotFoundException;
import com.ht.testtool.ui.MainFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MainContext {
  private static final MainContext INSTANCE = new MainContext();
  private final Map<ContextType, Object> contextMap = new ConcurrentHashMap<>();

  private MainContext() {
    this.initContextMap();
  }

  public static synchronized MainContext getInstance() {
    return INSTANCE;
  }

  private void initContextMap() {
    this.contextMap.put(ContextType.CLIENT_MANAGER, new ClientManager());
    this.contextMap.put(ContextType.WEBSOCKET_CLIENT_MAP, new ConcurrentHashMap<String, WebsocketClient>());

    log.info("MainContext load success");
  }

  public synchronized ClientManager getClientManager() {
    try {
      return (ClientManager) this.contextMap.get(ContextType.CLIENT_MANAGER);
    } catch (Exception e) {
      log.error("fail to get ClientManager", e);
      MainFrame.exitWithErrorDialog("fail to get ClientManager");
      return null;
    }
  }

  public synchronized void putWebsocketClient(final String websocketClientId, final WebsocketClient websocketClient) {
    this.getWebsocketClientMap().put(websocketClientId, websocketClient);
    log.info("putWebsocketClient to Context, websocketClientId: {}", websocketClientId);
  }

  @SuppressWarnings("unchecked")
  public Map<String, WebsocketClient> getWebsocketClientMap() {
    return (Map<String, WebsocketClient>) this.contextMap.get(ContextType.WEBSOCKET_CLIENT_MAP);
  }

  public synchronized WebsocketClient getWebsocketClientById(final String websocketClientId) throws ContextNotFoundException {
    Map<String, WebsocketClient> websocketClientMap = this.getWebsocketClientMap();
    if (!websocketClientMap.containsKey(websocketClientId)) {
      throw new ContextNotFoundException();
    }
    return websocketClientMap.get(websocketClientId);
  }
}
