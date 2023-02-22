package io.github.daodao.opslog.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志的结果数据对象
 *
 * @author Roger_Luo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpslogResult {

  /**
   * 当前应用的名称
   * <p>
   * 主要用于区分不同应用产生的日志信息
   * <p>
   * 可以通过配置项 opslog.applicationName 设置，默认取 spring.application.name
   */
  private String applicationName;

  /**
   * 租户
   */
  private String tenant;

  /**
   * 操作者
   */
  private String operator;

  /**
   * 模块
   */
  private String module;

  /**
   * 操作类型
   */
  private String type;

  /**
   * 日志内容
   */
  private String content;

  /**
   * 日志详情
   */
  private String detail;

  /**
   * 业务数据
   */
  private Object bizData;

  /**
   * 执行前的业务数据
   */
  private Object before;

  /**
   * 执行后的业务数据
   */
  private Object after;

  /**
   * 操作开始时间，时间戳，单位：ms
   */
  private Long startTime;

  /**
   * 操作结束时间，时间戳，单位：ms
   */
  private Long endTime;

  /**
   * 操作花费时间，时间戳，单位：ms
   */
  private Long duration;

  /**
   * 是否调用成功
   */
  private boolean successful;

  /**
   * 业务方法抛出的异常信息
   */
  private String errMsg;

  // ======== 执行方法相关的数据 ========

  /**
   * 正在请求的方法
   */
  private Method method;

  /**
   * 方法的入参
   */
  private Map<String, Object> arguments;

  /**
   * 方法执行结果（出参）
   */
  private Object result;

  // ======== HTTP 请求相关的数据 ========

  /**
   * 请求的 URL
   */
  private String url;

  /**
   * IP 地址
   */
  private String ip;

  // ======== 内部的数据 ========

  /**
   * Internal Error Messages
   */
  private Collection<String> internalErrorMsgs;

  // private List<FieldInfo> compareFields;
}
