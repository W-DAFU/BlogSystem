package com.duan.blogos.api.blogger;

import com.alibaba.dubbo.config.annotation.Reference;
import com.duan.blogos.annonation.TokenNotRequired;
import com.duan.blogos.annonation.Uid;
import com.duan.blogos.api.BaseController;
import com.duan.blogos.service.OnlineService;
import com.duan.blogos.service.blogger.BloggerAccountService;
import com.duan.blogos.service.common.dto.LoginResultDTO;
import com.duan.blogos.service.common.dto.blogger.BloggerAccountDTO;
import com.duan.blogos.service.common.restful.ResultModel;
import com.duan.blogos.service.common.util.Utils;
import com.duan.blogos.service.common.vo.LoginVO;
import com.duan.blogos.util.CodeMessage;
import com.duan.blogos.util.ExceptionUtil;
import com.duan.blogos.util.Util;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;

/**
 * Created on 2018/1/17.
 * 博主账号api
 *
 * @author DuanJiaNing
 */
@RestController
@RequestMapping("/blogger")
public class BloggerController extends BaseController {

    @Reference
    private BloggerAccountService accountService;

    @Reference
    private OnlineService onlineService;

    @PostMapping("/login/way=name")
    @TokenNotRequired
    public ResultModel loginWithUserName(@RequestParam String username,
                                         @RequestParam String password) {
        LoginVO vo = new LoginVO();
        vo.setUsername(username);
        vo.setPassword(password);

        ResultModel<LoginResultDTO> login = onlineService.login(vo);
        LoginResultDTO data = login.getData();
        if (login.isSuccess() && data != null) {
            data.setUsernameBase64(Utils.encodeUrlBase64(data.getUsername()));

            Cookie tokenCookie = new Cookie("token", data.getToken());
            tokenCookie.setPath("/");
            Util.getServletResponse().addCookie(tokenCookie);
        }

        return login;
    }

    @TokenNotRequired
    @RequestMapping(value = "/login/way=phone", method = RequestMethod.POST)
    public ResultModel loginWithPhoneNumber(@RequestParam("phone") String phone) {

        // UPDATE: 2018/9/23 更新
        /*
        handlePhoneCheck(phone, request);

        BloggerAccountDTO account = accountService.getAccountByPhone(phone);
        if (account == null) return new ResultModel<>("", ResultModel.FAIL);

        HttpSession session = request.getSession();
        session.setAttribute(sessionProperties.getBloggerId(), account.getId());
        session.setAttribute(sessionProperties.getBloggerName(), account.getUsername());
        session.setAttribute(sessionProperties.getLoginSignal(), "login");
*/
        // 成功登录
        return ResultModel.fail();
    }

    /**
     * 登出
     */
    @GetMapping("/logout")
    public ResultModel logout(@Uid Long uid) {
        return onlineService.logout(uid);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    @TokenNotRequired
    public ResultModel register(
            @RequestParam String username,
            @RequestParam String password) {

        handleNameCheck(username);
        if (!bloggerValidateService.checkPassword(password)) {
            throw ExceptionUtil.get(CodeMessage.COMMON_PARAMETER_ILLEGAL);
        }

        Long id = accountService.insertAccount(username, password);
        if (id == null)
            return handlerOperateFail();

        return ResultModel.success(id);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/account/check=username")
    @TokenNotRequired
    public ResultModel checkUsernameUsed(
            @RequestParam String username) {
        handleNameCheck(username);

        BloggerAccountDTO account = accountService.getAccount(username);
        if (account != null) {
            return ResultModel.fail("username has been occupied", CodeMessage.COMMON_DUPLICATION_DATA.getCode());
        } else {
            return ResultModel.success();
        }

    }

    /**
     * 检查电话号码是否存在
     */
    @GetMapping("/account/check=phone")
    @TokenNotRequired
    public ResultModel checkProfileExist(
            @RequestParam String phone) {

        BloggerAccountDTO account = accountService.getAccountByPhone(phone);

        if (account != null) {
            return ResultModel.fail("phone number has been occupied", CodeMessage.COMMON_DUPLICATION_DATA.getCode());
        } else {
            return ResultModel.success();
        }

    }

    /**
     * 修改用户名
     */
    @PutMapping("/account/item=name")
    public ResultModel modifyUsername(@Uid Long uid,
                                      @RequestParam String username) {
        handleNameCheck(username);

        boolean result = accountService.updateAccountUserName(uid, username);
        if (!result)
            return handlerOperateFail();

        return ResultModel.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/account/item=pwd")
    public ResultModel modifyPassword(@Uid Long uid,
                                      @RequestParam(value = "old") String oldPassword,
                                      @RequestParam(value = "new") String newPassword) {

        if (!bloggerValidateService.checkPassword(newPassword)) {
            throw ExceptionUtil.get(CodeMessage.COMMON_PARAMETER_ILLEGAL);
        }

        boolean result = accountService.updateAccountPassword(uid, oldPassword, newPassword);
        if (!result)
            return handlerOperateFail();

        return ResultModel.success();
    }

    /**
     * 注销账号
     */
    @DeleteMapping("/account")
    public ResultModel delete(@Uid Long uid) {

        onlineService.logout(uid);

        boolean result = accountService.deleteAccount(uid);
        if (!result)
            return handlerOperateFail();

        return ResultModel.success();
    }

}