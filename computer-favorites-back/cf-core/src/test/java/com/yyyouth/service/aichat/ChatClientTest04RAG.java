package com.yyyouth.service.aichat;


import cn.hutool.core.lang.UUID;

import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.service.aichat.demo.domain.WebsiteTest;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import io.pinecone.clients.Pinecone;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/05
 */
@SpringBootTest(classes = com.yyyouth.core.ComputerFavoritesApplication.class,
        properties = {"spring.rabbitmq.listener.simple.auto-startup=false"})
public class ChatClientTest04RAG {

    @Resource(name = "dashscopeChatClient")
    ChatClient dashscopeChatClient;


    @Resource(name = "deepseekChatClient")
    ChatClient deepseekChatClient;

    @Resource(name = "nolChatClient")
    ChatClient nolChatClient;

    @Resource
    AsyncMcpToolCallbackProvider mcpToolCallbackProvider;

//    @Value("${cf.ai.weaviate.endpoint}")
//    private String weaviateUrl;
//    @Value("${cf.ai.weaviate.api-key}")
//    private String weaviateApiKey;


    @Resource
    VectorStore vectorStore;

    @Resource
    WebsiteMapper websiteMapper;




    @Test
    public void test05(){
        String result = nolChatClient.prompt().user("帮我找一下vue.js 网站的有关标签")
                .call().chatResponse().getResult().getOutput().getText();
        System.out.println("result : "+result);
    }

    @Test
    public void test04(){
        String result = nolChatClient.prompt().user("你可以帮我找一下浏览量最高的网站吗?也就是点击量最高的网站")
                .call().chatResponse().getResult().getOutput().getText();
        System.out.println("result : "+result);
    }


    @Test
    public void test03() {
        List<Website> websites = websiteMapper.selectList(null).subList(0, 15);
        Map.of("k1", "v1", "k2", "v2");
        websites.stream().forEach(website -> {



            Map<String,Object> metadata = new HashMap<>();
            metadata.put("name", website.getName());
            metadata.put("url", website.getUrl());
            metadata.put("description", website.getDescription());
            metadata.put("summary", website.getSummary());
            metadata.put("categoryId", website.getCategoryId());
            metadata.put("clickCount", website.getClickCount());
            metadata.put("likeCount", website.getLikeCount());
            metadata.put("collectCount", website.getCollectCount());
            metadata.put("commentCount", website.getCommentCount());
            metadata.put("score", website.getScore());
            metadata.put("tags", website.getTags());
            metadata.put("isTop", website.getIsTop());
            metadata.put("isRecommend", website.getIsRecommend());
            metadata.put("status", website.getStatus());


            Document document = Document.builder()
                    .id(website.getId().toString())
                    .text(website.getName())
                    .metadata(metadata)
                    .build();

            vectorStore.add(List.of(document));

        });

    }


    @Test
    public void test02() {

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("百度的作者是?")
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build());
        System.out.println("documents==>" + documents);

    }


    /**
     * 测试向量数据库连接
     */
    @Test
    public void test01() throws Exception {


        WebsiteTest website = WebsiteTest.builder()
                .id(UUID.fastUUID().toString())
                .name("百度")
                .url("https://www.baidu.com")
                .description("百度是一个中国最大的搜索引擎")
                .author("百度")
                .build();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("url", website.getUrl());
        metadata.put("description", website.getDescription());
        metadata.put("author", website.getAuthor());

        Document document = new Document(website.getId(), website.getName(), metadata);


        //测试
        vectorStore.add(List.of(document));


        System.out.println("向量数据库连接成功：" + document.getText());

//        // Best practice: store your credentials in environment variables
//        WeaviateClient client = null;
//        try {
//             client = WeaviateClient.connectToWeaviateCloud(
//                    weaviateUrl, // Replace with your Weaviate Cloud URL
//                    weaviateApiKey // Replace with your Weaviate Cloud key
//            );
//            System.out.println(" 向量数据库连接成功了吗?   "+ client.isReady()); // Should print: `True`
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            client.close(); // Free up resources*/
//        }
    }


    @Test
    public void listMcpTools() {
        ToolCallback[] toolCallbacks = mcpToolCallbackProvider.getToolCallbacks();
        System.out.println("MCP tools size：" + toolCallbacks.length);
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println("工具名称：" + toolCallback.getToolDefinition().name());
            System.out.println("工具描述：" + toolCallback.getToolDefinition().description());
            System.out.println("工具参数：" + toolCallback.getToolDefinition().inputSchema());
        }
        Assertions.assertTrue(toolCallbacks.length > 0, "未发现 MCP 工具，请检查 spring.ai.mcp.client 配置和 Tavily 连接");
    }
}
