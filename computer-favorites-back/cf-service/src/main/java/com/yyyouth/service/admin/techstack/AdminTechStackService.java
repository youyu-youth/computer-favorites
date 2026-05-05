package com.yyyouth.service.admin.techstack;

import com.yyyouth.model.dto.admin.AdminTechStackBatchDeleteDTO;
import com.yyyouth.model.dto.admin.AdminTechStackCreateDTO;
import com.yyyouth.model.dto.admin.AdminTechStackEditDTO;
import com.yyyouth.model.dto.admin.AdminTechStackQueryDTO;
import com.yyyouth.model.vo.admin.AdminTechStackIconUploadVO;
import com.yyyouth.model.vo.admin.AdminTechStackPageVO;
import com.yyyouth.model.vo.admin.AdminTechStackStatsVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈服务
 */
public interface AdminTechStackService {

    /**
     * 查询技术栈分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    AdminTechStackPageVO queryTechStackPage(AdminTechStackQueryDTO queryDTO);

    /**
     * 查询技术栈统计
     *
     * @return 技术栈统计
     */
    AdminTechStackStatsVO queryTechStackStats();

    /**
     * 创建技术栈
     *
     * @param createDTO 创建参数
     * @return 技术栈ID
     */
    Long createTechStack(AdminTechStackCreateDTO createDTO);

    /**
     * 编辑技术栈
     *
     * @param id 技术栈ID
     * @param editDTO 编辑参数
     */
    void editTechStack(Long id, AdminTechStackEditDTO editDTO);

    /**
     * 更新技术栈状态
     *
     * @param id 技术栈ID
     * @param status 目标状态
     */
    void updateTechStackStatus(Long id, Integer status);

    /**
     * 删除技术栈（逻辑删除）
     *
     * @param id 技术栈ID
     */
    void deleteTechStack(Long id);

    /**
     * 批量删除技术栈（逻辑删除）
     *
     * @param batchDeleteDTO 批量参数
     * @return 删除数量
     */
    int batchDeleteTechStacks(AdminTechStackBatchDeleteDTO batchDeleteDTO);

    /**
     * 上传技术栈图标
     *
     * @param file 图标文件
     * @return 上传结果
     */
    AdminTechStackIconUploadVO uploadTechStackIcon(MultipartFile file);

    /**
     * 删除技术栈图标
     *
     * @param objectKey 对象键
     */
    void deleteTechStackIcon(String objectKey);
}
