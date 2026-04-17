package com.yyyouth.service.user.notification;

import com.yyyouth.model.dto.user.UserFeedbackCreateDTO;
import com.yyyouth.model.vo.user.UserFeedbackImageUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-04-17
 *
 * 用户反馈服务接口
 */
public interface UserFeedbackService {

    /**
     * 上传反馈附图
     *
     * @param file 图片文件
     * @return 上传结果
     */
    UserFeedbackImageUploadVO uploadFeedbackImage(MultipartFile file);

    /**
     * 删除反馈附图
     *
     * @param objectKey 对象键
     */
    void deleteFeedbackImage(String objectKey);

    /**
     * 提交用户反馈
     *
     * @param createDTO 反馈参数
     */
    void submitFeedback(UserFeedbackCreateDTO createDTO);
}
