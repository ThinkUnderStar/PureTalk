package thinkunderstar.puretalk.puretalkbackend.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import thinkunderstar.puretalk.puretalkbackend.common.DoPostMessage;
import thinkunderstar.puretalk.puretalkbackend.common.Result;

public interface SysAiService {
    /**
     * 获取AI对话列表
     * @return 分页返回对话列表
     */
    Result getSessions(int page,int size);

    /**
     * 获取单个对话内容
     *
     * @param sessionId 会话ID
     * @param page 页数
     * @param size 每页最大查询数
     * @return 分页结果
     */
    Result getMessages(long sessionId, int page, int size);

    /**
     * 删除此AI会话
     *
     * @param sessionId 会话ID
     * @return 删除结果
     */
    Result deleteSession(long sessionId);

    /**
     * 调用python服务端与大模型进行交互
     *
     * @param doPostMessage 与大模型的交互内容
     * @return AI交互返回的结果
     */
    SseEmitter postMessage(DoPostMessage doPostMessage);
}
