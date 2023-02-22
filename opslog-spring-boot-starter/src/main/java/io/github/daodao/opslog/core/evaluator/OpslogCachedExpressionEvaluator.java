package io.github.daodao.opslog.core.evaluator;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;

import io.github.daodao.opslog.common.OpslogConsts;
import lombok.extern.slf4j.Slf4j;

/**
 * spring spEL 表达式缓存求值器
 *
 * @author Roger_Luo
 */
@Slf4j
public class OpslogCachedExpressionEvaluator extends CachedExpressionEvaluator {

  private Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);
  private Set<String> unevaluableKeySet = ConcurrentHashMap.newKeySet();

  /**
   * 创建 spEL 模板上下文实例
   *
   * @param method
   * @param args
   * @param beanFactory
   * @param result
   * @param errMsg
   * @return
   */
  public EvaluationContext createEvaluationContext(Method method, Object[] args, BeanFactory beanFactory, Object result,
      String errMsg) {
    OpslogEvaluationContext evaluationContext = new OpslogEvaluationContext(null, method, args,
        this.getParameterNameDiscoverer(), result, errMsg);
    // setBeanResolver 主要用于支持 spEL 模板中调用指定类的方法，如：@XXXService.xxxx(#root)
    Optional.ofNullable(beanFactory).ifPresent(bf -> evaluationContext.setBeanResolver(new BeanFactoryResolver(bf)));
    return evaluationContext;
  }

  /**
   * Parse expression
   *
   * @param expression
   * @param methodKey
   * @param evaluationContext
   * @return
   */
  public Object parseExpression(String expression, AnnotatedElementKey methodKey, EvaluationContext evaluationContext) {
    if (canEvaluate(expression)) {
      try {
        return getExpression(this.expressionCache, methodKey, expression).getValue(evaluationContext);
      } catch (SpelEvaluationException | SpelParseException | IllegalStateException e) {
        log.warn("[Opslog] Evaluate failed. expression=[{}], error: {}", expression, e.getMessage());
        // 避免将可解析的参数名放到缓存，导致以后不能重新解析
        if (!expression.startsWith("#") && !expression.startsWith("@") && !expression.startsWith(OpslogConsts.PREFIX)) {
          unevaluableKeySet.add(expression);
          log.warn("[Opslog] Expression key [{}] has been added to unevaluable cache", expression);
        }
      }
    }
    return expression;
  }

  /**
   * Can be evaluated
   *
   * @param expression
   * @return
   */
  public boolean canEvaluate(String expression) {
    return !unevaluableKeySet.contains(expression);
  }
}
