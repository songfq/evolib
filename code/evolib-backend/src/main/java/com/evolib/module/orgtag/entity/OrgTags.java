package com.evolib.module.orgtag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName OrgTags
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:10
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@TableName("t_org_tags")
public class OrgTags {
	@TableId(type = IdType.AUTO)
	private Long id;
	
	private String number;
	
	private String name;
	
	private String state;
	
	private LocalDateTime createTime;
	
	private LocalDateTime modifyTime;
}
