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
  plugins: [require('@tailwindcss/typography')],
}
