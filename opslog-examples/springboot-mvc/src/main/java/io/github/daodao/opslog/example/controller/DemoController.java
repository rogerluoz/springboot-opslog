package io.github.daodao.opslog.example.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.daodao.opslog.core.annotation.Opslog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 专门放置演示用的接口
 *
 * @author Roger_Luo
 */
@Slf4j
@Validated
@Tag(name = "测试")
@RestController
@RequestMapping(value = "/demo", produces = { MediaType.APPLICATION_JSON_VALUE })
public class DemoController {

  @Opslog
  @Operation(summary = "没参数", description = "测试没参数场景")
  @GetMapping("/testEmptyArg")
  public String testEmptyArg() {
    return "testEmptyArg";
  }


  @Opslog(tenant = "住户", operator = "operator", module = "测试模块", type = "test", condition = "true",
    success = "成功的信息",
    failure = "失败的 message",
    detail = "详情",
    before = "执行前",
    after = "after 执行",
    bizData = "data")
  @Operation(summary = "全参数", description = "测试全参数场景")
  @GetMapping("/testFullArgs")
  public String testFullArgs() {
    return "testFullArgs";
  }

}
