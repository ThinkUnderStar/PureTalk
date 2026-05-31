package thinkunderstar.puretalk.puretalkbackend.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysRootService;

@RestController
@RequestMapping("/api/root")
public class RootAPI {
    private final SysRootService sysRootService;

    public RootAPI(SysRootService sysRootService) {
        this.sysRootService = sysRootService;
    }

    /**
     * 检查并执行AI传过来的查询sql
     *
     * @param sql AI传会来的sql语句
     * @return 查询结果
     */
    @PostMapping("/execute-sql")
    public Result executeSql(@RequestBody String sql) {
        return sysRootService.executeSql(sql);
    }
}
