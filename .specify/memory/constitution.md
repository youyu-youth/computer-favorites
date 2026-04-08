<!--
Sync Impact Report
- Version change: N/A -> 1.0.0
- Modified principles:
	- Template Principle 1 -> I. Code Quality Is a Non-Negotiable Baseline
	- Template Principle 2 -> II. Testing Is a Delivery Gate, Not a Decoration
	- Template Principle 3 -> III. UX Consistency Is a Functional Requirement
	- Template Principle 4 -> IV. Performance Budgets Must Be Declared and Verified
	- Template Principle 5 -> V. Governance Must Drive Technical Choices
- Added sections:
	- Decision and Implementation Criteria
	- Delivery Workflow and Quality Gates
- Removed sections:
	- None
- Templates requiring updates:
	- ✅ .specify/templates/plan-template.md
	- ✅ .specify/templates/spec-template.md
	- ✅ .specify/templates/tasks-template.md
	- ⚠ pending: .specify/templates/commands/*.md (directory not found)
	- ✅ .github/copilot-instructions.md
- Deferred TODOs:
	- None
-->

# Computer Favorites Engineering Constitution

## Core Principles

### I. Code Quality Is a Non-Negotiable Baseline
- 所有可合并代码 MUST 满足可读性优先、单一职责、低耦合高内聚，不得引入“大而全”类与跨层泄漏。
- 开发前 MUST 优先复用既有模式；若新增模式，MUST 在实现说明中给出必要性与收益对比。
- 领域边界、分层职责、命名一致性 MUST 与现有规范保持一致，不得以“先跑通”为理由牺牲结构质量。

理由：代码质量直接决定维护成本和缺陷密度，是长期交付能力的基线，不是后期可补项。

### II. Testing Is a Delivery Gate, Not a Decoration
- 任何行为变更（新功能、缺陷修复、契约调整）MUST 提供对应测试证据，至少覆盖受影响路径。
- 关键链路 MUST 包含集成测试或端到端验证，确保前后端、接口、数据与页面表现一致。
- 禁止“仅手工验证”替代自动化测试作为发布依据；测试失败或缺失即视为不可发布。

理由：测试标准用于约束交付质量下限，避免回归风险向线上转移。

### III. UX Consistency Is a Functional Requirement
- 用户体验一致性 MUST 作为需求本体，不得降级为“视觉优化建议”。
- 同一业务在桌面端与移动端 MUST 具有一致的信息架构、反馈语义、交互预期与主题切换行为。
- 可用性与可访问性问题（状态不可见、反馈不明确、可点击区域不一致）MUST 在合并前关闭。

理由：体验一致性决定用户学习成本和信任感，属于功能正确性的组成部分。

### IV. Performance Budgets Must Be Declared and Verified
- 每个特性在方案阶段 MUST 声明性能预算（如接口 p95、首屏加载、关键交互响应时间）。
- 实施后 MUST 提供可复现的性能验证结果，并与预算对比；超预算项 MUST 提供整改计划。
- 禁止以“功能完成”为理由跳过性能验收，除非治理流程中有明确、可追溯的临时豁免。

理由：性能是用户体验和系统成本的共同约束，必须前置设计、后置验证。

### V. Governance Must Drive Technical Choices
- 技术决策 MUST 以本宪章为最高约束，遵循“质量、测试、体验、性能”优先顺序。
- 出现取舍冲突时，MUST 记录决策背景、备选方案、拒绝理由与影响范围。
- 任何临时方案 MUST 有失效条件和回收时间点，禁止长期保留无主技术债。

理由：治理的目标是降低随意性，确保团队在压力场景下仍做出可审计、可复用的理性决策。

## Decision and Implementation Criteria

所有实现方案 MUST 在设计或评审阶段回答以下问题：

1. 代码质量：该方案是否复用现有模式，是否引入了不必要复杂度。
2. 测试标准：是否定义了最小可验证集合，是否覆盖核心风险路径。
3. 体验一致性：是否保证跨端体验语义一致，是否符合主题与交互规范。
4. 性能要求：是否声明预算并给出验证方法，是否评估了容量与峰值场景。

当多个方案均可行时，MUST 选择“更易测试、更易维护、体验更一致、预算更可控”的方案。

## Delivery Workflow and Quality Gates

1. 需求阶段 MUST 明确功能需求与四类非功能约束（质量、测试、体验、性能）。
2. 设计阶段 MUST 输出决策记录，说明实现选择如何满足本宪章原则。
3. 开发阶段 MUST 与任务清单保持一致，禁止跳过测试与性能验证任务。
4. 评审阶段 MUST 使用宪章门禁逐项检查，不通过项不得合并。
5. 发布阶段 MUST 留存验证证据（测试报告、体验核对、性能结果）以支持审计。

## Governance

- 宪章优先级：本文件高于一般实施习惯；如与其他文档冲突，以本宪章为准并触发文档修订。
- 修订流程：任何修订 MUST 提交变更说明，包含变更原因、影响范围、迁移策略，并经维护者审批。
- 版本策略：
	- MAJOR：原则删除、语义重定义或治理模型不兼容变更。
	- MINOR：新增原则、门禁或显著扩展约束范围。
	- PATCH：措辞澄清、示例优化、排版或无语义变化的修订。
- 合规审查：所有计划、规格、任务与 PR 评审 MUST 执行宪章对齐检查，并保留结果。

**Version**: 1.0.0 | **Ratified**: 2026-04-07 | **Last Amended**: 2026-04-07
