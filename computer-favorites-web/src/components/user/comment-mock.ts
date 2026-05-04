export interface CommentUser {
  id: number
  nickname: string
  avatar: string
}

export interface CommentReply {
  id: number
  user: CommentUser
  content: string
  createTime: string
  likeCount: number
  isLiked: boolean
  replyTo?: string
}

export interface CommentItem {
  id: number
  user: CommentUser
  content: string
  createTime: string
  likeCount: number
  isLiked: boolean
  replies: CommentReply[]
}

export const currentUser: CommentUser = {
  id: 1,
  nickname: '测试用户',
  avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=testuser',
}

export const mockComments: CommentItem[] = [
  {
    id: 101,
    user: {
      id: 2,
      nickname: '前端小王',
      avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=xiaowang',
    },
    content: '这个网站资源真的很全，收藏了！对初学者特别友好，推荐给大家。',
    createTime: '2026-04-28 14:32:18',
    likeCount: 12,
    isLiked: false,
    replies: [
      {
        id: 201,
        user: {
          id: 3,
          nickname: '后端老张',
          avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=laozhang',
        },
        content: '确实，我也在上面找到了不少好资料。',
        createTime: '2026-04-28 15:10:05',
        likeCount: 3,
        isLiked: false,
        replyTo: '前端小王',
      },
    ],
  },
  {
    id: 102,
    user: {
      id: 4,
      nickname: '算法爱好者',
      avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=algo',
    },
    content: '里面的算法题库更新很快，每周都有新题，非常适合刷题党。',
    createTime: '2026-04-27 09:15:42',
    likeCount: 8,
    isLiked: true,
    replies: [],
  },
  {
    id: 103,
    user: {
      id: 5,
      nickname: '设计小白',
      avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=design',
    },
    content: 'UI设计部分的教程很详细，跟着做就能出效果，已经做了三个项目了！',
    createTime: '2026-04-26 20:45:11',
    likeCount: 5,
    isLiked: false,
    replies: [
      {
        id: 202,
        user: {
          id: 6,
          nickname: '全栈大牛',
          avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=fullstack',
        },
        content: '同感！设计部分确实讲得比较透彻。',
        createTime: '2026-04-26 21:30:22',
        likeCount: 2,
        isLiked: false,
        replyTo: '设计小白',
      },
      {
        id: 203,
        user: {
          id: 7,
          nickname: '产品经理Lisa',
          avatar: 'https://api.dicebear.com/7.x/notionists/svg?seed=lisa',
        },
        content: '可以分享一下你的项目经验吗？',
        createTime: '2026-04-27 08:12:09',
        likeCount: 1,
        isLiked: false,
        replyTo: '设计小白',
      },
    ],
  },
]
