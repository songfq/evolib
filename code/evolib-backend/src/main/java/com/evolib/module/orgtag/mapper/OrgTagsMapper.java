package com.evolib.module.orgtag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolib.module.orgtag.entity.OrgTags;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @ClassName OrgTagsMapper
 * @Description
 * @Author songfq
 * @Date 2026/7/14 10:35
 * @Version 1.0
 * @Copyright:注意：本内容仅限于深圳市莫亚科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Mapper
public interface OrgTagsMapper extends BaseMapper<OrgTags> {
	
	@Select("SELECT * FROM t_org_tags WHERE number = #{number} ")
	Object select(@Param("number") String number);
}
