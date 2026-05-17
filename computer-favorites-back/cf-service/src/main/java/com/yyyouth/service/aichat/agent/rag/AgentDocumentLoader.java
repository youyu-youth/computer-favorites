package com.yyyouth.service.aichat.agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/20
 * agent 文档加载器
 *  对该项目的功能还为开发完，待优化和完善
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AgentDocumentLoader {


    private final ResourcePatternResolver patternResolver;


    /**
     * 加载本地markdown文件
     * @return
     */
    public List<Document> loadMarkdownFiles(){
        List<Document> documents = new ArrayList<>();
        log.info("加载markdown文件...");
        try {
            Resource[] resources = patternResolver
                    .getResources("classpath*:rag/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                MarkdownDocumentReaderConfig markdownConfig = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeBlockquote(false)
                        .withIncludeCodeBlock(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();

                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource,markdownConfig);
                documents.addAll(reader.get());
            }
            log.info("加载markdown文件成功...");
        } catch (IOException e) {
            log.error("加载文件失败!", e);
        }



        return documents;

    }


}
