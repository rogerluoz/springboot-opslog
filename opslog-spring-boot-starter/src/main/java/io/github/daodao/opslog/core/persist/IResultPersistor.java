package io.github.daodao.opslog.core.persist;

import io.github.daodao.opslog.model.OpslogResult;

/**
 * 日志持久化接口
 *
 * @author Roger_Luo
 */
public interface IResultPersistor {

  /**
   * 保存日志
   *
   * @param result
   */
  default void persist(OpslogResult result) {
    System.out.println("[Opslog] result=" + result.toString());
  };
}
