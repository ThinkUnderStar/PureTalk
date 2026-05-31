package thinkunderstar.puretalk.puretalkbackend.api;

import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoHandleReport;
import thinkunderstar.puretalk.puretalkbackend.common.DoReport;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysReportService;

@RestController
@RequestMapping("/api/report")
public class ReportAPI {
    private final SysReportService sysReportService;

    public ReportAPI(SysReportService sysReportService) {
        this.sysReportService = sysReportService;
    }

    /**
     * 举报帖子或评论
     *
     * @param doReport 举报体
     * @return 举报结果
     */
    @PostMapping("/send")
    public Result sendReport(@RequestBody DoReport doReport){
        return sysReportService.sendReport(doReport);
    }

    /**
     * 获取举报信息
     *
     * @param page 页数
     * @param size 一次获取的数量
     * @return 获取结果
     */
    @GetMapping
    public Result getReports(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size){
        return sysReportService.getReports(page,size);
    }

    /**
     * 获取举报目标
     *
     * @param reportId 举报id
     * @return 获取结果
     */
    @GetMapping("/{reportId}/target")
    public Result getReportTarget(@PathVariable long reportId){
        return sysReportService.getReportTarget(reportId);
    }

    /**
     * 处理举报
     *
     * @param doHandleReport 处理体
     * @return 处理提交结果
     */
    @PutMapping("/handle")
    public Result handleReport(@RequestBody DoHandleReport doHandleReport){
        return sysReportService.handleReport(doHandleReport);
    }
}
