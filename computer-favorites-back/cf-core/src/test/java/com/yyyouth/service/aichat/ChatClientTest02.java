package com.yyyouth.service.aichat;

import cn.hutool.core.lang.UUID;

import com.yyyouth.service.aichat.agent.tools.CalculatorTools;
import com.yyyouth.service.aichat.agent.tools.DateTimeTools;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/05
 */
@SpringBootTest(classes = com.yyyouth.core.ComputerFavoritesApplication.class)
public class ChatClientTest02 {

    @Resource(name = "dashscopeChatClient")
    ChatClient dashscopeChatClient;


    @Resource(name = "deepseekChatClient")
    ChatClient deepseekChatClient;

    @Resource(name = "nolChatClient")
    ChatClient nolChatClient;





    @Test
    public void test05(){

        String result = nolChatClient.prompt()
                .user("6 * 8 + 7 等于多少?")
                .call().chatResponse().getResult().getOutput().getText();

        System.out.println("result = > "+result);

    }





    /**
     * 手动控制工具的执行
     */
    @Test
    public void test04() {

        DefaultToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        String conversationId = UUID.randomUUID().toString();

        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(ToolCallbacks.from(new CalculatorTools()))
                .internalToolExecutionEnabled(false)
                .build();


        Prompt prompt = new Prompt(List.of(
                new SystemMessage("你是一名小助手"),
                new UserMessage("6 * 8 等于多少?")), chatOptions);


        chatMemory.add(conversationId, prompt.getInstructions());


        Prompt promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);


        ChatResponse chatResponse = nolChatClient.prompt(promptWithMemory).call().chatResponse();


        chatMemory.add(conversationId, chatResponse.getResult().getOutput());

        //判断是否有需要工具调用  循环处理工具调用
        while (chatResponse.hasToolCalls()) {
            //如果模型返回了工具调用请求，就用 toolCallingManager 执行工具，
            // 并把结果加入到 chatMemory，再继续调用模型，直到没有工具调用为止。
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(promptWithMemory, chatResponse);
            chatMemory.add(conversationId, toolExecutionResult.conversationHistory().get(toolExecutionResult.conversationHistory().size() - 1));
            //创建新的 Prompt 对象，并设置新的工具调用结果
            promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);
            ChatResponse response = nolChatClient.prompt(promptWithMemory).call().chatResponse();
            chatMemory.add(conversationId, response.getResult().getOutput());
            chatResponse = response;
        }
        System.out.println("第一次："+chatResponse.getResult().getOutput().getText());
        UserMessage newUserMessage = new UserMessage("有更加简单的方法吗？");
        chatMemory.add(conversationId, newUserMessage);
        String text = nolChatClient.prompt(new Prompt(chatMemory.get(conversationId))).call().chatResponse().getResult().getOutput().getText();
        System.out.println("第二次 = "+text);

    }


    @Test
    public void test03() {
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();

        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();

        Prompt prompt = new Prompt("你好，现在什么时间了?");

        ChatResponse chatResponse = dashscopeChatClient
                .prompt(prompt)
                .tools()
                .call().chatResponse();

        while (chatResponse.hasToolCalls()){
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);

            prompt = new Prompt(toolExecutionResult.conversationHistory(),chatOptions);
            chatResponse = dashscopeChatClient.prompt(prompt).call().chatResponse();

        }
        System.out.println(chatResponse.getResult().getOutput().getText());
    }


    @Test
    public void test02() {
        String text = deepseekChatClient
                .prompt()
                .user("你好,你是什么模型？什么版本?")
                .toolContext(Map.of("name","测试工具"))
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        System.out.println(text);
    }


    /**
     * 编程式：工具调用
     */
    @Test
    public void test01() {

        /*
        *
        * toolDefinition ：定义工具名称、描述和输入模式的 ToolDefinition 实例。您可以使用 ToolDefinition.Builder 类构建它。必需。

        toolMetadata ： ToolMetadata 实例定义了一些附加设置，例如是否将结果直接返回给客户端，以及要使用的结果转换器。您可以使用 ToolMetadata.Builder 类来构建它。

        toolMethod ：表示工具方法的 Method 实例。必需。

        toolObject ：包含工具方法的对象实例。如果该方法是静态的，则可以省略此参数。

        toolCallResultConverter ：用于将工具调用结果转换为 String 对象并发送回 AI 模型的 ToolCallResultConverter 实例。如果未提供，则将使用默认转换器（ DefaultToolCallResultConverter ）。
        * */

        Method method = ReflectionUtils.findMethod(DateTimeTools.class, "getCurrentDateTime");

        MethodToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinitions.builder(method)
                        .description("获取当前时间")
                        .build())
                .toolMethod(method)
                .toolObject(new DateTimeTools())
                .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
                .build();


        String text = deepseekChatClient.prompt().user("当前是什么时间?")
                .toolCallbacks(toolCallback)
                .call().chatResponse().getResult().getOutput().getText();
        System.out.println("结果："+ text);
    }
}
