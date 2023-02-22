package io.github.daodao.opslog.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.daodao.opslog.common.OpslogConsts;
import io.github.daodao.opslog.configuration.OpslogProperties;
import io.github.daodao.opslog.core.evaluator.OpslogCachedExpressionEvaluator;
import io.github.daodao.opslog.core.function.ICustomFunction;
import io.github.daodao.opslog.core.operator.IOperatorService;
import io.github.daodao.opslog.core.persist.IResultPersistor;
import io.github.daodao.opslog.model.MethodExecuteResult;
import io.github.daodao.opslog.model.OpslogResult;
import io.github.daodao.opslog.model.OpslogResult.OpslogResultBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志解析工具
 *
 * @author Roger_Luo
 */
@Slf4j
public class OpslogParser implements BeanFactoryAware {

  // 自定义函数模板格式：{function{xxx}}
  private static final Pattern CUSTOM_FUNCTION_PATTERN = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");

  // 找不到自定义函数时的格式：?function(xxx)?
  private static final String CUSTOM_FUNCTION_UNKNOWN_FORMAT = "?%s(%s)?";

  // 表达式缓存求职器
  private final OpslogCachedExpressionEvaluator cachedExpressionEvaluator = new OpslogCachedExpressionEvaluator();

  // Jackson 序列化，用于执行前的对象序列化
  private ObjectMapper objectMapper = new ObjectMapper();

  // beanFactory
  private BeanFactory beanFactory;

  private OpslogProperties properties;

  private IOperatorService operatorService;

  private IResultPersistor resultPersistor;

  /**
   * Constructor
   *
   * @param operatorService
   * @param resultPersistor
   */
  public OpslogParser(OpslogProperties properties, IOperatorService operatorService, IResultPersistor resultPersistor) {
    Assert.notNull(properties, "OpslogProperties must not be null");
    Assert.notNull(operatorService, "IOperatorService must not be null");
    Assert.notNull(resultPersistor, "IOpslogResultPersistor must not be null");
    this.properties = properties;
    this.operatorService = operatorService;
    this.resultPersistor = resultPersistor;
  }

  /**
   * 业务代码执行前，预处理日志的逻辑
   *
   * @param jpMethod
   * @param jpArgs
   * @param jpTargetClass
   * @param processData      需要处理的日志内容模板 map
   * @param internalErrorMsg 用来收集内部错误的集合
   * @return 返回预处理完的数据
   */
  public Map<String, Object> executeBefore(Method jpMethod, Object[] jpArgs, Class<?> jpTargetClass,
      Map<String, Object> processData, Collection<String> internalErrorMsg) {
    AnnotatedElementKey elementKey = new AnnotatedElementKey(jpMethod, jpTargetClass);
    EvaluationContext evaluationContext = cachedExpressionEvaluator.createEvaluationContext(jpMethod, jpArgs,
        beanFactory,
        null, null);
    // 暂存自定义函数执行的结果
    Map<String, Object> funcValMap = new HashMap<>();
    for (Entry<String, Object> entry : processData.entrySet()) {
      String annoName = entry.getKey();
      Object obj = entry.getValue();
      if (obj instanceof String && obj.toString().contains("{")) {
        Matcher matcher = CUSTOM_FUNCTION_PATTERN.matcher(obj.toString());
        while (matcher.find()) {
          String param = matcher.group(2);
          // 跳过内置参数
          if (param.contains(OpslogConsts.METHOD_ERROR_MESSAGE) || param.contains(OpslogConsts.METHOD_RESULT)
              || param.contains(OpslogConsts.INTERNAL_ERROR_MESSAGE)) {
            continue;
          }
          // 不执行 after 的模板
          if ("after".equalsIgnoreCase(annoName)) {
            continue;
          }

          // 获取自定义函数名
          String funcName = matcher.group(1);
          if (!ObjectUtils.isEmpty(funcName)) {
            try {
              ICustomFunction func = getFunctionBean(funcName);
              if ("before".equalsIgnoreCase(annoName) || func.isExecuteBefore()) {
                Object funcArgs = cachedExpressionEvaluator.parseExpression(param, elementKey, evaluationContext);
                Object funcValue = func.execute(funcArgs);
                // 需要把返回值深拷贝，防止业务方法执行完之后对象的引用被修改
                Object cloneFuncValue = objectMapper.readValue(objectMapper.writeValueAsString(funcValue),
                    funcValue.getClass());
                funcValMap.put(functionKey(annoName, funcName, param), cloneFuncValue);
              }
            } catch (Exception e) {
              // 收集错误信息，不抛出异常
              collectInternalError(internalErrorMsg, e);
            }
          }
        }
      }
    }

    return funcValMap;
  }

  /**
   * 业务代码执行后，处理日志的逻辑
   *
   * @param jpMethod
   * @param jpArgs
   * @param jpTargetClass
   * @param jpResult         业务方法返回的结果
   * @param executeResult    业务方法执行的结果
   * @param processData      需要处理的日志内容模板 map
   * @param beforeFuncData   {@link #executeBefore(Method, Object[], Class, Map, Collection)} 方法的结果
   * @param internalErrorMsg 用来收集内部错误的集合
   */
  public void executeAfter(Method jpMethod, Object[] jpArgs, Class<?> jpTargetClass, Object jpResult,
      MethodExecuteResult executeResult, Map<String, Object> processData, Map<String, Object> beforeFuncData,
      Collection<String> internalErrorMsg) {
    AnnotatedElementKey elementKey = new AnnotatedElementKey(jpMethod, jpTargetClass);
    EvaluationContext evaluationContext = cachedExpressionEvaluator.createEvaluationContext(jpMethod, jpArgs,
        beanFactory,
        jpResult, executeResult.getErrMsg());
    for (Entry<String, Object> entry : processData.entrySet()) {

      // skip empty or null expression
      if (ObjectUtils.isEmpty(entry.getValue())) {
        continue;
      }

      String expr = entry.getValue().toString();
      // 如果有大括号标记
      if (expr.contains("{")) {
        Matcher matcher = CUSTOM_FUNCTION_PATTERN.matcher(expr);
        StringBuffer parsedStr = new StringBuffer();
        while (matcher.find()) {
          String funcName = matcher.group(1);
          String param = matcher.group(2);

          // 先给 spEL 处理入参
          Object funcArgs = null;
          try {
            funcArgs = cachedExpressionEvaluator.parseExpression(param, elementKey, evaluationContext);
          } catch (Exception e) {
            // 收集错误信息，不抛出异常
            collectInternalError(internalErrorMsg, e);
          }

          // 函数名为空时，即：{{xxx}} 格式
          if (ObjectUtils.isEmpty(funcName)) {
            // 如果模板完全匹配时，直接返回对象，而不返回字符串
            if (matcher.start() == 0 && matcher.end() == expr.length()) {
              entry.setValue(funcArgs);
              break;
            }

            // 将 {{xxx}} 替换成 spEL 值
            matcher.appendReplacement(parsedStr, Optional.ofNullable(funcArgs).orElse("").toString());
          } else {
            // 有自定义函数时
            try {
              // 获取自定义函数
              ICustomFunction func = getFunctionBean(funcName);
              // 得到函数值
              Optional<Object> optData = Optional
                  .ofNullable(beforeFuncData.get(functionKey(entry.getKey(), funcName, param)));
              Object funcValue = optData.isPresent() ? optData.get() : func.execute(funcArgs);

              // 如果模板完全匹配时，直接返回对象，而不返回字符串
              if (matcher.start() == 0 && matcher.end() == expr.length()) {
                entry.setValue(funcValue);
                break;
              }

              // 将 {function{xxx}} 替换成函数值
              matcher.appendReplacement(parsedStr, Optional.ofNullable(funcValue).orElse("").toString());
            } catch (Exception e) { // 自定义函数找不到时

              // 如果模板完全匹配时，返回空字符串
              if (matcher.start() == 0 && matcher.end() == expr.length()) {
                entry.setValue("");
                // 收集错误信息，不抛出异常
                collectInternalError(internalErrorMsg, e);
                break;
              }

              // 将 {wrongFunc{xxx}} 替换成 ?wrongFunc(xxx)?
              matcher.appendReplacement(parsedStr, String.format(CUSTOM_FUNCTION_UNKNOWN_FORMAT, funcName, funcArgs));
              // 收集错误信息，不抛出异常
              collectInternalError(internalErrorMsg, e);
            }
          }
        }

        // 不是直接返回对象时，把剩余的字符串拼接进去
        if (parsedStr.length() != 0) {
          matcher.appendTail(parsedStr);
          entry.setValue(parsedStr.toString());
        }
      } else {
        // 直接用 spEL 解析
        try {
          entry.setValue(cachedExpressionEvaluator.parseExpression(expr, elementKey, evaluationContext));
        } catch (Exception e) {
          // 收集错误信息，不抛出异常
          collectInternalError(internalErrorMsg, e);
        }
      }
    }
  }

  /**
   * 执行统一处理
   *
   * @param processData
   * @param internalErrorMsg 用来收集内部错误的集合
   */
  public void executeGlobal(Map<String, Object> processData, Collection<String> internalErrorMsg) {
    if (ObjectUtils.isEmpty(processData.get("tenant"))) {
      processData.put("tenant", operatorService.getTenant());
    }
    if (ObjectUtils.isEmpty(processData.get("operator"))) {
      processData.put("operator", operatorService.getOperator());
    }
  }

  /**
   * 组装日志结果数据
   *
   * @param jpMethod         业务方法
   * @param jpArgNames       业务方法的入参名
   * @param jpArgs           业务方法的入参值
   * @param jpResult         业务方法返回的结果
   * @param executeResult    业务方法执行的结果数据
   * @param processData      成功解析的日志内容
   * @param internalErrorMsg 内部错误信息
   * @return
   */
  public OpslogResult convertOpslogResult(Method jpMethod, String[] jpArgNames, Object[] jpArgs, Object jpResult,
      MethodExecuteResult executeResult, Map<String, Object> processData, Collection<String> internalErrorMsg) {
    OpslogResultBuilder builder = OpslogResult.builder()
        .applicationName(properties.getApplicationName())
        .module(Optional.ofNullable(processData.get("module")).orElse("").toString())
        .type(Optional.ofNullable(processData.get("type")).orElse("").toString())
        .detail(Optional.ofNullable(processData.get("detail")).orElse("").toString())
        .tenant(Optional.ofNullable(processData.get("tenant")).orElse("").toString())
        .operator(Optional.ofNullable(processData.get("operator")).orElse("").toString())
        .bizData(processData.get("bizData")) // 可能为 Object 对象，不能 toString
        .before(processData.get("before")) // 可能为 Object 对象，不能 toString
        .after(processData.get("after")) // 可能为 Object 对象，不能 toString
        .successful(executeResult.isSuccessful())
        .startTime(executeResult.getStartTime())
        .endTime(executeResult.getEndTime())
        .duration(executeResult.getDuration())
        .errMsg(executeResult.getErrMsg());

    // 日志内容
    if (executeResult.isSuccessful() || !ObjectUtils.isEmpty(processData.get("failure"))) {
      builder.content(
          executeResult.isSuccessful() ? processData.get("success").toString() : processData.get("failure").toString());
    } else {
      log.warn("[Opslog] 'failure' content not found, using 'errMsg' content instead. method=[{}]", jpMethod.getName());
      builder.content(executeResult.getErrMsg());
    }

    // 方法相关的数据
    builder.method(jpMethod).arguments(convertMethodArgs(jpArgNames, jpArgs)).result(jpResult);

    // HTTP 请求相关的数据
    try {
      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
          .getRequest();
      // IP & URL
      builder.ip(request.getRemoteAddr()).url(request.getRequestURI());
    } catch (IllegalStateException e) {
      log.warn("[Opslog] get HTTP request information failed: {}", e.getMessage());
    }

    // 内部错误信息
    if (!ObjectUtils.isEmpty(internalErrorMsg)) {
      builder.internalErrorMsgs(internalErrorMsg);
    }

    return builder.build();
  }

  /**
   * 执行持久化
   *
   * @param result
   */
  public void persist(OpslogResult result) {
    resultPersistor.persist(result);
  }

  /**
   * 判断是否需要记录日志
   *
   * @param processData
   * @return
   */
  public boolean isRequired(Map<String, Object> processData) {
    Object condition = processData.get("condition");
    if (condition != null && "false".equalsIgnoreCase(condition.toString())) {
      return false;
    }
    return true;
  }

  /**
   * 获取自定义函数 bean
   *
   * @param name 自定义函数名
   * @return 为空时表示获取失败
   */
  private ICustomFunction getFunctionBean(String name) {
    Assert.hasText(name, "'name' must not be empty");
    return beanFactory.getBean(name, ICustomFunction.class);
  }

  /**
   * 拼接自定义函数的 key
   *
   * @param annoName
   * @param funcName
   * @param param
   * @return
   */
  private String functionKey(String annoName, String funcName, String param) {
    return annoName + funcName + param;
  }

  /**
   * 组装方法的入参参数
   *
   * @param argNames
   * @param args
   * @return
   */
  private Map<String, Object> convertMethodArgs(String[] argNames, Object[] args) {
    Map<String, Object> result = new HashMap<>(argNames.length);
    for (int i = 0; i < argNames.length; i++) {
      result.put(argNames[i], args[i]);
    }
    return result;
  }

  /**
   * 收集内部异常错误信息
   *
   * @param internalErrMsg 用来收集内部错误的集合
   * @param e
   */
  private void collectInternalError(Collection<String> internalErrMsg, Throwable e) {
    StringWriter sw = new StringWriter();
    PrintWriter printWriter = new PrintWriter(sw);
    try {
      e.printStackTrace(printWriter);
      internalErrMsg.add(sw.toString());
    } finally {
      printWriter.close();
    }
  }

  /**
   * 实现 {@link BeanFactoryAware} 以获取 spring 容器中的 {@link BeanFactory} 对象
   */
  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

}
