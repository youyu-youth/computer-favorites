/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: [
          'Inter',
          'ui-sans-serif',
          'system-ui',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Roboto',
          'Helvetica Neue',
          'Arial',
          'sans-serif',
        ],
        headline: ['Manrope', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        label: ['Inter', 'sans-serif'],
      },
      colors: {
        brand: {
          orange: '#e95322',
          blue: '#3b82f6',
          green: '#10b981',
        },
        brandOrange: '#e95322',
        brandBlue: '#3b82f6',
        brandGreen: '#10b981',
        primary: {
          50: 'rgb(var(--cf-color-primary-50-rgb) / <alpha-value>)',
          100: 'rgb(var(--cf-color-primary-100-rgb) / <alpha-value>)',
          400: 'rgb(var(--cf-color-primary-400-rgb) / <alpha-value>)',
          500: 'rgb(var(--cf-color-primary-500-rgb) / <alpha-value>)',
          600: 'rgb(var(--cf-color-primary-600-rgb) / <alpha-value>)',
        },
        secondary: {
          50: 'rgb(var(--cf-color-secondary-50-rgb) / <alpha-value>)',
          100: 'rgb(var(--cf-color-secondary-100-rgb) / <alpha-value>)',
          400: 'rgb(var(--cf-color-secondary-400-rgb) / <alpha-value>)',
          500: 'rgb(var(--cf-color-secondary-500-rgb) / <alpha-value>)',
          600: 'rgb(var(--cf-color-secondary-600-rgb) / <alpha-value>)',
        },
        tertiary: {
          50: 'rgb(var(--cf-color-tertiary-50-rgb) / <alpha-value>)',
          100: 'rgb(var(--cf-color-tertiary-100-rgb) / <alpha-value>)',
          400: 'rgb(var(--cf-color-tertiary-400-rgb) / <alpha-value>)',
          500: 'rgb(var(--cf-color-tertiary-500-rgb) / <alpha-value>)',
          600: 'rgb(var(--cf-color-tertiary-600-rgb) / <alpha-value>)',
        },
        neutral: {
          50: 'rgb(var(--cf-color-neutral-50-rgb) / <alpha-value>)',
          100: 'rgb(var(--cf-color-neutral-100-rgb) / <alpha-value>)',
          400: 'rgb(var(--cf-color-neutral-400-rgb) / <alpha-value>)',
          500: 'rgb(var(--cf-color-neutral-500-rgb) / <alpha-value>)',
          600: 'rgb(var(--cf-color-neutral-600-rgb) / <alpha-value>)',
        },
        semantic: {
          success: 'rgb(var(--cf-color-success-rgb) / <alpha-value>)',
          error: 'rgb(var(--cf-color-error-rgb) / <alpha-value>)',
          warning: 'rgb(var(--cf-color-warning-rgb) / <alpha-value>)',
          info: 'rgb(var(--cf-color-info-rgb) / <alpha-value>)',
        },
        surface: {
          page: 'rgb(var(--cf-color-surface-page-rgb) / <alpha-value>)',
          card: 'rgb(var(--cf-color-surface-card-rgb) / <alpha-value>)',
        },
        text: {
          primary: 'rgb(var(--cf-color-text-primary-rgb) / <alpha-value>)',
          secondary: 'rgb(var(--cf-color-text-secondary-rgb) / <alpha-value>)',
        },
        border: {
          default: 'rgb(var(--cf-color-border-default-rgb) / <alpha-value>)',
          muted: 'rgb(var(--cf-color-border-muted-rgb) / <alpha-value>)',
        },
        dark: {
          bg: 'rgb(var(--cf-color-dark-bg-rgb) / <alpha-value>)',
          card: 'rgb(var(--cf-color-dark-card-rgb) / <alpha-value>)',
          border: 'rgb(var(--cf-color-dark-border-rgb) / <alpha-value>)',
        },
        darkBg: 'rgb(var(--cf-color-dark-bg-rgb) / <alpha-value>)',
        darkCard: 'rgb(var(--cf-color-dark-card-rgb) / <alpha-value>)',
        darkBorder: 'rgb(var(--cf-color-dark-border-rgb) / <alpha-value>)',
      },
    },
  },
  plugins: [],
}
