import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Website, Category } from '@/types/website'

export const useWebsiteStore = defineStore('website', () => {
  const websites = ref<Website[]>([
    {
      id: 1,
      title: 'Vue.js',
      desc: '渐进式 JavaScript 框架，易学易用，性能出色，适用场景丰富的 Web 前端框架。',
      tag: '编程开发',
      hot: '88',
    },
    {
      id: 2,
      title: 'Tailwind CSS',
      desc: '只需书写 HTML 代码，无需书写 CSS，即可快速构建自定义用户界面的实用型 CSS 框架。',
      tag: 'UI/UX设计',
      hot: '59',
    },
    {
      id: 3,
      title: 'ChatGPT',
      desc: 'OpenAI 训练的大型语言模型，能够理解和生成自然语言文本，协助完成各类文本任务。',
      tag: 'AI 工具',
      hot: '312',
    },
    {
      id: 4,
      title: 'GitHub',
      desc: '全球最大的开源代码托管平台，开发者发现、分享和构建优秀软件的首选。',
      tag: '开源社区',
      hot: '120',
    },
    {
      id: 5,
      title: 'Figma',
      desc: '基于浏览器的协作式 UI 设计工具，让设计团队可以实时协作，提升设计效率。',
      tag: 'UI/UX设计',
      hot: '45',
    },
    {
      id: 6,
      title: 'Notion',
      desc: '提供笔记、任务、数据库和看板等多种功能的 All-in-one 生产力工具。',
      tag: '效率神器',
      hot: '76',
    },
    {
      id: 7,
      title: '掘金',
      desc: '一个帮助开发者成长的社区，包含丰富的高质量技术文章和技术交流。',
      tag: '文章/报告',
      hot: '34',
    },
    {
      id: 8,
      title: 'Bilibili',
      desc: '国内知名的视频弹幕网站，这里有最及时的动漫新番，也有极具创意的技术教学视频。',
      tag: '摸鱼必备',
      hot: '201',
    },
    {
      id: 9,
      title: 'Midjourney',
      desc: '强大的 AI 图像生成工具，只需输入提示词即可生成高质量、富有创意的艺术作品。',
      tag: 'AI 工具',
      hot: '156',
    },
  ])

  const categories = ref<Category[]>([
    { name: '编程开发', color: 'bg-green-500' },
    { name: 'UI/UX设计', color: 'bg-blue-500' },
    { name: 'AI 工具', color: 'bg-indigo-500' },
    { name: '效率神器', color: 'bg-yellow-500' },
    { name: '文章/报告', color: 'bg-red-500' },
    { name: '摸鱼必备', color: 'bg-pink-500' },
    { name: '终端/解释器', color: 'bg-teal-500' },
    { name: '开源社区', color: 'bg-orange-500' },
  ])

  const loading = ref(false)

  const fetchWebsites = async () => {
    loading.value = true
    // TODO: 未来实现API调用
    loading.value = false
  }

  const fetchCategories = async () => {
    loading.value = true
    // TODO: 未来实现API调用
    loading.value = false
  }

  return {
    websites,
    categories,
    loading,
    fetchWebsites,
    fetchCategories,
  }
})
