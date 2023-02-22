package io.github.daodao.opslog.common;

/**
 * 常量
 *
 * @author Roger_Luo
 */
public final class OpslogConsts {

  // 内置参数名的前缀
  public static final String PREFIX = "#";

  // 内置参数的后缀：业务异常信息的参数名
  public static final String METHOD_ERROR_MESSAGE_SUFFIX = "_errMsg";
  // 内置参数的后缀：业务返回数据的参数名
  public static final String METHOD_RESULT_SUFFIX = "_result";
  // 内置参数的后缀：内部发生异常的异常信息参数名
  public static final String INTERNAL_ERROR_MESSAGE_SUFFIX = "_itnErrMsg";

  // 完整参数名：业务异常信息的参数名
  public static final String METHOD_ERROR_MESSAGE = PREFIX + METHOD_ERROR_MESSAGE_SUFFIX;
  // 完整参数名：业务返回数据的参数名
  public static final String METHOD_RESULT = PREFIX + METHOD_RESULT_SUFFIX;
  // 完整参数名：内部发生异常的异常信息参数名
  public static final String INTERNAL_ERROR_MESSAGE = PREFIX + INTERNAL_ERROR_MESSAGE_SUFFIX;

}
