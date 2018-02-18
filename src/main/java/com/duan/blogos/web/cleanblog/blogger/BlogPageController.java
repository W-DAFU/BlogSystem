package com.duan.blogos.web.cleanblog.blogger;

import com.duan.blogos.dto.blogger.BloggerStatisticsDTO;
import com.duan.blogos.entity.blogger.BloggerAccount;
import com.duan.blogos.entity.blogger.BloggerProfile;
import com.duan.blogos.manager.properties.BloggerProperties;
import com.duan.blogos.restful.ResultBean;
import com.duan.blogos.service.blogger.BloggerAccountService;
import com.duan.blogos.service.blogger.BloggerStatisticsService;
import com.duan.blogos.service.blogger.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created on 2018/2/5.
 *
 * @author DuanJiaNing
 */
@Controller
@RequestMapping("/{bloggerName}")
public class BlogPageController {

    @Autowired
    private BloggerAccountService accountService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private BloggerStatisticsService statisticsService;

    @Autowired
    private BloggerProperties bloggerProperties;

    @RequestMapping("/archives")
    public ModelAndView mainPage(@PathVariable String bloggerName) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("blogger/main");

        BloggerAccount account = accountService.getAccount(bloggerName);
        if (account == null) {
            mv.setViewName("error/unknown_blogger");
            mv.addObject(bloggerProperties.getSessionNameOfErrorMsg(), "博主不存在！");
            return mv;
        }

        mv.addObject(bloggerProperties.getNameOfPageOwnerBloggerId(), account.getId());
        mv.addObject(bloggerProperties.getNameOfPageOwnerBloggerName(), account.getUsername());

        int id = account.getId();
        BloggerProfile profile = profileService.getBloggerProfile(id);
        mv.addObject("blogName", profile.getIntro());
        mv.addObject("aboutMe", profile.getAboutMe());

        ResultBean<BloggerStatisticsDTO> statistics = statisticsService.getBloggerStatistics(account.getId());
        mv.addObject("statistics", statistics.getData());

        return mv;
    }

}