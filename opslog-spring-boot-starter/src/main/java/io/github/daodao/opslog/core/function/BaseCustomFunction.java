package io.github.daodao.opslog.core.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 基类，内置了一些小方法
 *
 * @author Roger_Luo
 */
public abstract class BaseCustomFunction implements ICustomFunction {

  /**
   * 检查入参参数是不是数组或列表
   *
   * @param obj
   * @return
   */
  public boolean isArrayObject(Object obj) {
    if (obj.getClass().isArray() || obj instanceof Collection) {
      return true;
    }
    return false;
  }

  /**
   * 将入参参数转换成 List
   *
   * @param obj
   * @return
   */
  public List<?> convertObjectToList(Object obj) {
    List<?> result = new ArrayList<>();
    if (obj.getClass().isArray()) {
      result = new ArrayList<>(Arrays.asList((Object[]) obj));
    } else if (obj instanceof Collection) {
      result = new ArrayList<>((Collection<?>) obj);
    }
    return result;
  }

  /**
   * 根据类型转换 List
   *
   * @param <T>
   * @param obj
   * @param targetType
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> convertObjectToList(Object obj, Class<?> targetType) {
    List<T> result = new ArrayList<>();
    if (obj.getClass().isArray()) {
      result = new ArrayList<>(Arrays.asList((T[]) obj));
    } else if (obj instanceof Collection) {
      result = new ArrayList<>((Collection<T>) obj);
    }
    return result;
  }
}
