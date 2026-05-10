package com.yyyouth.service.user.userstats;

import com.yyyouth.model.vo.userstats.PublicFolderChildrenVO;
import com.yyyouth.model.vo.userstats.PublicFolderItemVO;
import com.yyyouth.model.vo.userstats.PublicFolderTreeVO;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-10
 *
 * 公开主页收藏夹查询服务（user-15 公开收藏夹）。
 *
 * <p>隐私链路：
 * <ul>
 *   <li>复用 {@link ProfilePublicService#getPublicProfile} 解析 username → targetUserId、做 visibility 校验，
 *       并取得 isOwn / showCollections 快照。</li>
 *   <li>非本人且 showCollections=0 时抛 {@code PROFILE_PRIVATE}。</li>
 * </ul>
 *
 * <p>单夹可见性叠加规则：
 * <ul>
 *   <li>本人视图：仅过滤 status=1 AND deleted=0，不限 is_public / is_hide。</li>
 *   <li>他人视图：额外过滤 is_public=1 AND is_hide=0；is_hide=1 自动表示对外不可见。</li>
 * </ul>
 *
 * <p>缓存键模板（key 末尾带 own/visitor 后缀避免不同访问者污染）：
 * <ul>
 *   <li>顶层：{@code user:profile:public-folders:{userId}:top:{limit}:{own|visitor}}（TTL 300s ±60s）</li>
 *   <li>子项：{@code user:profile:public-folder-children:{userId}:{folderId}:{pageNum}:{pageSize}:{own|visitor}}（TTL 180s ±30s）</li>
 *   <li>全量树：{@code user:profile:public-folder-tree:{userId}:{own|visitor}}（TTL 300s ±60s）</li>
 * </ul>
 */
public interface PublicFolderService {

    /**
     * 公开主页顶层收藏夹列表（最多 limit 个，过滤"空夹"；本人不过滤）。
     *
     * @param username      目标用户用户名
     * @param currentUserId 当前访问者 ID（未登录传 null）
     * @param limit         最多返回数量（建议 5）
     */
    List<PublicFolderItemVO> getTopFolders(String username, Long currentUserId, int limit);

    /**
     * 公开主页单收藏夹一层子项（子文件夹 + 直属网站分页）。对话框使用。
     *
     * @param folderId 顶层（或任意层）收藏夹 ID
     */
    PublicFolderChildrenVO getFolderChildren(String username, Long currentUserId,
                                             Long folderId, int pageNum, int pageSize);

    /**
     * 公开主页全量公开收藏夹树（"查看全部"页用）。
     */
    PublicFolderTreeVO getPublicFolderTree(String username, Long currentUserId);
}
