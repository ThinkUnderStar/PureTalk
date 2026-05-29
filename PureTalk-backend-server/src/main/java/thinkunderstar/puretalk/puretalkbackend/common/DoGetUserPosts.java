package thinkunderstar.puretalk.puretalkbackend.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;

@Data
public class DoGetUserPosts {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户头像链接
     */
    private String avatar;
    /**
     * 用户帖子数据
     */
    private Page<Post>  posts;
}
