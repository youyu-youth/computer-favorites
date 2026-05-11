<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

type SiteKey = 'github' | 'stackoverflow' | 'vue' | 'skills'

type SiteItem = {
  id: SiteKey
  name: string
  desc: string
  detailDesc: string
  href: string
  tag: string
}

const sites: SiteItem[] = [
  {
    id: 'github',
    name: 'GitHub',
    desc: '开源代码托管与协作，灵感与工程同时发生。',
    detailDesc:
      '全球最大的开源代码托管平台，拥有超过1亿开发者。在这里，你可以托管代码、审查变更、管理项目，并与其他开发者构建软件。从个人项目到企业级应用，GitHub都是协作的核心。',
    href: 'https://github.com',
    tag: 'Open Source',
  },
  {
    id: 'stackoverflow',
    name: 'Stack Overflow',
    desc: '问题、答案与经验沉淀，检索即生产力。',
    detailDesc:
      '全球最大的开发者问答社区。无论你遇到什么编程难题，几乎都能在这里找到答案。通过提问和回答，你不仅能解决问题，还能建立自己的声誉，帮助他人成长的同时自我提升。',
    href: 'https://stackoverflow.com',
    tag: 'Q&A',
  },
  {
    id: 'vue',
    name: 'Vue',
    desc: '渐进式框架，优雅与效率的平衡。',
    detailDesc:
      '渐进式JavaScript框架，易学易用，性能出色，适用场景丰富。Vue的核心库只关注视图层，不仅易于上手，还便于与第三方库或既有项目整合。配合现代化的工具链，Vue能够驱动复杂的单页应用。',
    href: 'https://vuejs.org',
    tag: 'Frontend',
  },
  {
    id: 'skills',
    name: 'Skills',
    desc: '把方法论固化为技能库，复用每一次经验。',
    detailDesc:
      '开发者技能树与路线图。不仅提供技术的学习路径，还整合了最佳实践、工具推荐和资源链接。通过结构化的知识体系，帮助开发者系统性地掌握新技术，从入门到精通，不再迷失在碎片化的信息中。',
    href: '#',
    tag: 'Toolkit',
  },
]

const defaultSite = sites[0] as SiteItem

const router = useRouter()
const hoverId = ref<SiteKey>('github')
const active = computed(() => sites.find((s) => s.id === hoverId.value) ?? defaultSite)

const visitAsGuest = () => {
  void router.push({ name: 'home' })
}

const iconColor = (id: SiteKey) => {
  if (id === 'stackoverflow') return 'text-amber-700 dark:text-amber-300'
  if (id === 'vue') return 'text-emerald-700 dark:text-emerald-300'
  if (id === 'skills') return 'text-slate-900 dark:text-white'
  return 'text-slate-900 dark:text-white'
}

const cardBgClass = (id: SiteKey) => {
  if (id === 'github')
    return 'bg-rose-50/85 hover:bg-rose-100/90 dark:bg-black/20 dark:hover:bg-white/10'
  if (id === 'stackoverflow')
    return 'bg-red-50/85 hover:bg-red-100/90 dark:bg-black/20 dark:hover:bg-white/10'
  if (id === 'vue')
    return 'bg-pink-50/85 hover:bg-pink-100/90 dark:bg-black/20 dark:hover:bg-white/10'
  return 'bg-fuchsia-50/85 hover:bg-fuchsia-100/90 dark:bg-black/20 dark:hover:bg-white/10'
}
</script>

<template>
  <div class="relative h-full min-h-screen w-full bg-slate-50 dark:bg-black">
    <div class="pointer-events-none absolute inset-0">
      <div class="absolute -left-24 top-10 h-72 w-72 rounded-full bg-emerald-500/20 blur-3xl" />
      <div class="absolute -right-24 bottom-10 h-72 w-72 rounded-full bg-amber-700/15 blur-3xl" />
      <svg class="absolute inset-0 h-full w-full opacity-40 dark:opacity-25" aria-hidden="true">
        <defs>
          <pattern id="grid-24" width="24" height="24" patternUnits="userSpaceOnUse">
            <path d="M 24 0 L 0 0 0 24" fill="none" stroke="currentColor" stroke-width="1" />
          </pattern>
        </defs>
        <rect
          width="100%"
          height="100%"
          fill="url(#grid-24)"
          class="text-slate-200 dark:text-white/10"
        />
      </svg>
    </div>

    <div
      class="relative mx-auto flex min-h-screen max-w-6xl items-start px-10 py-10 xl:items-center"
    >
      <div class="w-full">
        <div class="mb-6">
          <div class="text-xs font-medium tracking-[0.2em] text-slate-500 dark:text-slate-500">
            PROGRAMMER FAVORITES
          </div>
          <div class="mt-2 text-2xl font-semibold tracking-tight text-slate-900 dark:text-white">
            编程爱好者的宝藏网站
          </div>
          <div class="mt-2 max-w-xl text-sm leading-6 text-slate-600 dark:text-slate-400">
            你的灵感入口、资源收藏夹、以及下一次效率提升的起点。
          </div>
        </div>

        <div
          class="relative overflow-hidden rounded-2xl border border-slate-200/70 bg-white/70 shadow-[0_20px_60px_-40px_rgba(15,23,42,0.35)] backdrop-blur-xl dark:border-white/10 dark:bg-white/5"
        >
          <div
            class="flex items-center gap-2 border-b border-slate-200/70 bg-white/60 px-4 py-3 dark:border-white/10 dark:bg-white/5"
          >
            <div class="flex items-center gap-2">
              <div class="h-3 w-3 rounded-full bg-amber-500/80" />
              <div class="h-3 w-3 rounded-full bg-emerald-500/80" />
              <div class="h-3 w-3 rounded-full bg-slate-400/70" />
            </div>
            <div class="ml-3 truncate text-xs text-slate-500 dark:text-slate-400">
              preview://favorites
            </div>
          </div>

          <div class="grid grid-cols-1 gap-6 p-6 xl:grid-cols-[minmax(0,1fr)_320px]">
            <div>
              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <button
                  v-for="s in sites"
                  :key="s.id"
                  type="button"
                  @mouseenter="hoverId = s.id"
                  class="group flex w-full cursor-pointer flex-col justify-between rounded-xl border border-slate-200/70 px-4 py-4 text-left transition-all duration-200 hover:-translate-y-2 hover:border-slate-300 dark:border-white/10 dark:hover:border-white/20"
                  :class="cardBgClass(s.id)"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <div class="text-sm font-semibold text-slate-900 dark:text-white">
                        {{ s.name }}
                      </div>
                      <div class="mt-1 text-xs leading-5 text-slate-600 dark:text-slate-400">
                        {{ s.desc }}
                      </div>
                    </div>
                    <div
                      class="mt-0.5 inline-flex h-10 w-10 items-center justify-center rounded-lg border border-slate-200 bg-white/95 text-slate-900 transition-colors group-hover:bg-slate-100 dark:border-white/10 dark:bg-white/10 dark:group-hover:bg-white/15"
                      :class="iconColor(s.id)"
                    >
                      <svg
                        v-if="s.id === 'github'"
                        class="h-5 w-5"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                      >
                        <path
                          d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"
                        />
                      </svg>
                      <svg
                        v-else-if="s.id === 'stackoverflow'"
                        class="h-5 w-5"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                      >
                        <path
                          d="M18.986 21.865v-6.404h2.134V24H1.844v-8.539h2.13v6.404h15.012zM6.111 19.731H16.85v-2.137H6.111v2.137zm.259-4.852l10.48 2.189.451-2.07-10.478-2.187-.453 2.068zm1.359-5.056l9.705 4.53.903-1.95-9.706-4.53-.902 1.95zm2.714-6.043l8.758 6.13 1.214-1.72-8.756-6.13-1.216 1.72zm4.977-6.458l-1.353 1.614 7.56 6.34 1.35-1.615-7.557-6.339z"
                        />
                      </svg>
                      <svg
                        v-else-if="s.id === 'vue'"
                        class="h-5 w-5"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                      >
                        <path
                          d="M24,1.61H14.06L12,5.16,9.94,1.61H0L12,22.39ZM12,14.08,5.16,2.23H9.59L12,6.41l2.41-4.18h4.43Z"
                        />
                      </svg>
                      <svg
                        v-else-if="s.id === 'skills'"
                        class="h-5 w-5"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6l4 2" />
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                    </div>
                  </div>

                  <div class="mt-3 flex items-center justify-between">
                    <span
                      class="inline-flex items-center rounded-full border border-slate-200 bg-white px-2.5 py-1 text-[11px] font-medium text-slate-600 dark:border-white/10 dark:bg-white/5 dark:text-slate-400"
                    >
                      {{ s.tag }}
                    </span>
                    <span class="text-[11px] text-slate-400 dark:text-slate-500">Hover</span>
                  </div>
                </button>

                <div
                  class="sm:col-span-2 overflow-hidden rounded-xl border border-rose-200/80 bg-rose-50/80 px-4 py-4 shadow-sm dark:border-white/10 dark:bg-black/20"
                >
                  <div class="flex items-center justify-between">
                    <div class="text-sm font-semibold text-slate-900 dark:text-white">
                      Code Editor
                    </div>
                    <div class="text-xs text-slate-500 dark:text-slate-400">TypeScript</div>
                  </div>
                  <div class="mt-3 relative">
                    <div
                      class="overflow-hidden rounded-xl border border-rose-200/70 bg-[#111217] shadow-[0_10px_40px_-24px_rgba(15,23,42,0.8)] dark:border-white/10"
                    >
                      <div
                        class="flex items-center justify-between border-b border-white/10 bg-[#1a1b22] px-4 py-2"
                      >
                        <div class="flex items-center gap-2">
                          <div class="h-2.5 w-2.5 rounded-full bg-red-500/90"></div>
                          <div class="h-2.5 w-2.5 rounded-full bg-amber-400/90"></div>
                          <div class="h-2.5 w-2.5 rounded-full bg-emerald-500/90"></div>
                        </div>
                        <div class="text-[10px] tracking-[0.2em] text-slate-400">
                          TREASURE_VAULT.JS — EDITOR
                        </div>
                      </div>
                      <div
                        class="grid grid-cols-[32px_minmax(0,1fr)] gap-3 px-4 py-3 font-mono text-[14px] leading-7"
                      >
                        <div class="select-none text-right text-slate-500">
                          <div>01</div>
                          <div>02</div>
                          <div>03</div>
                          <div>04</div>
                          <div>05</div>
                        </div>
                        <div class="text-slate-200">
                          <div>
                            <span class="text-violet-300">const</span>
                            <span class="text-cyan-300">devTreasure</span>
                            <span class="text-amber-300">=</span>
                            <span class="text-amber-300">{</span>
                          </div>
                          <div>
                            &nbsp;&nbsp;<span class="text-slate-300">status</span
                            ><span class="text-amber-300">:</span>
                            <span class="text-emerald-300">'Ready'</span
                            ><span class="text-amber-300">,</span>
                          </div>
                          <div>
                            &nbsp;&nbsp;<span class="text-slate-300">passion</span
                            ><span class="text-amber-300">:</span>
                            <span class="text-lime-300">Infinity</span
                            ><span class="text-amber-300">,</span>
                          </div>
                          <div>
                            &nbsp;&nbsp;<span class="text-slate-300">action</span
                            ><span class="text-amber-300">:</span>
                            <span class="text-amber-300">()</span>
                            <span class="text-violet-300">=&gt;</span>
                            <span class="text-yellow-300">explore</span
                            ><span class="text-amber-300">()</span>
                          </div>
                          <div><span class="text-amber-300">};</span></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div
              class="relative overflow-hidden rounded-xl border border-rose-200/80 bg-rose-50/80 p-4 backdrop-blur-xl dark:border-white/10 dark:bg-white/5"
            >
              <div class="text-xs font-medium tracking-wide text-slate-500 dark:text-slate-400">
                PREVIEW
              </div>
              <div class="mt-2 text-base font-semibold text-slate-900 dark:text-white">
                {{ active.name }}
              </div>
              <div class="mt-2 text-sm leading-6 text-slate-600 dark:text-slate-400">
                {{ active.desc }}
              </div>
              <div
                class="mt-4 overflow-hidden rounded-lg border border-rose-200/80 bg-rose-50/70 dark:border-white/10 dark:bg-black/30"
              >
                <div class="h-28 w-full overflow-y-auto bg-rose-100/70 p-3 dark:bg-white/5">
                  <p class="text-xs leading-5 text-slate-600 dark:text-slate-400">
                    {{ active.detailDesc }}
                  </p>
                </div>
                <div
                  class="flex items-center justify-between border-t border-slate-200 px-3 py-2 dark:border-white/10"
                >
                  <div class="text-xs text-slate-500 dark:text-slate-400">{{ active.href }}</div>
                  <div class="text-xs text-slate-400 dark:text-slate-500">介绍</div>
                </div>
              </div>

              <div class="mt-4 flex items-center gap-2">
                <div class="h-2 w-2 rounded-full bg-emerald-500/80" />
                <div class="text-xs text-slate-500 dark:text-slate-400">悬停卡片查看预览</div>
              </div>

              <div
                class="mt-6 flex min-h-[140px] flex-col justify-between rounded-xl border border-emerald-500/20 bg-white/75 p-4 shadow-[inset_0_1px_0_rgba(255,255,255,0.08)] dark:border-white/10 dark:bg-black/30"
              >
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <div class="text-sm font-semibold text-slate-900 dark:text-white">访客模式</div>
                    <p class="mt-1 text-xs leading-5 text-slate-600 dark:text-slate-400">
                      无需登录，先浏览公开资源、网站详情和平台公告。
                    </p>
                  </div>
                  <div
                    class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-emerald-500/20 bg-emerald-50 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-200"
                  >
                    <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M13 5l7 7-7 7M5 12h14"
                      />
                    </svg>
                  </div>
                </div>
                <button
                  type="button"
                  class="mt-5 inline-flex h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-lg bg-slate-950 px-4 text-sm font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-slate-800 active:scale-[0.98] dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
                  aria-label="以访客身份浏览网站"
                  @click="visitAsGuest"
                >
                  <span>以访客身份浏览</span>
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 12h14M13 5l7 7-7 7"
                    />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
