package thinkunderstar.puretalk.puretalkbackend.service;

import thinkunderstar.puretalk.puretalkbackend.common.DoHandleReport;
import thinkunderstar.puretalk.puretalkbackend.common.DoReport;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

public interface SysReportService {
    /**
     * 举报帖子或评论
     *
     * @param doReport 举报体
     * @return 举报结果
     */
    Result sendReport( DoReport doReport);

    /**
     * 获取举报信息
     *
     * @param page 页数
     * @param size 一次获取的数量
     * @return 获取结果
     */
    Result getReports(int page, int size);

    /**
     * 获取举报目标
     *
     * @param reportId 举报id
     * @return 获取结果
     */
    Result getReportTarget( long reportId);

    /**
     * 处理举报
     *
     * @param doHandleReport 处理体
     * @return 处理提交结果
     */
    Result handleReport(DoHandleReport doHandleReport);

    /**
     * 一年清理一次已处理的举报
     */
    void clearHandledReport();
}
