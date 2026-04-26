package com.yyyouth.service.user.folder;

import com.yyyouth.model.dto.user.UserFolderCreateDTO;
import com.yyyouth.model.dto.user.UserFolderUpdateDTO;
import com.yyyouth.model.vo.user.UserFolderCreateVO;
import com.yyyouth.model.vo.user.UserFolderOptionsVO;
import com.yyyouth.model.vo.user.UserFolderTreeVO;

import java.util.List;

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

    /**
     * 获取当前用户文件夹树
     *
     * @return 文件夹树列表
     */
    List<UserFolderTreeVO> getFolderTree();

    /**
     * 更新收藏文件夹
     *
     * @param folderId 文件夹ID
     * @param updateDTO 更新参数
     */
    void updateFolder(Long folderId, UserFolderUpdateDTO updateDTO);

    /**
     * 删除收藏文件夹
     *
     * @param folderId 文件夹ID
     */
    void deleteFolder(Long folderId);

    /**
     * 切换文件夹隐藏状态
     *
     * @param folderId 文件夹ID
     * @param isHide 是否隐藏
     */
    void toggleFolderHide(Long folderId, boolean isHide);

    /**
     * 获取文件夹下拉选项
     *
     * @return 文件夹选项列表
     */
    List<UserFolderOptionsVO> getFolderOptions();

    /**
     * 验证用户密码
     *
     * @param password 待验证密码
     * @return 是否匹配
     */
    boolean verifyPassword(String password);
}
