package io.github.daodao.opslog.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * opslog properties
 *
 * @author Roger_Luo
 */
@Data
@ConfigurationProperties(prefix = OpslogProperties.PROPERTIES_PREFIX, ignoreUnknownFields = true)
public class OpslogProperties {

  /**
   * 配置前缀
   */
  public static final String PROPERTIES_PREFIX = "opslog";

  /**
   * enabled 配置的名字，需要跟下面的参数名字一样
   */
  public static final String PROPERTIES_ENABLED_NAME = "enabled";

  /**
   * 是否开启操作日志，默认开启
   */
  private boolean enabled = true;

  /**
   * 应用名称，默认取 spring.application.name
   */
  @Value("${spring.application.name:#{null}}")
  private String applicationName;
}
