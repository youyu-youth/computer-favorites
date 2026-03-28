import { ref, computed } from 'vue'

export function useWebsitesData() {
    const viewMode = ref('grid')
    const searchQuery = ref('')

    const categories = ref([
        { name: 'Developer Tools', count: '7,240', icon: 'fas fa-wrench', active: true },
        { name: 'TypeScript', count: '2,856', icon: 'fab fa-js-square', active: false },
        { name: 'Python', count: '2,596', icon: 'fab fa-python', active: false },
        { name: 'Remote', count: '2,542', icon: 'fas fa-cloud', active: false },
        { name: 'Local', count: '2,144', icon: 'fas fa-laptop', active: false },
        { name: 'Hybrid', count: '2,067', icon: 'fas fa-link', active: false },
        { name: 'App Automation', count: '1,305', icon: 'fas fa-robot', active: false },
        { name: 'Claimed', count: '1,150', icon: 'fas fa-check-circle', active: false },
        { name: 'Tools', count: '859', icon: 'fas fa-tools', active: false },
        { name: 'Databases', count: '856', icon: 'fas fa-database', active: false },
        { name: 'Code Execution', count: '749', icon: 'fas fa-code', active: false },
        { name: 'Autonomous Agents', count: '715', icon: 'fas fa-brain', active: false },
    ])

    const tools = ref([
        { title: 'API-delete-a-block', subtitle: 'Notion MCP Server' },
        { title: 'API-retrieve-a-database', subtitle: 'Notion ReadOnly MCP Server' },
        { title: 'API-retrieve-a-page', subtitle: 'Notion ReadOnly MCP Server' },
        { title: 'API-retrieve-a-database', subtitle: 'Notion MCP Server' },
    ])

    const connectors = ref([
        { title: '0nMCP — Universal AI AP...', subtitle: 'io.github.0nork' },
        { title: '123elec-mcp', subtitle: 'io.github.Servicedsi' },
        { title: '1stay', subtitle: 'com.stayker' },
        { title: '1stDibs', subtitle: 'com.1stdibs' },
    ])

    const servers = ref([
        {
            title: 'Docfork',
            author: 'docfork',
            isOfficial: true,
            icon: 'fas fa-file-alt',
            iconBg: 'bg-gradient-to-br from-orange-400 to-orange-600',
            description: 'Provides up-to-date documentation for 9000+ libraries directly in your...',
            tags: [
                { name: 'security', status: 'good' },
                { name: 'license', status: 'good' },
                { name: 'quality', status: 'good' }
            ]
        },
        {
            title: 'RSpace MCP Server',
            author: 'rspace-os',
            isOfficial: true,
            icon: 'fas fa-cube',
            iconBg: 'bg-gradient-to-br from-cyan-400 to-blue-600',
            description: 'A proof-of-concept server that enables LLM agents to interact with RSpace...',
            tags: [
                { name: 'security', status: 'good' },
                { name: 'license', status: 'good' },
                { name: 'quality', status: 'good' }
            ]
        },
        {
            title: 'GitHub MCP',
            author: 'github',
            isOfficial: true,
            icon: 'fab fa-github',
            iconBg: 'bg-gray-800',
            description: 'Official GitHub integration for MCP. Manage repositories, issues, and pull requests directly from your AI agent context.',
            tags: [
                { name: 'security', status: 'good' },
                { name: 'license', status: 'good' },
                { name: 'quality', status: 'good' }
            ]
        },
        {
            title: 'PostgreSQL Explorer',
            author: 'data-tools',
            isOfficial: false,
            icon: 'fas fa-database',
            iconBg: 'bg-blue-800',
            description: 'Securely query and explore your PostgreSQL databases. Features read-only modes and query sanitization.',
            tags: [
                { name: 'security', status: 'warning' },
                { name: 'license', status: 'good' },
                { name: 'quality', status: 'good' }
            ]
        }
    ])

    const filteredServers = computed(() => {
        if (!searchQuery.value) return servers.value;
        const query = searchQuery.value.toLowerCase();
        return servers.value.filter(server =>
            server.title.toLowerCase().includes(query) ||
            server.description.toLowerCase().includes(query)
        );
    });

    const currentPage = ref(1);
    const totalPages = ref(1697);

    const prevPage = () => {
        if (currentPage.value > 1) currentPage.value--;
    };

    const nextPage = () => {
        if (currentPage.value < totalPages.value) currentPage.value++;
    };

    const goToPage = (page: number | string) => {
        if (page !== '...' && typeof page === 'number') currentPage.value = page;
    };

    const visiblePages = computed(() => {
        if (totalPages.value <= 7) {
            return Array.from({ length: totalPages.value }, (_, i) => i + 1);
        }
        if (currentPage.value <= 3) {
            return [1, 2, 3, 4, '...', totalPages.value - 1, totalPages.value];
        }
        if (currentPage.value >= totalPages.value - 2) {
            return [1, 2, '...', totalPages.value - 3, totalPages.value - 2, totalPages.value - 1, totalPages.value];
        }
        return [1, '...', currentPage.value - 1, currentPage.value, currentPage.value + 1, '...', totalPages.value];
    });

    return {
        viewMode,
        searchQuery,
        categories,
        tools,
        connectors,
        servers,
        filteredServers,
        currentPage,
        totalPages,
        prevPage,
        nextPage,
        goToPage,
        visiblePages
    }
}
