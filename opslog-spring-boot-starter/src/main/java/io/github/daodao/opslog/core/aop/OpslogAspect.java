package io.github.daodao.opslog.core.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ObjectUtils;

import io.github.daodao.opslog.core.OpslogParser;
import io.github.daodao.opslog.core.annotation.Opslog;
import io.github.daodao.opslog.model.MethodExecuteResult;
import io.github.daodao.opslog.model.OpslogAnnotationData;
import io.github.daodao.opslog.model.OpslogResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志 AOP 切面逻辑
 *
 * @author Roger_Luo
 */
@Slf4j
@Aspect
@AllArgsConstructor
public class OpslogAspect {

  // 日志解析工具
  private OpslogParser opslogParser;

  /**
   * 定义切点
   */
  @Pointcut("@annotation(io.github.daodao.opslog.core.annotation.Opslog)")
  public void pointcut() {
  }

  @Around("pointcut() && @annotation(opslog)")
  public Object around(ProceedingJoinPoint joinPoint, Opslog opslog) throws Throwable {
    // ### 准备参数 ###
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    String[] argNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();
    Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());

    Map<String, Object> processData = OpslogAnnotationData.valueOf(opslog).toProcessData();
    List<String> internalErrMsgs = new ArrayList<>();

    // ### 执行业务方法前 ###
    Map<String, Object> beforeFuncValue = new HashMap<>();
    // 判断是否需要执行日志
    if (opslogParser.isRequired(processData)) {
      try {
        // 业务方法执行前的自定义函数解析
        beforeFuncValue = opslogParser.executeBefore(method, args, targetClass, processData, internalErrMsgs);
      } catch (Exception e) {
        // 记录内部错误，不要影响业务
        internalErrMsgs.add(String.format("OpslogParser.executeBefore() failed: %s", e.toString()));
        if (log.isDebugEnabled()) {
          log.error("[Opslog] Invoking OpslogParser.executeBefore() failed.", e);
        } else {
          log.warn("[Opslog] Invoking OpslogParser.executeBefore() failed: {}", e.getMessage());
        }
      }
    }

    // ### 执行业务方法 ###
    Object jpResult = null;
    MethodExecuteResult executeResult = new MethodExecuteResult(true);
    try {
      jpResult = joinPoint.proceed();
      executeResult.succeed();
    } catch (Throwable e) {
      // 业务方法执行异常时，捕获异常并暂存
      executeResult.exception(e);
    }

    // ### 业务方法执行后 ###
    if (opslogParser.isRequired(processData)) {
      try {
        // 执行日志解析
        opslogParser.executeAfter(method, args, targetClass, jpResult, executeResult, processData, beforeFuncValue,
            internalErrMsgs);

        // 执行全局方法
        opslogParser.executeGlobal(processData, internalErrMsgs);
      } catch (Exception e) {
        // 记录内部错误，不要影响业务
        internalErrMsgs.add(String.format("OpslogParser.executeAfter() failed: %s", e.toString()));
        if (log.isDebugEnabled()) {
          log.error("[Opslog] Invoking OpslogParser.executeAfter() failed.", e);
        } else {
          log.warn("[Opslog] Invoking OpslogParser.executeAfter() failed: {}", e.getMessage());
        }
      } finally {
        try {
          if (opslogParser.isRequired(processData)) {
            // 组装日志结果
            OpslogResult logResult = opslogParser.convertOpslogResult(method, argNames, args, jpResult, executeResult,
                processData, internalErrMsgs);
            // 执行持久化
            opslogParser.persist(logResult);
          }
        } catch (Throwable e) {
          if (log.isDebugEnabled()) {
            log.error("[Opslog] Invoking OpslogParser.persist() failed.", e);
          } else {
            log.warn("[Opslog] Invoking OpslogParser.persist() failed: {}", e.getMessage());
          }
        }
      }
    }

    // 抛出业务异常
    if (!executeResult.isSuccessful() || !ObjectUtils.isEmpty(executeResult.getThrowable())) {
      throw executeResult.getThrowable();
    }

    return jpResult;
  }
}
