package io.github.daodao.opslog.configuration;

import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.github.daodao.opslog.core.OpslogParser;
import io.github.daodao.opslog.core.aop.OpslogAspect;
import io.github.daodao.opslog.core.function.BaseCustomFunction;
import io.github.daodao.opslog.core.function.ICustomFunction;
import io.github.daodao.opslog.core.operator.IOperatorService;
import io.github.daodao.opslog.core.persist.IResultPersistor;
import lombok.extern.slf4j.Slf4j;

/**
 * opslog configuration
 *
 * @author Roger_Luo
 *
 */
@Slf4j
@Configuration
@Import(AopAutoConfiguration.class)
@EnableConfigurationProperties({ OpslogProperties.class })
@ConditionalOnProperty(prefix = OpslogProperties.PROPERTIES_PREFIX, name = OpslogProperties.PROPERTIES_ENABLED_NAME, havingValue = "true", matchIfMissing = true)
public class OpslogAutoConfiguration {

  /**
   * AspectJ AOP Bean
   *
   * @param opslogParser
   * @return
   */
  @Bean
  @ConditionalOnClass
  OpslogAspect opslogAspect(OpslogParser opslogParser) {
    log.info("[Opslog] Initializing Opslog AspectJ AOP");
    return new OpslogAspect(opslogParser);
  }

  /**
   * Core Opslog Parser
   *
   * @param properties
   * @param operatorService
   * @param resultPersistor
   * @return
   */
  @Bean
  OpslogParser opslogParser(OpslogProperties properties, IOperatorService operatorService,
      IResultPersistor resultPersistor) {
    log.info("[Opslog] Initializing Opslog Parser");
    return new OpslogParser(properties, operatorService, resultPersistor);
  }

  /**
   * Default ICustomFunction
   *
   * @return
   */
  @Bean
  @ConditionalOnMissingBean(ICustomFunction.class)
  ICustomFunction customFunction() {
    log.info("[Opslog] Bean of type 'ICustomFunction' not found, using default one");
    return new BaseCustomFunction() {
    };
  }

  /**
   * Default IOperatorService
   *
   * @return
   */
  @Bean
  @ConditionalOnMissingBean(IOperatorService.class)
  IOperatorService operatorService() {
    log.info("[Opslog] Bean of type 'IOperatorService' not found, using default one");
    return new IOperatorService() {
    };
  }

  /**
   * Default IResultPersistor
   *
   * @return
   */
  @Bean
  @ConditionalOnMissingBean(IResultPersistor.class)
  IResultPersistor resultPersistor() {
    log.info("[Opslog] Bean of type 'IResultPersistor' not found, using default one");
    return new IResultPersistor() {
    };
  }
}
