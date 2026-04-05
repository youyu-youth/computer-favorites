<script setup lang="ts">
import { ref } from 'vue'
import {
  SquareTerminal,
  Heart,
  BookOpen,
  Code2,
  Database,
  Cloud,
  Download,
  Play,
  Package,
  Github,
  HelpCircle,
  Bug,
} from 'lucide-vue-next'

const activeTab = ref('Overview')
const tabs = ['Overview', 'Schema', 'Related Servers', 'Score', 'Discussions']
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900 dark:bg-black dark:text-gray-100">
    <main class="mx-auto flex-grow w-full max-w-7xl px-4 sm:px-6 lg:px-8 pb-12 pt-8">
      <!-- 头部信息区 -->
    <header class="mb-8 flex flex-col justify-between gap-6 md:flex-row md:items-start">
      <div class="flex items-start gap-4">
        <!-- Logo -->
        <div
          class="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-sm border border-gray-700 bg-slate-800"
        >
          <SquareTerminal class="h-10 w-10 text-primary-500" />
        </div>

        <div>
          <div class="mb-1 flex items-center gap-3 flex-wrap">
            <h1 class="text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
              CodeMaster MCP
            </h1>
            <span
              class="rounded border border-blue-500/30 bg-blue-500/20 px-2 py-0.5 text-xs font-medium text-blue-400"
              >官方</span
            >
            <button
              class="flex items-center gap-1.5 rounded border border-gray-300 px-3 py-1 text-sm text-gray-700 transition-colors hover:bg-gray-100 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800"
            >
              <Heart class="h-4 w-4" />
              收藏
            </button>
          </div>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
            由
            <a href="#" class="text-primary-500 hover:underline">DevForge Inc.</a>
            提供 ·
            <a href="#" class="hover:text-gray-300 text-gray-400">声明所有权</a>
          </p>
        </div>
      </div>

      <!-- 右侧标签 -->
      <div class="flex flex-wrap gap-3">
        <div
          class="flex items-center gap-2 rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 dark:border-gray-700 dark:text-gray-300"
        >
          <BookOpen class="h-4 w-4" /> API 文档权限
        </div>
        <div
          class="flex items-center gap-2 rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 dark:border-gray-700 dark:text-gray-300"
        >
          <Code2 class="h-4 w-4" /> 代码片段库
        </div>
        <div
          class="flex items-center gap-2 rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 dark:border-gray-700 dark:text-gray-300"
        >
          <Database class="h-4 w-4" /> 语法数据库
        </div>
      </div>
    </header>

    <!-- 导航 Tabs -->
    <div class="mb-6 flex items-end justify-between border-b border-gray-200 dark:border-gray-800">
      <nav class="no-scrollbar flex gap-6 overflow-x-auto">
        <button
          v-for="tab in tabs"
          :key="tab"
          @click="activeTab = tab"
          class="pb-3 text-sm font-medium whitespace-nowrap border-b-2 transition-colors duration-200"
          :class="
            activeTab === tab
              ? 'border-primary-500 text-primary-500'
              : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 dark:text-gray-400 dark:hover:border-gray-700 dark:hover:text-gray-300'
          "
        >
          {{ tab }}
        </button>
      </nav>
      <div class="hidden items-center gap-4 pb-3 text-xs text-gray-500 md:flex dark:text-gray-400">
        <span class="flex items-center gap-1.5"
          ><span class="h-2 w-2 rounded-full bg-blue-400"></span> TypeScript</span
        >
        <span class="flex items-center gap-1.5"><Cloud class="h-3.5 w-3.5" /> 远程服务</span>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
      <!-- 左侧：文档内容区域 -->
      <div class="lg:col-span-2">
        <!-- 顶端 Markdown 风格的简介引用区 -->
        <blockquote
          class="flat-design mb-6 w-full rounded-r border-l-4 border-primary-500 bg-primary-500/10 p-4 dark:bg-primary-500/20"
        >
          <p
            class="text-sm italic leading-relaxed text-primary-600 md:text-base dark:text-amber-400"
          >
            CodeMaster MCP 是一个赋能 AI 的全栈编程辅助核心，通过 MCP
            协议提供多语言解析、架构验证与实时漏洞扫描能力，旨在让 AI 助手完美融入您的开发工作流。
          </p>
        </blockquote>

        <!-- 核心文档区 -->
        <div
          class="flat-design rounded border border-[#094438]/20 bg-[#094438]/10 p-6 sm:p-8 dark:border-[#094438]/40 dark:bg-[#094438]/30"
        >
          <h2 class="mb-4 text-3xl font-bold text-gray-900 dark:text-white">
            CodeMaster MCP Server
          </h2>

          <!-- 徽章 -->
          <div class="mb-6 flex flex-wrap gap-2">
            <span
              class="rounded border border-gray-300 bg-gray-200 px-2 py-0.5 text-xs font-mono text-gray-700 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300"
              >license MIT</span
            >
            <span class="rounded bg-primary-500 px-2 py-0.5 text-xs font-mono text-white"
              >npm v2.1.0</span
            >
            <span class="rounded bg-cyan-600 px-2 py-0.5 text-xs font-mono text-white"
              >MCP compatible</span
            >
          </div>

          <p class="mb-4 text-lg font-medium text-gray-800 dark:text-gray-200">
            赋予您的 AI 助手专家级的全栈编程与架构设计能力。
          </p>

          <p class="mb-6 text-sm leading-relaxed text-gray-700 sm:text-base dark:text-gray-300">
            一个基于 MCP (Model Context Protocol) 架构的服务器，旨在为 AI
            编程助手提供深度的上下文感知能力。它包含了超过 50
            种编程语言的语法树解析、流行框架的最佳实践验证、常见 Bug
            模式排查，以及自动化的单元测试规范生成——所有这些都作为标准工具提供给 AI 调用。
          </p>

          <div class="mb-8 text-sm">
            <span class="font-bold text-gray-900 dark:text-white">官方网站: </span>
            <a href="#" class="text-primary-500 hover:underline">codemaster.dev</a>
          </div>

          <h3
            class="mb-4 border-b border-[#094438]/20 pb-2 text-xl font-bold text-gray-900 dark:border-[#094438]/40 dark:text-white"
          >
            为什么选择我们？
          </h3>
          <p class="mb-4 text-sm leading-relaxed text-gray-700 sm:text-base dark:text-gray-300">
            配置一个能够准确理解复杂项目结构的 AI 编程环境通常需要耗费大量时间：定义 AST
            解析规则、建立类型推导模型、处理跨文件依赖、遵守团队代码规范 (Linting rules)。CodeMaster
            MCP 服务器将这些繁杂的基础设施打包：
          </p>
          <ul
            class="list-disc space-y-2 pl-5 text-sm text-gray-700 sm:text-base dark:text-gray-300"
          >
            <li>提供即插即用的多语言解析器接口。</li>
            <li>内置基于云端的大规模开源代码片段库检索。</li>
            <li>实时进行依赖漏洞扫描与安全建议。</li>
            <li>零配置接入 VS Code, Cursor 和 WebStorm 等主流开发环境。</li>
          </ul>
        </div>
      </div>

      <!-- 右侧：操作与信息栏 -->
      <div class="flex flex-col gap-6 lg:col-span-1">
        <!-- 操作按钮 -->
        <div class="flex flex-col gap-3">
          <button
            class="flat-design flex w-full items-center justify-center gap-2 rounded bg-primary-500 px-4 py-3 font-bold text-white transition-colors hover:bg-primary-600"
          >
            <Download class="h-5 w-5" />
            安装服务器 (Install)
          </button>
          <button
            class="flat-design flex w-full items-center justify-center gap-2 rounded border border-gray-300 bg-gray-200 px-4 py-3 font-bold text-gray-900 transition-colors hover:bg-gray-300 dark:border-[#1e4d42] dark:bg-[#112d26] dark:text-gray-100 dark:hover:bg-[#163a32]"
          >
            <Play class="h-5 w-5" />
            在浏览器中体验 (Try)
          </button>
        </div>

        <!-- 评分卡片 -->
        <div
          class="flat-design rounded border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-black"
        >
          <ul class="space-y-4">
            <li class="flex items-center gap-3 text-sm">
              <div
                class="flex h-5 w-5 items-center justify-center rounded-full border-2 border-green-500 text-[10px] font-bold text-green-500"
              >
                A
              </div>
              <span class="text-gray-700 dark:text-gray-300">安全性 – 未知漏洞</span>
            </li>
            <li class="flex items-center gap-3 text-sm">
              <div
                class="flex h-5 w-5 items-center justify-center rounded-full border-2 border-green-500 text-[10px] font-bold text-green-500"
              >
                A
              </div>
              <span class="text-gray-700 dark:text-gray-300">许可证 – 宽松开源协议</span>
            </li>
            <li class="flex items-center gap-3 text-sm">
              <div
                class="flex h-5 w-5 items-center justify-center rounded-full border-2 border-green-500 text-[10px] font-bold text-green-500"
              >
                A
              </div>
              <span class="text-gray-700 dark:text-gray-300">代码质量 – A 级评定</span>
            </li>
          </ul>
          <div class="mt-4 border-t border-gray-200 pt-3 text-center dark:border-gray-800">
            <a
              href="#"
              class="text-xs text-gray-500 underline hover:text-primary-500 dark:text-gray-400"
              >这些分数是如何计算的？</a
            >
          </div>
        </div>

        <!-- 资源链接 -->
        <div>
          <h4 class="mb-3 text-lg font-bold text-gray-900 dark:text-white">开发资源</h4>
          <ul class="space-y-2 text-sm">
            <li>
              <a
                href="#"
                class="flex items-center gap-2 text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-400"
              >
                <Package class="h-4 w-4" /> NPM Package
              </a>
            </li>
            <li>
              <a
                href="#"
                class="flex items-center gap-2 text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-400"
              >
                <Github class="h-4 w-4" /> GitHub Repository
              </a>
            </li>
            <li>
              <a
                href="#"
                class="flex items-center gap-2 text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-400"
              >
                <HelpCircle class="h-4 w-4" /> 获取帮助 (Need Help?)
              </a>
            </li>
            <li>
              <a
                href="#"
                class="flex items-center gap-2 text-gray-600 transition-colors hover:text-primary-500 dark:text-gray-400"
              >
                <Bug class="h-4 w-4" /> 报告问题 (Report Issue)
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </main>
</div>
</template>

<style scoped>
/* 隐藏滚动条但保留功能 */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 扁平化设计 */
.flat-design {
  box-shadow: none !important;
}
</style>
