package thinkunderstar.puretalk.puretalkbackend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import thinkunderstar.puretalk.puretalkbackend.common.DoPostMessage;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.service.SysAiService;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final SysAiService sysAiService;

    public AiController(SysAiService sysAiService) {
        this.sysAiService = sysAiService;
    }

    /**
     * 获取AI对话列表
     *
     * @return 分页返回对话列表
     */
    @GetMapping("/sessions")
    @SaCheckLogin
    public Result getSessions(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return sysAiService.getSessions(page, size);
    }

    /**
     * 获取单个对话内容
     *
     * @param sessionId 会话ID
     * @param page      页数
     * @param size      每页最大查询数
     * @return 分页结果
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @SaCheckLogin
    public Result getMessages(@PathVariable long sessionId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return sysAiService.getMessages(sessionId, page, size);
    }

    /**
     * 删除此AI会话
     *
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/sessions/{sessionId}/delete")
    @SaCheckLogin
    public Result deleteSession(@PathVariable long sessionId) {
        return sysAiService.deleteSession(sessionId);
    }

    /**
     * 调用python服务端与大模型进行交互
     *
     * @param doPostMessage 与大模型的交互内容
     * @return AI交互返回的结果
     */
    @PostMapping("/sessions/messages/post")
    public SseEmitter postMessage(@RequestBody DoPostMessage doPostMessage) {
        return sysAiService.postMessage(doPostMessage);
    }

}