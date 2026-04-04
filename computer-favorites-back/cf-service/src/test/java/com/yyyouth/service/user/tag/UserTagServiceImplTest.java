package com.yyyouth.service.user.tag;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.model.dto.user.UserTagQueryDTO;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.vo.user.UserTagPageVO;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.user.tag.impl.UserTagServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserTagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private UserTagServiceImpl userTagService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, Tag.class);
    }

    /**
     * 查询标签分页列表应返回分页结果
     */
    @Test
    void shouldQueryTagPageSuccessfully() {
        UserTagQueryDTO queryDTO = new UserTagQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setSortField("useCount");
        queryDTO.setSortOrder(-1);

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Vue");
        tag.setColor("#42B883");
        tag.setUseCount(12);

        when(tagMapper.selectCount(any())).thenReturn(1L);
        when(tagMapper.selectList(any())).thenReturn(List.of(tag));

        UserTagPageVO pageVO = userTagService.queryTagPage(queryDTO);

        assertThat(pageVO.getTotal()).isEqualTo(1L);
        assertThat(pageVO.getPageNum()).isEqualTo(1);
        assertThat(pageVO.getPageSize()).isEqualTo(10);
        assertThat(pageVO.getTotalPages()).isEqualTo(1L);
        assertThat(pageVO.getRecords()).hasSize(1);
        assertThat(pageVO.getRecords().get(0).getName()).isEqualTo("Vue");
        assertThat(pageVO.getRecords().get(0).getColor()).isEqualTo("#42B883");
    }

    /**
     * 查询标签分页列表无数据时应返回空列表
     */
    @Test
    void shouldReturnEmptyRecordsWhenNoTagData() {
        UserTagQueryDTO queryDTO = new UserTagQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        when(tagMapper.selectCount(any())).thenReturn(0L);

        UserTagPageVO pageVO = userTagService.queryTagPage(queryDTO);

        assertThat(pageVO.getTotal()).isEqualTo(0L);
        assertThat(pageVO.getRecords()).isEmpty();
        assertThat(pageVO.getTotalPages()).isEqualTo(0L);
    }

    /**
     * useCount 为 0 的标签也应在用户端列表回显
     */
    @Test
    void shouldIncludeTagWhenUseCountIsZero() {
        UserTagQueryDTO queryDTO = new UserTagQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        Tag tag = new Tag();
        tag.setId(2L);
        tag.setName("Spring");
        tag.setColor("#67C23A");
        tag.setUseCount(0);

        when(tagMapper.selectCount(any())).thenReturn(1L);
        when(tagMapper.selectList(any())).thenReturn(List.of(tag));

        UserTagPageVO pageVO = userTagService.queryTagPage(queryDTO);

        assertThat(pageVO.getTotal()).isEqualTo(1L);
        assertThat(pageVO.getRecords()).hasSize(1);
        assertThat(pageVO.getRecords().get(0).getName()).isEqualTo("Spring");
        assertThat(pageVO.getRecords().get(0).getUseCount()).isEqualTo(0);
    }
}