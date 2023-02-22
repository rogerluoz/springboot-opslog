package io.github.daodao.opslog.core.evaluator;

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

import io.github.daodao.opslog.common.OpslogConsts;

/**
 * spring spEL 解析器用到的模板上下文
 *
 * @author Roger_Luo
 */
public class OpslogEvaluationContext extends MethodBasedEvaluationContext {

  public OpslogEvaluationContext(Object rootObject, Method method, Object[] arguments,
      ParameterNameDiscoverer parameterNameDiscoverer, Object result, String errMsg) {

    // 把 controller 方法的参数都放到上下文
    super(rootObject, method, arguments, parameterNameDiscoverer);

    super.lazyLoadArguments();

    // 将业务结果以及业务异常信息放进内置参数名
    super.setVariable(OpslogConsts.METHOD_RESULT_SUFFIX, Optional.ofNullable(result).orElse(""));
    super.setVariable(OpslogConsts.METHOD_ERROR_MESSAGE_SUFFIX, Optional.ofNullable(errMsg).orElse(""));
  }

}
