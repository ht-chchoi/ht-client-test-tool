package com.ht.testtool.data.dto;

import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class ConnectionInfo {
  private String scheme;
  private String ip;
  private int port;
  private String path;
  private Map<String, String> params;
  private Object body;

  public URI createUri() throws URISyntaxException {
    return new URI(new StringBuilder(this.scheme)
        .append("://")
        .append(this.ip)
        .append(":")
        .append(this.port)
        .append(path)
        .append("?")
        .append(params.entrySet().stream()
            .map(paramEntry -> paramEntry.getKey() + "=" + paramEntry.getValue())
            .collect(Collectors.joining("&")))
        .toString());
  }
}
