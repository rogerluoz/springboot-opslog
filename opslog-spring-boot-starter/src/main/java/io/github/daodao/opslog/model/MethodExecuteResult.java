package io.github.daodao.opslog.model;

import lombok.Data;

/**
 * 方法执行结果对象
 *
 * @author Roger_Luo
 */
@Data
public class MethodExecuteResult {

  /**
   * 是否执行成功
   */
  private boolean successful;

  /**
   * 业务异常
   */
  private Throwable throwable;

  /**
   * 业务异常信息
   */
  private String errMsg;

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

  public MethodExecuteResult() {
  }

  public MethodExecuteResult(boolean successful, Throwable throwable, String errMsg) {
    this.successful = successful;
    this.throwable = throwable;
    this.errMsg = errMsg;
  }

  public MethodExecuteResult(boolean startNow) {
    if (startNow) {
      start();
    }
  }

  public void start() {
    this.successful = false;
    this.startTime = System.currentTimeMillis();
  }

  public void succeed() {
    this.successful = true;
    this.endTime = System.currentTimeMillis();
    this.duration = this.endTime - this.startTime;
  }

  public void exception(Throwable throwable) {
    this.successful = false;
    this.endTime = System.currentTimeMillis();
    this.duration = this.endTime - this.startTime;
    this.throwable = throwable;
    this.errMsg = throwable.getMessage();
  }
}
