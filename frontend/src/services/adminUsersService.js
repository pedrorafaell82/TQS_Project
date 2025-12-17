/**
 * Serviço de Administração de Utilizadores
 * User Story: SOUN-23 – User Management
 *
 * Responsável por comunicar com o backend para gestão de utilizadores
 */

const API_BASE_URL = "http://localhost:8080/api/admin/users";

/**
 * Função generica para executar pedidos HTTP ao backend
 * Inclui cookies de sessão para autenticação (ADMIN)
 *
 * @param {string} url
 * @param {object} options
 * @returns {Promise<any>}
 */
async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: "include", 
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
    });

    if (!response.ok) {
        const msg = await response.text().catch(() => "");
        throw new Error(msg || `Erro HTTP: ${response.status}`);
    }

    // Alguns endpoints (PATCH/DELETE) podem não devolver body
    if (response.status === 204) return null;

    return response.json();
}

/**
 * Obtem todos os utilizadores da plataforma
 * (owners e renters)
 *
 * @returns {Promise<Array>}
 */
export async function getAllUsers() {
    return request(API_BASE_URL);
}

/**
 * Ativa ou desativa um utilizador
 *
 * @param {number} userId
 * @param {boolean} active
 */
export async function setUserActive(userId, active) {
    return request(`${API_BASE_URL}/${userId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ active }),
    });
}

/**
 * Atualiza o papel (role) de um utilizador
 *
 * @param {number} userId
 * @param {string} role
 */
export async function updateUserRole(userId, role) {
    return request(`${API_BASE_URL}/${userId}/role`, {
        method: "PATCH",
        body: JSON.stringify({ role }),
    });
}
