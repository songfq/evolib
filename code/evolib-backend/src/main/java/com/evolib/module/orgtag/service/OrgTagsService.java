package com.evolib.module.orgtag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolib.module.orgtag.dto.OrgTagsRequest;
import com.evolib.module.orgtag.dto.OrgTagsResponse;

/**
 * @ClassName OrgTagsService
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:25
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface OrgTagsService {
	OrgTagsResponse add(OrgTagsRequest request);
	
	OrgTagsResponse save(OrgTagsRequest request);
	
	IPage<Object> getList(Integer pageNum, Integer pageSize);
	
	IPage<Object> getList(String number, Integer pageNum, Integer pageSize);
}
