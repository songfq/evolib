package com.evolib.module.borrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolib.module.borrow.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
    
    @Select("SELECT COUNT(*) FROM borrow_records WHERE reader_id = #{readerId} AND status = 'BORROWED' AND due_date < NOW()")
    int countOverdue(@Param("readerId") String readerId);
    
    @Select("SELECT COUNT(*) FROM borrow_records WHERE reader_id = #{readerId} AND status = 'BORROWED'")
    int countCurrentBorrows(@Param("readerId") String readerId);
    
    @Select("SELECT COUNT(*) FROM borrow_records WHERE reader_id = #{readerId} AND isbn = #{isbn} AND status = 'BORROWED'")
    int countDuplicate(@Param("readerId") String readerId, @Param("isbn") String isbn);
}