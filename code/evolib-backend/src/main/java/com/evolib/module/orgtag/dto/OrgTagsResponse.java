package com.evolib.module.orgtag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName OrgTagsResponse
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:23
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrgTagsResponse {
	
	private String number;
	
	private String name;
	
	private String state;
	
	private LocalDateTime createTime;
	
	private LocalDateTime modifyTime;
}
