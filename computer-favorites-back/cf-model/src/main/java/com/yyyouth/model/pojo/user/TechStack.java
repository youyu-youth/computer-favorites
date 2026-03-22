package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_tech_stack")
public class TechStack {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("status")
    private Integer status;

    @TableField("sort")
    private Integer sort;

    @TableField("deleted")
    private Integer deleted;
}
