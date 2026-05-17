package com.yyyouth.service.aichat.agent.tools;

import com.yyyouth.model.pojo.website.Website;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/13 网站相关工具 给 agent 调用的
 */
public class WebsitesTool {


    @Tool(description = "当你需要保存网站到数据库中")
    public void saveWebsite(@ToolParam(description = "网站相关信息实体") Website website) {

    }

    @Tool(description = "当你需要从数据库中删除该网站时")
    public void deleteWebsite(@ToolParam(description = "网站相关信息实体") Website website) {

    }

}
