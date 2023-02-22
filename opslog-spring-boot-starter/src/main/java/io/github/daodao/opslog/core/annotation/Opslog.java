package io.github.daodao.opslog.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志注解
 *
 * @author Roger_Luo
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Opslog {

  /**
   * 租户，SAAS 系统中区分不同的用户
   *
   * @return
   */
  String tenant() default "";

  /**
   * 模块名字，例如 user / order 等
   *
   * @return
   */
  String module() default "";

  /**
   * 操作的类型，例如：read / write / update / delete 等
   *
   * @return
   */
  String type() default "";

  /**
   * 操作者
   *
   * @return
   */
  String operator() default "";

  /**
   * 自定义业务数据
   *
   * @return
   */
  String bizData() default "";

  /**
   * 执行成功时使用的日志内容
   *
   * @return
   */
  String success() default "";

  /**
   * 执行失败异常时使用的日志内容
   *
   * @return
   */
  String failure() default "";

  /**
   * 额外的数据
   *
   * @return
   */
  String detail() default "";

  /**
   * 是否记录日志的条件，默认 true(需要记录)
   *
   * @return
   */
  String condition() default "";

  /**
   * 执行前的数据
   *
   * @return
   */
  String before() default "";

  /**
   * 执行后的数据
   *
   * @return
   */
  String after() default "";
}
