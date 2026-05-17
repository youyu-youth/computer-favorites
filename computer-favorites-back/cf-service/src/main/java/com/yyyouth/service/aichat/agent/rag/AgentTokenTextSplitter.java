package com.yyyouth.service.aichat.agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/20
 * 文本切分器
 *  对该项目的功能还为开发完，待优化和完善
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AgentTokenTextSplitter {

    /**
     * 使用构建器模式进行文本切分
     * @param documents
     * @return
     */
    public List<Document> splitWithBuilder(List<Document> documents) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                // 每个文本块的目标大小（以标记为单位）（默认值：800）。
                .withChunkSize(1000)
                //每个文本块的最小字符数（默认值：350）。
                .withMinChunkSizeChars(400)
                //要包含的块的最小长度（默认值：5）。
                .withMinChunkLengthToEmbed(10)
                //从文本生成的最大块数（默认值：10000）。
                .withMaxNumChunks(5000)
                //是否在代码块中保留分隔符（如换行符）（默认值：true）。
                .withKeepSeparator(true)
                .build();
        return splitter.apply(documents);
    }


}
