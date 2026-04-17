/**
 * 中文用户模块翻译
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { UserMessages } from '../../types'

export const user: UserMessages = {
  profile: {
    title: '个人主页',
    mySubmissions: '我的投稿网站',
    editProfile: '编辑资料',
    viewProfile: '查看资料',
    basicInfo: '基本信息',
    nickname: '昵称',
    avatar: '头像',
    gender: '性别',
    male: '男',
    female: '女',
    other: '其他',
    country: '国家',
    city: '城市',
    signature: '个性签名',
    hobbies: '兴趣爱好',
    techStack: '技术栈',
    socialLinks: '社交链接',
    github: 'GitHub',
    gitee: 'Gitee',
    blog: '博客',
    otherRepo: '其他仓库',
    contribution: '贡献',
    contributions: '次贡献',
    noContribution: '暂无贡献记录',
    projects: '项目',
    websites: '网站',
    skills: '技能',
    noSkills: '暂无技能标签',
  },
  home: {
    hero: {
      title: '编程爱好者的宝藏收藏',
      subtitle: '发现、收藏、分享优质编程资源',
      uploadButton: '上传网站',
      browseButton: '浏览热门',
    },
    feedback: {
      navButton: '反馈',
      dialogTitle: '意见反馈',
      dialogDescription: '欢迎提交建议或问题，我们会尽快跟进处理。',
      typeLabel: '反馈类型',
      typeSuggestion: '建议',
      typeBug: 'Bug反馈',
      typeComplaint: '投诉',
      typeExperience: '使用感受',
      contentLabel: '反馈内容',
      contentPlaceholder: '请详细描述你的问题或建议，至少10个字符。',
      contentCount: '已输入 {current} 字，最少 {min} 字，最多 {max} 字',
      contactLabel: '联系方式（选填）',
      contactPlaceholder: '可填写邮箱、手机号或社交账号，便于我们回访。',
      imagesLabel: '截图附件（选填）',
      uploadAction: '上传',
      imageAlt: '反馈截图 {index}',
      removeImage: '删除第 {index} 张截图',
      submitAction: '提交反馈',
      submitSuccess: '反馈提交成功，感谢你的建议！',
      submitFailed: '反馈提交失败，请稍后重试',
    },
    filter: {
      search: '搜索',
      searchPlaceholder: '搜索网站名称或描述...',
      categories: '分类',
      allCategories: '全部分类',
      logic: {
        and: '且',
        or: '或',
      },
    },
    grid: {
      visit: '访问',
      copyLink: '复制链接',
      noWebsite: '暂无网站',
      noUrl: '该网站暂无链接',
    },
  },
}
