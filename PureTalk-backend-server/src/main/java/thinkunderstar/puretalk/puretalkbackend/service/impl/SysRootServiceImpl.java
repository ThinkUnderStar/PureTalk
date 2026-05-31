package thinkunderstar.puretalk.puretalkbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import thinkunderstar.puretalk.puretalkbackend.common.DeleteData;
import thinkunderstar.puretalk.puretalkbackend.common.DoRegister;
import thinkunderstar.puretalk.puretalkbackend.common.Result;
import thinkunderstar.puretalk.puretalkbackend.entity.User;
import thinkunderstar.puretalk.puretalkbackend.exception.AuthException;
import thinkunderstar.puretalk.puretalkbackend.exception.BusinessException;
import thinkunderstar.puretalk.puretalkbackend.service.SysRootService;
import thinkunderstar.puretalk.puretalkbackend.service.UserService;
import thinkunderstar.puretalk.puretalkbackend.util.DesensitizeUtils;
import thinkunderstar.puretalk.puretalkbackend.util.ValidateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysRootServiceImpl implements SysRootService {
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;

    public SysRootServiceImpl(UserService userService, JdbcTemplate jdbcTemplate) {
        this.userService = userService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Result doAdminRegister(DoRegister doRegister) {
        User user = userService.getOne(new QueryWrapper<User>().eq("phone", doRegister.getPhone()));
        //判断格式是否真确
        if (ValidateUtils.usernameValidate(doRegister.getUsername())
                && ValidateUtils.passwordValidate(doRegister.getPassword())) {
            //认证是否为有效手机号与邮箱
            if (doRegister.isRightPhone() && doRegister.isRightEmail()) {
                //判断用户是否已经存在
                if (user != null) {
                    if (user.getDeleted() == 0) {
                        throw new AuthException("用户已存在");
                    } else {
                        userService.remove(new QueryWrapper<User>().eq("phone", doRegister.getPhone()));

                        userService.save(new User(doRegister.getUsername()
                                , BCrypt.hashpw(doRegister.getPassword(), BCrypt.gensalt(12))
                                , doRegister.getPhone()
                                , doRegister.getEmail()));
                        return Result.success();
                    }
                } else {
                    userService.save(new User(doRegister.getUsername()
                            , BCrypt.hashpw(doRegister.getPassword(), BCrypt.gensalt(12))
                            , doRegister.getPhone()
                            , doRegister.getEmail()
                            ,2));
                    return Result.success();
                }
            } else {
                throw new AuthException("手机或邮箱验证码错误");
            }
        }else {
            throw new AuthException("用户名或密码格式错误");
        }
    }

    @Override
    public Result getDeleteAdminData(String phone) {
        String role;

        if (!ValidateUtils.phoneValidate(phone)) {
            throw new BusinessException("手机号格式有误");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("phone", phone));

        if (user == null) {
            throw new BusinessException("该用户不存在");
        } else {
            if (user.getRole() == 1) {
                role = "用户";
            } else if (user.getRole() == 2) {
                role = "管理员";
            } else if (user.getRole() == 3) {
                throw new BusinessException("root账户不可删除!");
            } else {
                throw new BusinessException("非法账户身份信息");
            }
        }

        return Result.success(new DeleteData(user.getUsername()
                , DesensitizeUtils.desensitizePhone(user.getPhone())
                , DesensitizeUtils.desensitizeEmail(user.getEmail())
                , role
                , user.getCreateTime()));
    }

    @Override
    public Result deleteAdmin(String phone) {
        if (!ValidateUtils.phoneValidate(phone)) {
            throw new BusinessException("手机号格式有误");
        }

        User user = userService.getOne(new QueryWrapper<User>().eq("phone", phone));

        if (user == null) {
            throw new BusinessException("该用户不存在");
        } else if (user.getRole() == 3) {
            throw new BusinessException("root账户不可删除!");
        }

        user.setDeleted(1);
        user.setDeleteTime(LocalDateTime.now());
        user.setWhoDelete(StpUtil.getLoginIdAsLong());
        userService.updateById(user);

        return Result.success();
    }

    @Override
    public Result executeSql(String sql) {
        //0 清除 Markdown 包裹
        sql = sql.trim();

        // 去除Markdown代码块标记 ```
        if (sql.startsWith("```sql")) {
            sql = sql.substring(6);
        } else if (sql.startsWith("```")) {
            sql = sql.substring(3);
        }
        if (sql.endsWith("```")) {
            sql = sql.substring(0, sql.length() - 3);
        }

        // 去除可能包裹整个SQL字符串的外层双引号（JSON序列化导致）
        sql = sql.trim();
        if (sql.startsWith("\"") && sql.endsWith("\"")) {
            sql = sql.substring(1, sql.length() - 1);
        }

        // 1.1 基础过滤：必须是 SELECT 且不含危险关键字
        String trimmed = sql.trim().toUpperCase();
        log.info("AI传来的SQL:{}", trimmed);

        if (!trimmed.startsWith("SELECT")) {
            log.error("无法执行的SQL:{}", trimmed);
            throw new BusinessException("仅允许执行 SELECT 查询语句");
        }

        //1.2二重过滤确保内部无危险的关键词
        String[] keywords = {"DROP", "ALTER", "TRUNCATE", "INSERT", "UPDATE", "DELETE", "REPLACE"};
        for (String keyword : keywords) {
            if (trimmed.contains(keyword)) {
                log.error("无法执行的SQL:{}", trimmed);
                throw new BusinessException("SQL中包含禁止的关键字：" + keyword);
            }
        }

        // 2. 自动限制返回行数
        if (!trimmed.contains("LIMIT")) {
            sql += " LIMIT 100";
        }

        // 3. 执行查询
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return Result.success(result);
        } catch (Exception e) {
            log.error("无法执行的SQL:{}", trimmed);
            throw new BusinessException("SQL执行失败: " + e.getMessage());
        }
    }
}
