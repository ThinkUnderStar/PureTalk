package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import thinkunderstar.puretalk.puretalkbackend.common.DoPostMessage;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.AiMessage;
import thinkunderstar.puretalk.puretalkbackend.entity.AiSession;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.mapper.AiMessageMapper;
import thinkunderstar.puretalk.puretalkbackend.mapper.AiSessionMapper;
import thinkunderstar.puretalk.puretalkbackend.service.AiMessageService;
import thinkunderstar.puretalk.puretalkbackend.service.SysAiService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysAiServiceImpl implements SysAiService {
    private final AiSessionServiceImpl aiSessionService;
    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final AiMessageService aiMessageService;
    private final UserService userService;
    private final WebClient webClient;

    public SysAiServiceImpl(AiSessionServiceImpl aiSessionService,
                            AiSessionMapper aiSessionMapper,
                            AiMessageMapper aiMessageMapper,
                            AiMessageService aiMessageService,
                            UserService userService,
                            WebClient webClient) {
        this.aiSessionService = aiSessionService;
        this.aiSessionMapper = aiSessionMapper;
        this.aiMessageMapper = aiMessageMapper;
        this.aiMessageService = aiMessageService;
        this.userService = userService;
        this.webClient = webClient;
    }

    @Override
    public Result getSessions(int page,int size) {
        long userId = StpUtil.getLoginIdAsLong();

        if(!aiSessionService.exists(new LambdaQueryWrapper<AiSession>().eq(AiSession::getUserId,userId))){
            return Result.success(null);
        }

        Page<AiSession> aiSessionPage = new Page<>(page,size);

        LambdaQueryWrapper<AiSession> aiSessionLambdaQueryWrapper =
                new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getUserId, userId)
                .orderByDesc(AiSession::getCreateTime);

        Page<AiSession> resultPage = aiSessionMapper.selectPage(aiSessionPage, aiSessionLambdaQueryWrapper);

        return Result.success(resultPage);
    }

    @Override
    public Result getMessages(long sessionId, int page, int size) {
        //判断会话是否存在
        AiSession aiSession = aiSessionService.getById(sessionId);
        if (aiSession == null){
            throw new BusinessException("该会话不存在");
        }
        Long userId = aiSession.getUserId();
        if(StpUtil.getLoginIdAsLong() != userId){
            throw new BusinessException("该会话不存在");
        }

        Page<AiMessage> aiMessagePage = new Page<>(page,size);

        LambdaQueryWrapper<AiMessage> aiMessageLambdaQueryWrapper =
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, sessionId)
                        .orderByDesc(AiMessage::getCreateTime);

        Page<AiMessage> resultPage = aiMessageMapper.selectPage(aiMessagePage, aiMessageLambdaQueryWrapper);

        return Result.success(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteSession(long sessionId) {
        //判断会话是否存在
        AiSession aiSession = aiSessionService.getById(sessionId);
        if (aiSession == null){
            throw new BusinessException("该会话不存在");
        }
        Long userId = aiSession.getUserId();
        if(StpUtil.getLoginIdAsLong() != userId){
            throw new BusinessException("该会话不存在");
        }

        aiMessageService.remove(new LambdaQueryWrapper<AiMessage>().eq(AiMessage::getSessionId,sessionId));
        aiSessionService.removeById(sessionId);

        return Result.success("删除会话成功");
    }

    @Override
    public SseEmitter postMessage(DoPostMessage doPostMessage) {
        AiSession aiSession;
        List<Map<String,String>> context = new ArrayList<>();

        //获取当前会话对象
        if (doPostMessage.getSessionId() == 0){
            //新建对话的场景
            aiSession = new AiSession();
            aiSession.setUserId(StpUtil.getLoginIdAsLong());

            //设置标题
            String title = doPostMessage.getMessage();
            if (title.length() > 12) {
                title = title.substring(0, 12) + "···";
            }
            aiSession.setTitle(title);

            //会话保存至数据库，以此获取数据库分配的ID
            aiSessionService.save(aiSession);

            //context拼接
            HashMap<String, String> map = new HashMap<>();
            map.put("role","user");
            map.put("content", doPostMessage.getMessage());
            context.add(map);
        }else {
            //已有对话的场景
            //已有对话不存在过滤
            aiSession = aiSessionService.getById(doPostMessage.getSessionId());
            if (aiSession == null){
                throw new BusinessException("该会话不存在");
            }
            Long userId = aiSession.getUserId();
            if(StpUtil.getLoginIdAsLong() != userId){
                throw new BusinessException("该会话不存在");
            }

            //context拼接
            List<AiMessage> list = aiMessageService
                    .list(new LambdaQueryWrapper<AiMessage>()
                            .eq(AiMessage::getSessionId, aiSession.getId())
                            .orderByAsc(AiMessage::getCreateTime));

            list.forEach(aiMessage ->
            {
                HashMap<String, String> map = new HashMap<>();
                map.put("role", aiMessage.getRole());
                map.put("content", aiMessage.getContent());
                context.add(map);
            });

            HashMap<String, String> map = new HashMap<>();
            map.put("role","user");
            map.put("content", doPostMessage.getMessage());
            context.add(map);
        }

        //将用户消息存入数据库
        AiMessage aiMessage = new AiMessage();
        aiMessage.setRole("user");
        aiMessage.setSessionId(aiSession.getId());
        aiMessage.setContent(doPostMessage.getMessage());
        aiMessageService.save(aiMessage);

        //获取用户信息
        User user = userService.getById(StpUtil.getLoginIdAsLong());

        //发送消息
        StringBuilder returnMessage = new StringBuilder();
        SseEmitter emitter = new SseEmitter(0L);


        //第一次对话返回一个事件
        if (doPostMessage.getSessionId() == 0) {
            try {
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data(aiSession.getId()));
            } catch (IOException e) {
                emitter.completeWithError(e);
                return emitter;
            }
        }

        //获取大模型返回的回复
        //请求体准备
        Map<String,Object> body = new HashMap<>();
        body.put("name",user.getUsername());
        body.put("user_id",user.getId());
        body.put("role",user.getRole());
        body.put("status",user.getStatus());
        body.put("context",context);

        //向python端服务发送请求
        webClient.post()
                .uri("/ai/chat/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
                .map(chunk -> {
                    String trimmed = chunk.trim();
                    if (trimmed.startsWith("data:")) {
                        return trimmed.substring(5).trim();
                    }
                    return trimmed;
                })
                .subscribe(
                        //处理每一块获得的数据
                        chunk->{
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }

                            //拼接完整的字符串
                            synchronized (returnMessage) {
                                returnMessage.append(chunk);
                            }
                        },

                        //连接错误处理
                        emitter::completeWithError,

                        //连接结束处理
                        ()->{
                            AiMessage aiReturnMessage = new AiMessage();
                            aiReturnMessage.setRole("assistant");
                            aiReturnMessage.setSessionId(aiSession.getId());
                            aiReturnMessage.setContent(returnMessage.toString());
                            aiMessageService.save(aiReturnMessage);
                            emitter.complete();
                        }
                );

        return emitter;
    }
}
