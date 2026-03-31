<template>
  <div class="min-h-screen w-full flex flex-col bg-[rgb(var(--cf-color-surface-page-rgb)/1)] font-body text-[rgb(var(--cf-color-text-primary-rgb)/1)] m-0 p-0">
    <!-- 左右分栏容器 -->
    <main class="flex flex-1 flex-col md:flex-row">
      <!-- 左侧展示区域 -->
      <section class="relative hidden items-center justify-center overflow-hidden border-r border-black/35 bg-[rgb(var(--cf-color-surface-page-rgb)/1)] p-12 md:flex md:w-1/2 lg:w-3/5">
        <div class="absolute inset-0 opacity-[0.05] dark:opacity-[0.12]" style="background-image: radial-gradient(circle at 2px 2px, rgb(var(--cf-color-primary-500-rgb)) 1px, transparent 0); background-size: 48px 48px;"></div>
        <div class="absolute -top-48 -left-48 h-full w-full rounded-full bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)] blur-[180px] opacity-20 pointer-events-none"></div>

        <div class="relative z-10 w-full max-w-3xl space-y-12">
          <header>
            <h1 class="font-headline text-4xl font-extrabold leading-tight tracking-tight text-[rgb(var(--cf-color-text-primary-rgb)/1)] lg:text-5xl">
              为生产而优化的<br>
              <span class="text-[rgb(var(--cf-color-primary-500-rgb)/1)]">精选工作区。</span>
            </h1>
            <p class="mt-4 max-w-lg text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">
              在统一视图中访问您精选的代码块、团队协作以及部署状态。
            </p>
          </header>

          <!-- 动态能力卡片区域 -->
          <div class="grid grid-cols-12 items-start gap-6 rounded-2xl p-6 md:p-8 bg-[#4d8a74]/10 dark:bg-[#4d8a74]/15 backdrop-blur-xl border border-[#4d8a74]/30 dark:border-[#4d8a74]/40 shadow-[0_8px_32px_0_rgba(77,138,116,0.15)] dark:shadow-[0_8px_32px_0_rgba(77,138,116,0.25)] transition-all duration-500 hover:bg-[#4d8a74]/15 dark:hover:bg-[#4d8a74]/25">
            <div class="liquid-glass col-span-12 overflow-hidden rounded-xl shadow-2xl transition-all duration-500 lg:col-span-8">
              <div class="flex items-center justify-between border-b border-black/15 bg-black/[0.06] px-4 py-2 dark:border-black/40 dark:bg-black/40">
                <div class="flex gap-1.5">
                  <div class="h-3 w-3 rounded-full bg-red-500/80 dark:bg-red-500/50"></div>
                  <div class="h-3 w-3 rounded-full bg-amber-500/80 dark:bg-amber-500/50"></div>
                  <div class="h-3 w-3 rounded-full bg-emerald-500/80 dark:bg-emerald-500/50"></div>
                </div>
                <span class="font-mono text-[10px] uppercase tracking-widest text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">useAuth.ts</span>
              </div>

              <div class="overflow-x-auto p-6 font-mono text-sm leading-relaxed">
                <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div class="group rounded-lg border border-black/15 bg-black/[0.03] p-4 transition-all hover:border-[rgb(var(--cf-color-primary-500-rgb)/0.4)] dark:border-black/40 dark:bg-black/25">
                    <div class="mb-3 flex items-center gap-3">
                      <div class="flex h-8 w-8 items-center justify-center rounded bg-[rgb(var(--cf-color-primary-500-rgb)/0.2)]">
                        <i class="pi pi-sitemap text-xl text-[rgb(var(--cf-color-primary-500-rgb)/1)]"></i>
                      </div>
                      <div>
                        <h4 class="text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)] transition-colors group-hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]">React 框架</h4>
                        <p class="text-[10px] text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">基于组件的 UI</p>
                      </div>
                    </div>
                    <div class="flex gap-2">
                      <span class="rounded-full border border-black/15 bg-black/[0.05] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-text-secondary-rgb)/1)] dark:border-black/40 dark:bg-black/30">v18.2</span>
                      <span class="rounded-full border border-[rgb(var(--cf-color-primary-500-rgb)/0.2)] bg-[rgb(var(--cf-color-primary-500-rgb)/0.1)] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-primary-500-rgb)/1)]">已验证</span>
                    </div>
                  </div>

                  <div class="group rounded-lg border border-black/15 bg-black/[0.03] p-4 transition-all hover:border-[rgb(var(--cf-color-primary-500-rgb)/0.4)] dark:border-black/40 dark:bg-black/25">
                    <div class="mb-3 flex items-center gap-3">
                      <div class="flex h-8 w-8 items-center justify-center rounded bg-emerald-500/10">
                        <i class="pi pi-clone text-xl text-emerald-500"></i>
                      </div>
                      <div>
                        <h4 class="text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)] transition-colors group-hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]">Vue 引擎</h4>
                        <p class="text-[10px] text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">响应式核心</p>
                      </div>
                    </div>
                    <div class="flex gap-2">
                      <span class="rounded-full border border-black/15 bg-black/[0.05] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-text-secondary-rgb)/1)] dark:border-black/40 dark:bg-black/30">v3.4</span>
                      <span class="rounded-full border border-black/15 bg-black/[0.05] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-text-secondary-rgb)/1)] dark:border-black/40 dark:bg-black/30">核心</span>
                    </div>
                  </div>

                  <div class="group rounded-lg border border-black/15 bg-black/[0.03] p-4 transition-all hover:border-[rgb(var(--cf-color-primary-500-rgb)/0.4)] dark:border-black/40 dark:bg-black/25">
                    <div class="mb-3 flex items-center gap-3">
                      <div class="flex h-8 w-8 items-center justify-center rounded bg-sky-500/10">
                        <i class="pi pi-palette text-xl text-sky-500"></i>
                      </div>
                      <div>
                        <h4 class="text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)] transition-colors group-hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]">Tailwind 设计</h4>
                        <p class="text-[10px] text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">实用优先样式</p>
                      </div>
                    </div>
                    <div class="flex gap-2">
                      <span class="rounded-full border border-black/15 bg-black/[0.05] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-text-secondary-rgb)/1)] dark:border-black/40 dark:bg-black/30">v3.4</span>
                      <span class="rounded-full border border-[rgb(var(--cf-color-primary-500-rgb)/0.2)] bg-[rgb(var(--cf-color-primary-500-rgb)/0.1)] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-primary-500-rgb)/1)]">活跃</span>
                    </div>
                  </div>

                  <div class="group rounded-lg border border-black/15 bg-black/[0.03] p-4 transition-all hover:border-[rgb(var(--cf-color-primary-500-rgb)/0.4)] dark:border-black/40 dark:bg-black/25">
                    <div class="mb-3 flex items-center gap-3">
                      <div class="flex h-8 w-8 items-center justify-center rounded bg-blue-500/10">
                        <i class="pi pi-code text-xl text-blue-500"></i>
                      </div>
                      <div>
                        <h4 class="text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)] transition-colors group-hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]">TypeScript</h4>
                        <p class="text-[10px] text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">静态类型</p>
                      </div>
                    </div>
                    <div class="flex gap-2">
                      <span class="rounded-full border border-black/15 bg-black/[0.05] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-text-secondary-rgb)/1)] dark:border-black/40 dark:bg-black/30">v5.0</span>
                      <span class="rounded-full border border-black/15 bg-black/[0.05] px-2 py-0.5 text-[9px] uppercase tracking-wider text-[rgb(var(--cf-color-text-secondary-rgb)/1)] dark:border-black/40 dark:bg-black/30">严格</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-span-12 space-y-6 lg:col-span-4">
              <div class="liquid-glass rounded-xl p-5 shadow-2xl transition-all duration-300">
                <div class="mb-4 flex items-center justify-between">
                  <i class="pi pi-server text-2xl font-bold text-[rgb(var(--cf-color-primary-500-rgb)/1)]"></i>
                  <div class="flex items-center gap-2">
                    <div class="animate-pulse-gold h-2 w-2 rounded-full bg-emerald-500"></div>
                    <span class="text-[10px] font-bold uppercase tracking-widest text-emerald-500">已同步</span>
                  </div>
                </div>
                <h3 class="font-headline text-lg font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)]">优先仓库</h3>
                <p class="mt-1 mb-4 text-xs text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">main/dev-env-configs</p>
                <div class="h-1 w-full overflow-hidden rounded-full bg-black/[0.08] dark:bg-black/30">
                  <div class="h-full w-3/4 bg-[rgb(var(--cf-color-primary-500-rgb)/1)] shadow-[0_0_8px_rgba(217,119,6,0.6)]"></div>
                </div>
              </div>

              <div class="liquid-glass rounded-xl p-5 shadow-2xl transition-all duration-300">
                <div class="mb-1 text-[10px] font-bold uppercase tracking-widest text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">全局资产</div>
                <div class="flex items-end gap-2">
                  <span class="font-headline text-3xl font-extrabold text-[rgb(var(--cf-color-text-primary-rgb)/1)]">4.2k</span>
                  <span class="mb-1 text-xs font-bold text-[rgb(var(--cf-color-primary-500-rgb)/1)]">+12%</span>
                </div>
                <p class="mt-1 text-xs text-[rgb(var(--cf-color-text-secondary-rgb)/0.8)]">已保存代码片段</p>
              </div>
            </div>

            <div class="liquid-glass col-span-12 flex items-center justify-between rounded-xl p-4">
              <div class="flex items-center gap-4">
                <div class="flex -space-x-3">
                  <img alt="User" class="h-10 w-10 rounded-full border-2 border-[rgb(var(--cf-color-surface-page-rgb)/1)] ring-1 ring-black/15 dark:ring-black/45" src="https://lh3.googleusercontent.com/aida-public/AB6AXuA58Zi-avnk6RLylZ8pnhFPbb6cBJJuvyIyPFtzGZdwC7bMvtZT6gxG1tJCYNLBI4n8shvT9lCejK9a6R9ykw7FrUGNYQouDGjfTiQmBkiK_-FHOXQaiMFihPKIGT1cbdoPtx1bXfq_gUkj-v9QKixOkZ5I3soqeaOoKJPgGysIgUkGwFnM_QUhTPC1TVKpvTgNqpE-Bdu9g81Ak4BWWNAZGsvcITiLuHqsQeeM1nAo8EP1egYy6URaqoQe-TzfCiFURoodqbNiafrG">
                  <img alt="User" class="h-10 w-10 rounded-full border-2 border-[rgb(var(--cf-color-surface-page-rgb)/1)] ring-1 ring-black/15 dark:ring-black/45" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDpGaW7tLWwaxjKqYK-yJlsqJG8kNRDvrEMNNjxZe1UP1ZdAr7QRr53CWxOeumfkHeq6G627NbcGDU6e8AUlqdD741l7YOOt-oszfr-EXv-ua5YPLsEtKGFvq77Pw5Qm-iMWfHgdp_DSueHBTuvpHbEL2CyWJI-4l49S5gYysMZcc7rB5DtGzqXbeh6TdJ5YuT6tyJYhE-O6x8ckXnZaSL1w7gnHGrG3kuZDowlxHsfb_EFkjJhArIYQDLeIbenJdgUYM91o1rL7D40">
                  <img alt="User" class="h-10 w-10 rounded-full border-2 border-[rgb(var(--cf-color-surface-page-rgb)/1)] ring-1 ring-black/15 dark:ring-black/45" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDG-6cUjt0eu277_6Z8kqSSBVEyH30vezq-P25DXNA0gTQy_doMjhEC1Rr3myaw4UstG_OISsFrsxZ9wxEqhwO5RH4skjRSoWKyWgMyKmpOZj2DwfWq7Hjd_nl1GWXwRYrzjF50HXsCihEmIBbo62Hi1unlb3sVkvotALZ2jgvvJPYtJuLFSYKDbeb_PAKs5ybkE1E16bzVyENz_GjyZSt_FFxjKHjhpJpMgPbyGy6pkXOJ37orEwDb9NVQJyd3y5LFxnVMZcTtQSar">
                  <div class="flex h-10 w-10 items-center justify-center rounded-full border-2 border-[rgb(var(--cf-color-surface-page-rgb)/1)] bg-black/[0.08] text-[10px] font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)] ring-1 ring-black/15 dark:bg-[rgb(var(--cf-color-dark-card-rgb)/1)] dark:ring-black/45">
                    +5
                  </div>
                </div>
                <div>
                  <span class="block text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)]">活跃团队</span>
                  <span class="text-[10px] uppercase tracking-widest text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">在线协作者</span>
                </div>
              </div>
              <button class="cursor-pointer rounded-lg border border-black/15 bg-black/[0.05] p-2 text-[rgb(var(--cf-color-text-secondary-rgb)/1)] transition-colors hover:bg-black/[0.08] hover:text-[rgb(var(--cf-color-text-primary-rgb)/1)] dark:border-black/45 dark:bg-black/25 dark:hover:bg-black/35 dark:hover:text-white" type="button">
                <i class="pi pi-ellipsis-h"></i>
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 右侧登录表单区域 -->
      <section class="relative flex flex-1 items-center justify-center border-l border-black/35 bg-[rgb(var(--cf-color-surface-page-rgb)/1)] p-4 md:p-8 overflow-hidden z-0">
        <!-- 右侧背景装饰，用于凸显玻璃质感 -->
        <div class="absolute inset-0 opacity-[0.03] dark:opacity-[0.08] pointer-events-none" style="background-image: radial-gradient(circle at 2px 2px, rgb(var(--cf-color-primary-500-rgb)) 1px, transparent 0); background-size: 32px 32px;"></div>
        <div class="absolute -bottom-48 -right-48 h-full w-full rounded-full bg-[#4d8a74] blur-[160px] opacity-10 dark:opacity-20 pointer-events-none z-[-1]"></div>
        <div class="absolute top-1/4 -right-1/4 h-64 w-64 rounded-full bg-blue-500 blur-[120px] opacity-10 dark:opacity-[0.15] pointer-events-none z-[-1]"></div>

        <div class="w-full max-w-md relative z-10 glass-panel rounded-2xl p-8 sm:p-10 border border-black/5 dark:border-white/10 shadow-2xl transition-all duration-300">
          <div class="mb-10 text-center md:text-left">
            <h2 class="font-headline mb-4 text-3xl font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)]">欢迎回来</h2>
            <p class="font-medium text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">请输入您的详细信息以登录。</p>
          </div>

          <form class="space-y-8" @submit.prevent="handleLogin">
            <div>
              <label class="mb-3 block text-xs font-semibold uppercase tracking-widest text-[rgb(var(--cf-color-text-secondary-rgb)/1)]" for="email">电子邮箱</label>
              <InputText
                id="email"
                v-model="email"
                type="email"
                autocomplete="username"
                placeholder="name@company.com"
                required
                :pt="inputPt"
              />
            </div>

            <div>
              <div class="mb-3 flex items-center justify-between">
                <label class="block text-xs font-semibold uppercase tracking-widest text-[rgb(var(--cf-color-text-secondary-rgb)/1)]" for="password">密码</label>
                <a class="text-xs font-bold text-[rgb(var(--cf-color-primary-500-rgb)/1)] transition-colors hover:text-[rgb(var(--cf-color-primary-600-rgb)/1)]" href="#">忘记密码？</a>
              </div>
              <div class="relative">
                <InputText
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="current-password"
                  placeholder="••••••••"
                  required
                  :pt="inputPt"
                />
                <button
                  class="absolute top-1/2 right-3 -translate-y-1/2 text-[rgb(var(--cf-color-text-secondary-rgb)/1)] transition-colors hover:text-[rgb(var(--cf-color-text-primary-rgb)/1)]"
                  type="button"
                  @click="showPassword = !showPassword"
                >
                  <i :class="['pi', showPassword ? 'pi-eye-slash' : 'pi-eye', 'text-[20px]']"></i>
                </button>
              </div>
            </div>

            <div class="pt-6">
              <Button
                type="submit"
                label="登录控制台"
                class="inline-flex w-full cursor-pointer justify-center rounded-lg border-none bg-[rgb(var(--cf-color-primary-500-rgb)/1)] py-3 text-center text-sm font-headline font-bold text-white shadow-lg shadow-[rgb(var(--cf-color-primary-500-rgb)/0.15)] transition-all duration-200 hover:brightness-110 hover:shadow-[rgb(var(--cf-color-primary-500-rgb)/0.25)] active:scale-[0.98]"
              />
            </div>

            <div class="my-8 flex justify-center text-[10px] uppercase tracking-[0.2em]">
              <span class="font-semibold text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">或使用以下方式继续</span>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <Button type="button" class="flex cursor-pointer items-center justify-center gap-2 rounded-lg border border-black/20 bg-black/[0.03] px-4 py-2.5 transition-all duration-200 hover:bg-black/[0.06] dark:border-black/45 dark:bg-[rgb(var(--cf-color-dark-card-rgb)/1)] dark:hover:bg-black">
                <svg class="h-4 w-4" viewBox="0 0 24 24">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="currentColor"></path>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="currentColor"></path>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="currentColor"></path>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.66l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="currentColor"></path>
                </svg>
                <span class="text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)]">Google</span>
              </Button>
              <Button type="button" class="flex cursor-pointer items-center justify-center gap-2 rounded-lg border border-black/20 bg-black/[0.03] px-4 py-2.5 transition-all duration-200 hover:bg-black/[0.06] dark:border-black/45 dark:bg-[rgb(var(--cf-color-dark-card-rgb)/1)] dark:hover:bg-black">
                <svg class="h-4 w-4" viewBox="0 0 24 24">
                  <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" fill="currentColor"></path>
                </svg>
                <span class="text-sm font-bold text-[rgb(var(--cf-color-text-primary-rgb)/1)]">GitHub</span>
              </Button>
            </div>

            <p class="pt-6 text-center text-sm text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">
              还没有账号？
              <a class="font-bold text-[rgb(var(--cf-color-primary-500-rgb)/1)] hover:underline" href="#">免费注册</a>
            </p>
          </form>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="w-full border-t border-black/35 bg-[rgb(var(--cf-color-surface-page-rgb)/1)] py-8">
      <div class="mx-auto flex w-full max-w-7xl flex-col items-center justify-between gap-4 px-8 md:flex-row">
        <div class="font-headline font-bold text-[rgb(var(--cf-color-primary-500-rgb)/1)]">DEV CURATOR</div>
        <nav class="flex flex-wrap justify-center gap-6">
          <a class="font-headline text-[10px] uppercase tracking-[0.2em] text-[rgb(var(--cf-color-text-secondary-rgb)/1)] transition-colors duration-200 hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]" href="#">隐私政策</a>
          <a class="font-headline text-[10px] uppercase tracking-[0.2em] text-[rgb(var(--cf-color-text-secondary-rgb)/1)] transition-colors duration-200 hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]" href="#">服务条款</a>
          <a class="font-headline text-[10px] uppercase tracking-[0.2em] text-[rgb(var(--cf-color-text-secondary-rgb)/1)] transition-colors duration-200 hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]" href="#">安全</a>
          <a class="font-headline text-[10px] uppercase tracking-[0.2em] text-[rgb(var(--cf-color-text-secondary-rgb)/1)] transition-colors duration-200 hover:text-[rgb(var(--cf-color-primary-500-rgb)/1)]" href="#">状态</a>
        </nav>
        <div class="font-headline text-[10px] uppercase tracking-[0.2em] text-[rgb(var(--cf-color-text-secondary-rgb)/1)]">© 2024 Developer Curator. 保留所有权利。</div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

// 登录表单状态
const email = ref('')
const password = ref('')
const showPassword = ref(false)

// PrimeVue InputText 在 unstyled 模式下通过 pt 精准注入类名
const inputPt = {
  root: {
    class:
      'w-full rounded-lg bg-black/[0.03] px-3.5 py-2.5 text-sm text-[rgb(var(--cf-color-text-primary-rgb)/1)] ring-1 ring-black/20 transition-all duration-200 outline-none placeholder:text-[rgb(var(--cf-color-text-secondary-rgb)/0.75)] focus:ring-2 focus:ring-[rgb(var(--cf-color-primary-500-rgb)/1)] dark:bg-[#2C2C2E] dark:ring-[#3A3A3C]',
  },
} as const

const handleLogin = () => {
  console.log('登录尝试', {
    email: email.value
  })
}
</script>

<style scoped>
@keyframes pulse-gold {
  0%,
  100% {
    opacity: 0.5;
  }

  50% {
    opacity: 1;
  }
}

.animate-pulse-gold {
  animation: pulse-gold 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* 右侧表单玻璃面板 */
.glass-panel {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

:global(.dark .glass-panel) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.01) 100%);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  box-shadow:
    0 8px 32px 0 rgba(0, 0, 0, 0.3),
    inset 0 1px 1px 0 rgba(255, 255, 255, 0.1),
    inset 0 0 0 1px rgba(255, 255, 255, 0.05);
}

/* 玻璃拟态容器 */
.liquid-glass {
  background: rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  box-shadow:
    0 8px 32px 0 rgba(0, 0, 0, 0.08),
    inset 0 0 0 1px rgba(0, 0, 0, 0.2);
}

:global(.dark .liquid-glass) {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 0, 0, 0.45);
  box-shadow:
    0 8px 32px 0 rgba(0, 0, 0, 0.8),
    inset 0 0 0 1px rgba(0, 0, 0, 0.32);
}

.liquid-glass:hover {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.2);
}

:global(.dark .liquid-glass:hover) {
  background: rgba(0, 0, 0, 0.32);
  border-color: rgba(0, 0, 0, 0.7);
  box-shadow:
    0 8px 32px 0 rgba(0, 0, 0, 0.8),
    inset 0 0 0 1px rgba(0, 0, 0, 0.42);
}

/* 修复浏览器自动填充在暗黑模式下将输入框背景刷白的问题 */
:global(.dark input[data-pc-name='inputtext']:-webkit-autofill),
:global(.dark input[data-pc-name='inputtext']:-webkit-autofill:hover),
:global(.dark input[data-pc-name='inputtext']:-webkit-autofill:focus),
:global(.dark input[data-pc-name='inputtext']:-webkit-autofill:active) {
  background-color: #2c2c2e !important;
  color: rgb(var(--cf-color-text-primary-rgb) / 1) !important;
  -webkit-text-fill-color: rgb(var(--cf-color-text-primary-rgb) / 1) !important;
  -webkit-box-shadow: 0 0 0 1000px #2c2c2e inset !important;
  box-shadow: 0 0 0 1000px #2c2c2e inset !important;
  caret-color: rgb(var(--cf-color-text-primary-rgb) / 1) !important;
  border-radius: 0.5rem;
  transition: background-color 9999s ease-out 0s;
}
</style>
