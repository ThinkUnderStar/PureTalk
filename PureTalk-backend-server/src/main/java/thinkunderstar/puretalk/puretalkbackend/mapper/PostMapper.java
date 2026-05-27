package thinkunderstar.puretalk.puretalkbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;

import java.util.List;

public interface PostMapper extends BaseMapper<Post> {
    @Select("SELECT *, MATCH(title) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS relevance " +
            "FROM posts " +
            "WHERE MATCH(title) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "ORDER BY relevance DESC")
    List<Post> searchByTitle(@Param("keyword") String keyword);
}
