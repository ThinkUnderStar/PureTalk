package thinkunderstar.puretalk.puretalkbackend.common;

import lombok.Data;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;

@Data
public class PostVO extends Post {
    //新增返回值
    private String userName;
    private String avatar;
}
