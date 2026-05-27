package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thinkunderstar.puretalk.puretalkbackend.common.*;
import thinkunderstar.puretalk.puretalkbackend.entity.Comment;
import thinkunderstar.puretalk.puretalkbackend.entity.Notification;
import thinkunderstar.puretalk.puretalkbackend.entity.Post;
import thinkunderstar.puretalk.puretalkbackend.entity.Report;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.mapper.ReportMapper;
import thinkunderstar.puretalk.puretalkbackend.service.*;
import thinkunderstar.puretalk.puretalkbackend.util.RedisTokenBucketLimiter;

@Slf4j
@Service
public class SysReportServiceImpl implements SysReportService {
    private final ReportService reportService;
    private final ReportMapper reportMapper;
    private final SysCommentService sysCommentService;
    private final SysPostService sysPostService;
    private final SysAdminService sysAdminService;
    private final PostService postService;
    private final CommentService commentService;
    private final NotificationService notificationService;
    private final RedisTokenBucketLimiter redisTokenBucketLimiter;

    public SysReportServiceImpl(ReportService reportService, ReportMapper reportMapper, SysCommentService sysCommentService, SysPostService sysPostService, SysAdminService sysAdminService, PostService postService, CommentService commentService, NotificationService notificationService, RedisTokenBucketLimiter redisTokenBucketLimiter) {
        this.reportService = reportService;
        this.reportMapper = reportMapper;
        this.sysCommentService = sysCommentService;
        this.sysPostService = sysPostService;
        this.sysAdminService = sysAdminService;
        this.postService = postService;
        this.commentService = commentService;
        this.notificationService = notificationService;
        this.redisTokenBucketLimiter = redisTokenBucketLimiter;
    }

    @Override
    public Result sendReport(DoReport doReport) {
        if (!(doReport.getReportType() == 1 || doReport.getReportType() == 2)){
            throw new BusinessException("举报业务异常");
        }

        //用户限流
        boolean success = redisTokenBucketLimiter.tryAcquireByUser(StpUtil.getLoginIdAsString(), 3, 1);

        if (!success){
            throw new BusinessException("举报过于频繁");
        }

        Report report = new Report(StpUtil.getLoginIdAsLong(),doReport.getReportType(),doReport.getTargetId(),doReport.getTargetUserId(),doReport.getReason(),0);
        reportService.save(report);

        return Result.success();
    }

    @Override
    public Result getReports(int page, int size) {
        Page<Report> reportPage = new Page<>(page,size);

        LambdaQueryWrapper<Report> lambdaQueryWrapper = new LambdaQueryWrapper<Report>()
                .eq(Report::getStatus,0)
                .orderByAsc(Report::getCreateTime);

        Page<Report> pageResult = reportMapper.selectPage(reportPage, lambdaQueryWrapper);

        return Result.success(pageResult);
    }

    @Override
    public Result getReportTarget(long reportId) {
        Report report = reportService.getById(reportId);

        if (report == null){
            throw new BusinessException("该举报信息不存在");
        }

        //举报类型 1-帖子 2-评论
        if (report.getReportType() == 1){
            Post post = postService.getById(report.getTargetId());
            return Result.success(new ReturnReportTarget(1,post));
        }else {
            Comment comment = commentService.getById(report.getTargetId());

            if (comment == null){
                return Result.success(new ReturnReportTarget(2,(CommentTarget) null));
            }else {
                Post post = postService.getById(comment.getPostId());
                return Result.success(new ReturnReportTarget(2, new CommentTarget(post, comment)));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result handleReport(DoHandleReport doHandleReport) {
        Report report = reportService.getById(doHandleReport.getReportId());

        if (report == null){
            throw new BusinessException("该举报不存在");
        }

        //处理状态 0-待处理 1-已处理 2-驳回
        if (report.getStatus() == 1 || report.getStatus() == 2){
            throw new BusinessException("该举报已被处理");
        }

        //添加处理此举报的管理员id
        report.setHandlerId(doHandleReport.getHandlerId());

        //添加处理结果备注
        report.setHandleResult(doHandleReport.getHandleResult());

        //驳回举报
        if (doHandleReport.isDenied()){
            report.setStatus(2);
            reportService.updateById(report);
            //添加到用户通知
            notificationService.save(new Notification(report.getReportUserId(),1, report.getId()));
            return Result.success();
        }

        //处理举报
        report.setStatus(1);

        if (doHandleReport.isDeleted()){
            //举报类型 1-帖子 2-评论
            if(report.getReportType() == 1){
                sysPostService.deletePost(report.getTargetId());
            }else {
                sysCommentService.deleteComment(report.getTargetId());
            }
        }

        if (doHandleReport.isBanned()){
            sysAdminService.banUser(new BanUser(report.getTargetUserId(),doHandleReport.getBanReason(), doHandleReport.getBanTime()));
        }

        reportService.updateById(report);

        //添加到用户通知
        notificationService.save(new Notification(report.getReportUserId(),1, report.getId()));

        return Result.success();
    }

    @Override
    public void clearHandledReport() {
        log.info("开始清除上一年已处理的举报请求");
        reportService.remove(new LambdaQueryWrapper<Report>()
                .in(Report::getStatus,1,2));
        log.info("清理完成");
    }
}
