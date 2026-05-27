package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import thinkunderstar.puretalk.puretalkbackend.common.DoHandleReport;
import thinkunderstar.puretalk.puretalkbackend.common.DoReport;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysReportService;

@RestController
@RequestMapping("/report")
public class ReportController {
    private final SysReportService sysReportService;

    public ReportController(SysReportService sysReportService) {
        this.sysReportService = sysReportService;
    }

    /**
     * 举报帖子或评论
     *
     * @param doReport 举报体
     * @return 举报结果
     */
    @PostMapping("/send")
    @SaCheckLogin
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
    @SaCheckLogin
    @SaCheckRole(value = {"admin","root"},mode = SaMode.OR)
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
    @SaCheckLogin
    @SaCheckRole(value = {"admin","root"},mode = SaMode.OR)
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
    @SaCheckLogin
    @SaCheckRole(value = {"admin","root"},mode = SaMode.OR)
    public Result handleReport(@RequestBody DoHandleReport doHandleReport){
        return sysReportService.handleReport(doHandleReport);
    }

    /**
     * 一年清理一次已处理的举报
     */
    @Scheduled(cron = "0 0 0 1 1 ?")     // 每年1月1日零点
    @Async
    public void clearHandledReport(){
        sysReportService.clearHandledReport();
    }
}
