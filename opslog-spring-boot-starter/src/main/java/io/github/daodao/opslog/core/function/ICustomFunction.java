package io.github.daodao.opslog.core.function;

/**
 * 自定义函数接口
 *
 * @author Roger_Luo
 */
public interface ICustomFunction {
  /**
   * 是否在业务方法逻辑执行前，执行自定义函数
   *
   * @return 是否提前执行
   */
  default boolean isExecuteBefore() {
    return false;
  };

  /**
   * 自定义函数执行逻辑
   *
   * @param param 参数
   * @return 执行结果
   */
  default Object execute(Object param) {
    return null;
  };
}
