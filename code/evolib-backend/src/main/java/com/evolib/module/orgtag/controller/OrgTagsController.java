package com.evolib.module.orgtag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolib.common.Result;
import com.evolib.module.orgtag.service.OrgTagsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName OrgTagsController
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:59
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class OrgTagsController {
	private final OrgTagsService orgTagsService;
	
	@GetMapping("/orgTags/list")
	public Result<IPage<Object>> getList(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
		log.info("查询组织标签列表: pageNum={}, pageSize={}", pageNum, pageSize);
		IPage<Object> list = orgTagsService.getList(pageNum, pageSize);
		return Result.ok(list);
	}
}
