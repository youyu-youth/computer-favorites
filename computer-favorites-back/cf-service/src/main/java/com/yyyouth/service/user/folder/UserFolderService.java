package com.yyyouth.service.user.folder;

import com.yyyouth.model.dto.user.UserFolderCreateDTO;
import com.yyyouth.model.vo.user.UserFolderCreateVO;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏文件夹服务接口
 */
public interface UserFolderService {

    /**
     * 创建收藏文件夹
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    UserFolderCreateVO createFolder(UserFolderCreateDTO createDTO);
}
