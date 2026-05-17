package com.yyyouth.service.aichat;



import com.yyyouth.service.aichat.demo.domain.ChatResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.PromptTemplateMessageActions;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/04
 */
@SpringBootTest(classes = com.yyyouth.core.ComputerFavoritesApplication.class)
public class ChatClientTest01 {


    @Resource(name = "dashscopeChatClient")
    ChatClient dashscopeChatClient;


    @Resource(name = "deepseekChatClient")
    ChatClient deepseekChatClient;





    @Test
    public void test07() {
        String content = dashscopeChatClient.prompt()
                .user("你好，我叫yyyouth！")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"1"))
                .call()
                .content();
        System.out.println(content);
        System.out.println("--------------------------------------------------------------------------");

        content = dashscopeChatClient.prompt()
                .user("我叫什么 ？")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,"1"))
                .call()
                .content();
        System.out.println(content);
    }




    /**
     * 上传图片测试  测试多模态的能力
     */
    @Test
    public void test06() {
        String response = deepseekChatClient.prompt()
                .user(u ->
                        u.text("Explain what do you see on this picture?")
                                .media(MimeTypeUtils.IMAGE_JPEG, new ClassPathResource("/file/img/lufei.jpg"))
                )
                .call()
                .content();
        System.out.println("res === " + response);
    }


    @Test
    public void test05() {
        // 方法1：
     /*   Map<String, Object> result = chatClient
                .prompt()
                .user(u -> u.text("Provide me a List of {subject}")
                        .param("subject", "an array of numbers from 1 to 9 under they key name 'numbers'"))
                .call()
                .entity(new ParameterizedTypeReference<Map<String, Object>>() {});

        System.out.println("result = "+result);*/

        // 方法2：
        MapOutputConverter mapOutputConverter = new MapOutputConverter();

        String format = mapOutputConverter.getFormat();
        String template = """
                Provide me a List of {subject}
                {format}
                """;

        Prompt prompt = PromptTemplate.builder().template(template)
                .variables(Map.of("subject", "an array of numbers from 1 to 9 under they key name 'numbers'", "format", format))
                .build()
                .create();

        Generation generation = dashscopeChatClient.prompt(prompt).call().chatResponse().getResult();

        Map<String, Object> result = mapOutputConverter.convert(generation.getOutput().getText());

        System.out.println("result = " + result);
    }


    @Test
    public void test04() {
        BeanOutputConverter<ChatResult> beanOutputConverter =
                new BeanOutputConverter<>(ChatResult.class);

        String format = beanOutputConverter.getFormat();

        String actor = "Tom Hanks";

        String template = """
                Generate the filmography of 5 movies for {actor}.
                {format}
                """;

        Generation result = dashscopeChatClient.prompt(
                        PromptTemplate.builder()
                                .template(template)
                                .variables(Map.of("actor", actor, "format", format))
                                .build()
                                .create()
                )
                .call()
                .chatResponse()
                .getResult();

        ChatResult chatResult = beanOutputConverter.convert(result.getOutput().getText());

        System.out.println("chatResult = " + chatResult);

    }


    @Test
    public void test03() {
        ChatResult result = dashscopeChatClient.prompt().user("你是谁?").call().entity(ChatResult.class);
        System.out.println("结果：" + result);
    }


    @Test
    public void test02() {
        PromptTemplateMessageActions promptInput = new PromptTemplate("给我讲一个 {storyName} 的童话故事");
        Message message = promptInput.createMessage(Map.of("storyName", "悲伤"));
        String result = dashscopeChatClient.prompt().messages(message).call().chatResponse().getResult().getOutput().getText();
        System.out.println(result);
    }

    @Test
    public void test01() {
        String text = deepseekChatClient.prompt().user("你好,你是什么模型？什么版本?")
                .call().chatResponse().getResult().getOutput().getText();
        System.out.println(text);
    }
}
