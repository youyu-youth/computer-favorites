package com.yyyouth.service.user.website;

import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionQueryDTO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionDetailVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionIconUploadVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionPageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿网站服务
 */
public interface UserWebsiteSubmissionService {

    /**
     * 上传投稿网站图标
     *
     * @param file 图标文件
     * @return 上传结果
     */
    UserWebsiteSubmissionIconUploadVO uploadSubmissionIcon(MultipartFile file);

    /**
     * 删除投稿网站图标
     *
     * @param objectKey 对象键
     */
    void deleteSubmissionIcon(String objectKey);

    /**
     * 提交网站投稿
     *
     * @param createDTO 投稿参数
     * @return 网站ID
     */
    Long submitWebsite(UserWebsiteSubmissionCreateDTO createDTO);

    /**
     * 提交网站投稿（显式传递 userId，供 Agent tool 等非 HTTP 线程调用）
     *
     * @param createDTO 投稿参数
     * @param submitterId 提交用户ID
     * @return 网站ID
     */
    Long submitWebsite(UserWebsiteSubmissionCreateDTO createDTO, Long submitterId);

    /**
     * 查询我的投稿分页
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    UserWebsiteSubmissionPageVO queryMySubmissionPage(UserWebsiteSubmissionQueryDTO queryDTO);

    UserWebsiteSubmissionPageVO queryPublicApprovedSubmissionPage(String username, Long currentUserId, Integer limit);

    /**
     * 查询我的投稿详情
     *
     * @param websiteId 网站ID
     * @return 投稿详情
     */
    UserWebsiteSubmissionDetailVO queryMySubmissionDetail(Long websiteId);

    /**
     * 编辑我的投稿
     *
     * @param websiteId 网站ID
     * @param editDTO 编辑参数
     */
    void editMySubmission(Long websiteId, UserWebsiteSubmissionCreateDTO editDTO);

    /**
        * 取消我的投稿
        *
        * @param websiteId 网站ID
        */
        void cancelMySubmission(Long websiteId);

        /**
     * 重新提交我的投稿
     *
     * @param websiteId 网站ID
     */
    void resubmitMySubmission(Long websiteId);
}
