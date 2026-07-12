package com.evolib.module.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolib.module.book.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    
    @Update("UPDATE books SET available_stock = available_stock - 1 WHERE isbn = #{isbn} AND available_stock > 0")
    int decrementStock(@Param("isbn") String isbn);
    
    @Update("UPDATE books SET available_stock = available_stock + 1 WHERE isbn = #{isbn}")
    int incrementStock(@Param("isbn") String isbn);
    
    @Select("SELECT COUNT(*) FROM books WHERE title LIKE #{keyword} OR author LIKE #{keyword} OR isbn LIKE #{keyword}")
    int countByKeyword(@Param("keyword") String keyword);
}