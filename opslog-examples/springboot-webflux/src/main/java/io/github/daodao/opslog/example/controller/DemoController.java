package io.github.daodao.opslog.example.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 专门放置演示用的接口
 *
 * @author Roger_Luo
 */
@RestController
@Validated
@RequestMapping(value = "/demo", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
public class DemoController {


}
