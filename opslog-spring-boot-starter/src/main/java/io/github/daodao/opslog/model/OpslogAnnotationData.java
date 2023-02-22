package io.github.daodao.opslog.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import io.github.daodao.opslog.core.annotation.Opslog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注解{@code @Opslog}的实体对象
 *
 * @author Roger_Luo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpslogAnnotationData {
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
   * 成功时的日志内容
   */
  private String success;

  /**
   * 失败时的日志内容
   */
  private String failure;

  /**
   * 日志详情数据
   */
  private String detail;

  /**
   * 是否记录日志的条件，默认 true(需要记录)
   */
  private String condition;

  /**
   * 业务数据
   */
  private String bizData;

  /**
   * 执行前的业务数据
   */
  private Object before;

  /**
   * 执行后的业务数据
   */
  private Object after;

  /**
   * 将注解{@code @Opslog} 转换成 {@link OpslogAnnotationData}
   * <p>
   * Convert {@code @Opslog} to {@link OpslogAnnotationData}
   *
   * @param opslog
   * @return
   */
  public static OpslogAnnotationData valueOf(Opslog opslog) {
    OpslogAnnotationDataBuilder builder = OpslogAnnotationData.builder()
        .tenant(opslog.tenant())
        .operator(opslog.operator())
        .module(opslog.module())
        .type(opslog.type())
        .success(opslog.success())
        .failure(opslog.failure())
        .detail(opslog.detail())
        .condition(opslog.condition())
        .bizData(opslog.bizData())
        .before(opslog.before())
        .after(opslog.after());
    return builder.build();
  }

  /**
   * 转化成处理过程中使用的 map data
   *
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public Map<String, Object> toProcessData() throws IllegalArgumentException, IllegalAccessException {
    Field[] declaredFields = this.getClass().getDeclaredFields();
    Map<String, Object> result = new HashMap<>(declaredFields.length);
    for (Field field : declaredFields) {
      field.setAccessible(true);
      result.put(field.getName(), field.get(this));
    }
    return result;
  }
}
