package com.evolib.module.orgtag.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @ClassName OrgTagsRequest
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:20
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class OrgTagsRequest {
	
	@NotBlank(message = "编码不能为空")
	private String number;
	
	@NotBlank(message = "名称不能为空")
	private String name;
	
	private String state;
	
	private LocalDateTime createTime;
	
	private LocalDateTime modifyTime;
}
