package io.github.daodao.opslog.core.operator;

/**
 * 获取操作者和租户
 *
 * @author Roger_Luo
 */
public interface IOperatorService {
  /**
   * 当前操作者
   *
   * @return
   */
  default String getOperator() {
    return null;
  };

  /**
   * 当前租户
   *
   * @return
   */
  default String getTenant() {
    return null;
  };
}
