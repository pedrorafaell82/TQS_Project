/**
 * Base URL: http://localhost:8080/api
 */

import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true 
});


export const getInstruments = () => api.get('/instruments');

/**
 * Obtem um instrumento pelo ID
 */
export const getInstrumentById = (id) => api.get(`/instruments/${id}`);

/**
 * Cria um novo instrumento
 * (Owner)
 */
export const createInstrument = (instrument) =>
    api.post('/instruments', instrument);

/**
 * Ativa ou desativa um instrumento
 * (Owner)
 *
 * @param {number} id
 * @param {boolean} active
 */
export const updateInstrumentStatus = (id, active) =>
    api.patch(`/instruments/${id}/status`, { active });

export default api;
