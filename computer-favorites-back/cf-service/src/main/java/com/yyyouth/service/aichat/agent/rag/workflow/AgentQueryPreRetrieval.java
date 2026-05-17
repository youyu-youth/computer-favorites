package com.yyyouth.service.aichat.agent.rag.workflow;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/04/22
 * @Description: 阶段1： pre-retrieval  代理文件预检索  对用户提示词进行预检索
 * 对该项目的功能还为开发完，待优化和完善
 */
@Slf4j
@Component
public class AgentQueryPreRetrieval {


    private final ChatClient.Builder builder;

    @Autowired
   private VectorStore vectorStore;

    @Resource
    private DocumentJoiner documentJoiner;


    public AgentQueryPreRetrieval(ChatModel deepSeekChatModel) {
        this.builder = ChatClient.builder(deepSeekChatModel);
    }



    /**
     * 多查询扩展
     *MultiQueryExpander 使用大型语言模型将查询扩展为多个语义不同的变体，以捕捉不同的视角，这有助于检索额外的上下文信息并增加找到相关结果的机会。
     * @param message
     * @return
     */
    public List<Query> multiQueryExpand(String message) {

        // 1. 扩展查询
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                .numberOfQueries(3)
                .build();
        List<Query> queries = queryExpander.expand(new Query(message));


//        //2. 向量库检索 以及 网络搜索引擎检索
//        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
//                .vectorStore(vectorStore)
//                .similarityThreshold(0.7)
//                .topK(5)
//                .build();
//
//        Map<Query, List<List<Document>>> docsForQuery = new HashMap<>();
//        for (Query q : queries) {
//            List<List<Document>> docsFromSources = List.of(
//                    documentRetriever.retrieve(q)
//            );
//            docsForQuery.put(q, docsFromSources);
//        }
//
//        //3. 合并文档
//        documentJoiner.join(docsForQuery)

        return queries;
    }




    /**
     * 查询重写
     *RewriteQueryTransformer 使用大型语言模型重写用户查询，以便在查询目标系统（例如向量存储或网络搜索引擎）时提供更好的结果。
     *
     * 当用户查询冗长、含糊不清或包含可能影响搜索结果质量的无关信息时，此转换器非常有用。
     * @param message
     * @return
     */
    public String queryRewrite(String message) {
        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
        Query rewrittenQuery = queryTransformer.transform(new Query(message));
        return rewrittenQuery.text();
    }





    /**
     * 查询压缩
     * CompressionQueryTransformer 使用大型语言模型将对话历史和后续查询压缩成一个独立的查询，该查询能够捕捉对话的本质。
     *
     * 当对话历史很长，并且后续查询与对话上下文相关时，此转换器非常有用。
     * @param message
     * @return
     */
    public String queryCompression(String message) {
//        Query query = Query.builder()
//                .text("And what is its second largest city?")
//                .history(new UserMessage("What is the capital of Denmark?"),
//                        new AssistantMessage("Copenhagen is the capital of Denmark."))
//                .build();

        Query query = Query.builder()
                .text(message)
                // 这里可以使用 tool 工具查询数据库的对话历史来进行补充
                .history(new UserMessage("我是今年才结婚"))
                .build();

        QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();

        Query compressedQuery = queryTransformer.transform(query);
        return compressedQuery.text();
    }


    /**
     * 查询翻译
     * TranslationQueryTransformer 使用大型语言模型将查询翻译成目标语言，该目标语言必须受用于生成文档嵌入的嵌入模型支持。
     * 如果查询本身已是目标语言，则直接返回，不做任何更改。如果查询的语言未知，也直接返回，不做任何更改。
     * @param message
     * @return
     */
    public String queryTranslation(String message) {

        Query query = new Query(message);

        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetLanguage("english")
                .build();

        Query transformedQuery = queryTransformer.transform(query);

        return transformedQuery.text();
    }




}
