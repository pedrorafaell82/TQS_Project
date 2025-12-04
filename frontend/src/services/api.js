/**
 * API Service - Backend communication
 * Base URL: http://localhost:8080/api
 */
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: { 'Content-Type': 'application/json' }
});

// INSTRUMENTS
export const getInstruments = () => api.get('/instruments');
export const getInstrumentById = (id) => api.get(`/instruments/${id}`);

export default api;
