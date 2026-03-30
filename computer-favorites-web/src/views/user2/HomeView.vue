<template>
  <div class="font-body selection:bg-primary-500 selection:text-white min-h-screen text-text-primary">
    <!-- Hero Section: Terminal Style -->
    <section class="max-w-7xl mx-auto px-8 py-24 md:py-32 flex flex-col items-start gap-6">
        <div class="font-mono text-primary-500 text-sm tracking-widest uppercase opacity-80 mb-2">
          &gt; initial_payload_received.exe
        </div>
        <h1 class="font-headline text-5xl md:text-7xl font-extrabold tracking-tighter text-text-primary max-w-4xl leading-none">
          Curated intelligence for <span class="text-primary-500">hyper-scale</span> engineers.
        </h1>
        <div class="flex items-center gap-2 font-mono text-xl md:text-2xl text-text-primary bg-surface-card p-4 cyber-border w-full max-w-2xl mt-4">
          <span class="text-primary-500">$</span>
          <span>locate --all resources where stack="future"</span>
          <span class="terminal-cursor"></span>
        </div>
        <p class="text-text-secondary max-w-xl text-lg mt-2">
          A high-density archive of technical gold, curated for the modern developer who demands editorial precision over noise.
        </p>
      </section>

      <!-- Filter Bar -->
      <section class="max-w-7xl mx-auto px-8 mb-12">
        <div class="flex flex-wrap items-center gap-4 border-b border-border-default pb-4">
          <Button unstyled class="px-6 py-2 border border-primary-500 text-primary-500 font-label text-xs uppercase tracking-widest bg-primary-500/5 cursor-pointer">
            All Treasures
          </Button>
          <Button unstyled v-for="cat in ['Frontend', 'Infrastructure', 'AI/ML Models', 'Security Labs']" :key="cat" class="px-6 py-2 border border-transparent text-text-secondary hover:text-text-primary font-label text-xs uppercase tracking-widest transition-colors cursor-pointer">
            {{ cat }}
          </Button>
        </div>
      </section>

      <!-- Resource Grid: Uniform 3-Column Grid -->
      <section class="max-w-7xl mx-auto px-8 pb-32">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
          <!-- Card Component abstracted within loop for brevity -->
          <div v-for="card in cards" :key="card.title" class="liquid-glass group transition-all duration-300 p-8 flex flex-col h-full rounded-lg cf-surface-card">
            <div class="mb-6">
              <span class="material-symbols-outlined text-4xl text-primary-500 mb-4">{{ card.icon }}</span>
              <h3 class="font-headline text-xl font-bold text-text-primary mb-2 group-hover:text-primary-500 transition-colors">
                {{ card.title }}
              </h3>
              <p class="text-text-secondary text-sm leading-relaxed mb-6">{{ card.desc }}</p>
            </div>
            <div class="mt-auto">
              <div class="flex flex-wrap gap-2 mb-6">
                <!-- tags -->
                <span class="text-[9px] font-bold py-1 px-2 border border-border-default text-text-secondary uppercase">{{ card.tag1 }}</span>
                <span class="text-[9px] font-bold py-1 px-2 border border-primary-500/40 text-primary-500 uppercase">{{ card.tag2 }}</span>
              </div>
              <div class="flex items-center justify-between border-t border-border-default pt-4">
                <div class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-xs text-primary-500">person</span>
                  <span class="text-text-secondary text-[10px] font-bold uppercase tracking-tight">{{ card.author }}</span>
                </div>
                <div class="flex items-center gap-3 text-text-secondary">
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-xs">star</span>
                    <span class="text-[10px]">{{ card.stars }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-xs">bookmark</span>
                    <span class="text-[10px]">{{ card.bookmarks }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="flex items-center justify-center gap-2">
          <Button unstyled class="w-10 h-10 flex items-center justify-center border border-border-default text-text-secondary hover:text-text-primary hover:border-text-primary transition-colors rounded-sm cursor-pointer">
            <span class="material-symbols-outlined">chevron_left</span>
          </Button>
          <Button unstyled class="w-10 h-10 flex items-center justify-center border border-primary-500 bg-primary-500/10 text-primary-500 font-mono text-sm rounded-sm cursor-pointer">01</Button>
          <Button unstyled class="w-10 h-10 flex items-center justify-center border border-border-default text-text-primary hover:border-text-primary transition-colors font-mono text-sm rounded-sm cursor-pointer">02</Button>
          <Button unstyled class="w-10 h-10 flex items-center justify-center border border-border-default text-text-primary hover:border-text-primary transition-colors font-mono text-sm rounded-sm cursor-pointer">03</Button>
          <span class="text-text-secondary px-2">...</span>
          <Button unstyled class="w-10 h-10 flex items-center justify-center border border-border-default text-text-primary hover:border-text-primary transition-colors font-mono text-sm rounded-sm cursor-pointer">12</Button>
          <Button unstyled class="w-10 h-10 flex items-center justify-center border border-border-default text-text-secondary hover:text-text-primary hover:border-text-primary transition-colors rounded-sm cursor-pointer">
            <span class="material-symbols-outlined">chevron_right</span>
          </Button>
        </div>
      </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Button from 'primevue/button'

const cards = ref([
  {
    icon: 'hub',
    title: 'Zero-Knowledge Proofs in Distributed Systems',
    desc: 'In-depth guide to privacy-first infrastructure using modern cryptographic primitives and Rust patterns.',
    tag1: 'Backend',
    tag2: 'Crypto',
    author: 'Alex Chen',
    stars: '1.2k',
    bookmarks: '482'
  },
  {
    icon: 'terminal',
    title: 'Bash Productivity Hacks for Kernel Contribs',
    desc: 'Automate your patch submission workflow with these 12 hidden scripts used by top maintainers.',
    tag1: 'Sysadmin',
    tag2: 'Linux',
    author: 'Marcus Thorne',
    stars: '892',
    bookmarks: '156'
  },
  {
    icon: 'javascript',
    title: 'Web-Assembly Boilerplate for 2024',
    desc: 'The ultimate starting point for high-performance web applications using Rust and WASM-bindgen.',
    tag1: 'Frontend',
    tag2: 'Performance',
    author: 'Elena V.',
    stars: '2.1k',
    bookmarks: '930'
  },
  {
    icon: 'psychology',
    title: 'Mastering Transformer Models: Visual Guide',
    desc: 'A visual breakdown of self-attention mechanisms and multi-head attention in modern LLMs.',
    tag1: 'AI/ML',
    tag2: 'Education',
    author: 'Sarah Jenkins',
    stars: '3.4k',
    bookmarks: '1.1k'
  },
  {
    icon: 'shield',
    title: 'Post-Quantum Cryptography Prep',
    desc: 'Implementing NIST-approved quantum-resistant algorithms in your existing security stack today.',
    tag1: 'Security',
    tag2: 'Advanced',
    author: 'Dr. Aris',
    stars: '561',
    bookmarks: '224'
  },
  {
    icon: 'settings_ethernet',
    title: 'Infrastructure as Code: 2024 Patterns',
    desc: 'Evolution of IaC from Terraform to Crossplane and Pulumi. Best practices for platform engineering.',
    tag1: 'Cloud',
    tag2: 'DevOps',
    author: 'Liam Rossi',
    stars: '1.5k',
    bookmarks: '670'
  }
])
</script>

<style scoped>
/* Scoped styles overrides to ensure terminal cursor animates correctly */
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.terminal-cursor {
  animation: blink 1s step-end infinite;
}
</style>
