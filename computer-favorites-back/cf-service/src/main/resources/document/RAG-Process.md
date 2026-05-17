## 总体设计概览

**目标流程：**

> Query → Retrieval → Join → Rerank → Compress → LLM

**核心思想：**

- **Retrieval：**多路检索（向量库 + Web 搜索）
- **Join：**合并多查询、多数据源文档
- **Rerank：**按相关性重排
- **Compress：**压缩上下文，控制 token
- **LLM：**最终生成回答

下面我用 Spring Boot + Spring AI 写一套“能直接上生产”的结构示例。

---

## 1. 依赖与基础组件

### Maven 依赖示意

```xml
<dependencies>
    <!-- Spring AI 核心 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>

    <!-- 向量库（示例：Milvus / PgVector / Redis 等，按需选择） -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-vectorstore-milvus</artifactId>
    </dependency>

    <!-- Web 搜索客户端（自定义或第三方 SDK） -->
    <!-- 这里留空，由你自己实现 WebSearchClient -->
</dependencies>
```

---

## 2. Retrieval：多路检索配置

### 2.1 向量库检索器（VectorStoreDocumentRetriever）

```java
@Configuration
public class RetrievalConfig {

    @Bean
    public DocumentRetriever vectorStoreRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.75)
                .topK(8)
                .filterExpression("type == 'doc' && status == 'active'")
                .build();
    }

    @Bean
    public DocumentRetriever webSearchRetriever(WebSearchClient webSearchClient) {
        return new WebSearchDocumentRetriever(webSearchClient, 5);
    }
}
```

- **vectorStoreRetriever：**语义检索 + 过滤 + 阈值控制
- **webSearchRetriever：**实时 Web 检索（你可以封装成 `WebSearchDocumentRetriever`，实现 `DocumentRetriever` 接口）

---

## 3. Join：多查询 + 多数据源文档合并

### 3.1 Query 扩展（可选）

```java
@Component
public class MultiQueryExpander {

    private final ChatClient chatClient;

    public MultiQueryExpander(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<Query> expand(String userQuery) {
        // 简化示例：你可以用 LLM 生成多个改写查询
        return List.of(
                new Query(userQuery),
                new Query(userQuery + " 详细说明"),
                new Query("用更技术的角度解释：" + userQuery)
        );
    }
}
```

### 3.2 文档 Join（ConcatenationDocumentJoiner）

```java
@Component
public class DocumentJoinerPipeline {

    private final DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();

    public List<Document> join(Map<Query, List<List<Document>>> documentsForQuery) {
        return documentJoiner.join(documentsForQuery);
    }
}
```

---

## 4. Rerank：重排文档

你可以用两种方式：

- **Embedding/向量相似度重排**
- **LLM-based Rerank（效果更好，成本更高）**

### 4.1 简单 LLM Reranker 示例

```java
@Component
public class LlmReranker {

    private final ChatClient chatClient;

    public LlmReranker(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<Document> rerank(String userQuery, List<Document> docs) {

        // 简化：把文档内容拼成一个 prompt，让 LLM 输出排序后的索引
        StringBuilder sb = new StringBuilder();
        sb.append("用户问题：").append(userQuery).append("\n\n");
        sb.append("下面是候选文档，请按相关性从高到低排序，返回文档索引（从0开始，用逗号分隔）：\n\n");

        for (int i = 0; i < docs.size(); i++) {
            sb.append("[").append(i).append("] ")
              .append(docs.get(i).getContent()).append("\n\n");
        }

        String result = chatClient.prompt()
                .user(sb.toString())
                .call()
                .content();

        // 假设 LLM 返回类似：0,2,1,3...
        List<Integer> order = parseOrder(result);

        return order.stream()
                .filter(i -> i >= 0 && i < docs.size())
                .map(docs::get)
                .toList();
    }

    private List<Integer> parseOrder(String result) {
        return Arrays.stream(result.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }
}
```

---

## 5. Compress：上下文压缩（控制 token）

### 5.1 简单 Token 限制压缩器

```java
@Component
public class TokenLimitCompressor {

    private final int maxTokens = 2000; // 根据模型上下文长度调整

    public List<Document> compress(List<Document> docs) {
        List<Document> result = new ArrayList<>();
        int tokens = 0;

        for (Document doc : docs) {
            int docTokens = estimateTokens(doc.getContent());
            if (tokens + docTokens > maxTokens) {
                break;
            }
            result.add(doc);
            tokens += docTokens;
        }
        return result;
    }

    private int estimateTokens(String text) {
        // 简化估算：真实项目可以用 tiktoken 等库
        return text.length() / 3;
    }
}
```

你也可以做更智能的压缩：

- **摘要每个文档**
- **只保留关键段落**
- **按字段权重裁剪**

---

## 6. LLM：最终回答生成

### 6.1 整个 Pipeline 服务

```java
@Service
public class RagService {

    private final MultiQueryExpander queryExpander;
    private final DocumentRetriever vectorStoreRetriever;
    private final DocumentRetriever webSearchRetriever;
    private final DocumentJoinerPipeline documentJoinerPipeline;
    private final LlmReranker reranker;
    private final TokenLimitCompressor compressor;
    private final ChatClient chatClient;

    public RagService(MultiQueryExpander queryExpander,
                      DocumentRetriever vectorStoreRetriever,
                      DocumentRetriever webSearchRetriever,
                      DocumentJoinerPipeline documentJoinerPipeline,
                      LlmReranker reranker,
                      TokenLimitCompressor compressor,
                      ChatClient chatClient) {
        this.queryExpander = queryExpander;
        this.vectorStoreRetriever = vectorStoreRetriever;
        this.webSearchRetriever = webSearchRetriever;
        this.documentJoinerPipeline = documentJoinerPipeline;
        this.reranker = reranker;
        this.compressor = compressor;
        this.chatClient = chatClient;
    }

    public String answer(String userQuery) {

        // 1. 扩展查询
        List<Query> queries = queryExpander.expand(userQuery);

        // 2. 多路检索：向量库 + Web 搜索
        Map<Query, List<List<Document>>> docsForQuery = new LinkedHashMap<>();
        for (Query q : queries) {
            List<List<Document>> docsFromSources = List.of(
                    vectorStoreRetriever.retrieve(q),
                    webSearchRetriever.retrieve(q)
            );
            docsForQuery.put(q, docsFromSources);
        }

        // 3. Join：合并 + 去重
        List<Document> joinedDocs = documentJoinerPipeline.join(docsForQuery);

        // 4. Rerank：按相关性重排
        List<Document> rerankedDocs = reranker.rerank(userQuery, joinedDocs);

        // 5. Compress：控制上下文长度
        List<Document> compressedDocs = compressor.compress(rerankedDocs);

        // 6. 构造 Prompt + 调用 LLM
        String context = compressedDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                你是一个专业助手，请基于以下“检索到的文档”回答用户问题。
                如果文档中没有相关信息，请明确说明你不知道，而不是编造。

                【用户问题】
                %s

                【检索到的文档】
                %s
                """.formatted(userQuery, context);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
```

---

## 7. 这套流程为什么“接近生产级”？

- **多路检索：**向量库 + Web，兼顾“知识库 + 实时性”
- **多查询扩展：**缓解用户提问不精确的问题
- **Join + 去重：**避免重复文档污染上下文
- **Rerank：**把最相关的文档排在前面
- **Compress：**严格控制 token，避免超长上下文
- **LLM Prompt 设计：**明确“不知道就说不知道”，降低幻觉

---

如果你愿意，我们可以下一步做得更狠一点，比如：

- 换成你现在实际用的向量库（Milvus / Redis / PgVector 等）
- 把 WebSearchRetriever 换成你真实的搜索 API
- 把 Rerank 和 Compress 改成你目标模型（如 GPT-4 / Claude / Gemini）的最佳实践

你现在的项目里，向量库和 LLM 分别用的是什么？我可以直接按你的技术栈改一版“贴脸实战版”。