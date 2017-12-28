package com.duan.blogos.web.api.blogger;

import com.duan.blogos.dto.blogger.BloggerLinkDTO;
import com.duan.blogos.enums.BloggerPictureCategoryEnum;
import com.duan.blogos.result.ResultBean;
import com.duan.blogos.service.blogger.profile.GalleryService;
import com.duan.blogos.service.blogger.profile.LinkService;
import com.duan.blogos.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created on 2017/12/28.
 * 博主友情链接api
 *
 * @author DuanJiaNing
 */
@RestController
@RequestMapping("/blogger/{bloggerId}/link")
public class BloggerLinkController extends BaseBloggerController {

    @Autowired
    private LinkService linkService;

    @Autowired
    private GalleryService galleryService;

    /**
     * 获取链接
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResultBean<List<BloggerLinkDTO>> get(HttpServletRequest request,
                                                @PathVariable Integer bloggerId,
                                                @RequestParam("offset") Integer offset,
                                                @RequestParam("rows") Integer rows) {

        checkAccount(request, bloggerId);

        int os = offset == null || offset < 0 ? 0 : offset;
        int rs = rows == null || rows < 0 ? 0 : bloggerPropertiesManager.getRequestBloggerLinkCount();
        ResultBean<List<BloggerLinkDTO>> result = linkService.listBloggerLink(bloggerId, os, rs);
        if (result == null) handlerEmptyResult(new RequestContext(request));

        return result;
    }

    /**
     * 新增链接
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResultBean newLink(HttpServletRequest request,
                              @PathVariable Integer bloggerId,
                              @RequestParam(value = "iconId", required = false) Integer iconId,
                              @RequestParam("title") String title,
                              @RequestParam("url") String url,
                              @RequestParam(value = "bewrite", required = false) String bewrite) {
        checkAccount(request, bloggerId);
        RequestContext context = new RequestContext(request);

        //检查图片是否存在
        if (iconId != null && iconId > 0 && !galleryService.getPictureForCheckExist(iconId,
                BloggerPictureCategoryEnum.BLOGGER_LINK_ICON.getCode())) {
            throw exceptionManager.getUnknownPictureException(context);
        }

        //检查title和url规范
        if (StringUtils.isEmpty(title) || !StringUtils.isURL(url))
            throw exceptionManager.getParameterIllegalException(context);

        int id = linkService.insertBloggerLink(bloggerId, iconId == null ? -1 : iconId, title, url, bewrite);
        if (id <= 0) handlerOperateFail(request);

        return new ResultBean<>(id);
    }

    //检查链接是否存在
    private void checkLink(Integer linkId, RequestContext context) {
        if (linkId == null || linkId <= 0 || !linkService.getLinkForCheckExist(linkId,
                BloggerPictureCategoryEnum.BLOGGER_LINK_ICON.getCode())) {
            throw exceptionManager.getParameterIllegalException(context);
        }
    }

    /**
     * 更新链接
     */
    @RequestMapping(value = "/{linkId}", method = RequestMethod.POST)
    public ResultBean update(HttpServletRequest request,
                             @PathVariable Integer bloggerId,
                             @PathVariable Integer linkId,
                             @RequestParam("iconId") Integer newIconId,
                             @RequestParam("title") String newTitle,
                             @RequestParam("url") String newUrl,
                             @RequestParam("bewrite") String newBewrite) {
        checkAccount(request, bloggerId);
        RequestContext context = new RequestContext(request);
        checkLink(linkId, context);

        //检查图片是否存在
        if (newIconId != null && newIconId > 0 && !galleryService.getPictureForCheckExist(newIconId,
                BloggerPictureCategoryEnum.BLOGGER_LINK_ICON.getCode())) {
            throw exceptionManager.getUnknownPictureException(context);
        }

        //检查url规范
        if (!StringUtils.isURL(newUrl))
            throw exceptionManager.getParameterIllegalException(context);

        boolean result = linkService.updateBloggerLink(linkId, -1, newIconId == null ? -1 : newIconId, newTitle, newUrl, newBewrite);
        if (!result) handlerOperateFail(request);

        return new ResultBean<>("");
    }

    /**
     * 删除链接
     */
    @RequestMapping(value = "/{linkId}", method = RequestMethod.DELETE)
    public ResultBean delete(HttpServletRequest request,
                             @PathVariable Integer bloggerId,
                             @PathVariable Integer linkId) {
        checkAccount(request, bloggerId);
        RequestContext context = new RequestContext(request);
        checkLink(linkId, context);

        boolean result = linkService.deleteBloggerLink(linkId);
        if (!result) handlerOperateFail(request);

        return new ResultBean<>("");
    }


}
