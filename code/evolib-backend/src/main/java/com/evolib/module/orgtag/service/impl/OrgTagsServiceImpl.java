package com.evolib.module.orgtag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolib.module.orgtag.dto.OrgTagsRequest;
import com.evolib.module.orgtag.dto.OrgTagsResponse;
import com.evolib.module.orgtag.entity.OrgTags;
import com.evolib.module.orgtag.mapper.OrgTagsMapper;
import com.evolib.module.orgtag.service.OrgTagsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OrgTagsServiceImpl
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:30
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgTagsServiceImpl implements OrgTagsService {
	private final OrgTagsMapper orgTagsMapper;
	
	@Override
	public OrgTagsResponse add(OrgTagsRequest request) {
		
		return null;
	}
	
	@Override
	public OrgTagsResponse save(OrgTagsRequest request) {
		return null;
	}
	
	@Override
	public IPage<Object> getList(Integer pageNum, Integer pageSize) {
		return getList("", pageNum, pageSize);
	}
	
	@Override
	public IPage<Object> getList(String number, Integer pageNum, Integer pageSize) {
		Page<OrgTags> page = new Page<>(pageNum, pageSize);
		
		LambdaQueryWrapper<OrgTags> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(OrgTags::getNumber, number);
		
		IPage<OrgTags> recordPage = orgTagsMapper.selectPage(page, wrapper);
		
		return recordPage.convert(record -> {
			Map<String, Object> result = new HashMap<>();
			result.put("id", record.getId());
			result.put("number", record.getNumber());
			result.put("name", record.getName());
			result.put("state", record.getState());
			result.put("createTime", record.getCreateTime());
			result.put("modifyTime", record.getModifyTime());
			
			return result;
		});
	}
}
