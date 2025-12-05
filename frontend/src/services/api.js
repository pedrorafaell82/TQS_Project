/**
 * API Service - Centralized API calls
 * @module api
 */
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// ===== INSTRUMENTS ENDPOINTS =====

/**
 * Get all instruments
 * @returns {Promise} List of instruments
 */
export const getInstruments = () => api.get('/instruments');

/**
 * Search instruments by keyword
 * @param {string} keyword - Search term
 * @param {string} city - City filter (optional)
 * @returns {Promise} Filtered instruments
 */
export const searchInstruments = (keyword, city) => {
    const params = {};
    if (keyword) params.keyword = keyword;
    if (city) params.city = city;
    return api.get('/instruments/search', { params });
};

/**
 * Filter instruments by price
 * @param {number} maxPrice - Maximum daily price
 * @param {string} city - City filter (optional)
 * @returns {Promise} Filtered instruments
 */
export const filterByPrice = (maxPrice, city) => {
    const params = { maxPrice };
    if (city) params.city = city;
    return api.get('/instruments/filter', { params });
};

/**
 * Get instrument details by ID
 * @param {number} id - Instrument ID
 * @returns {Promise} Instrument details
 */
export const getInstrumentById = (id) => api.get(`/instruments/${id}`);

// ===== FAVORITES ENDPOINTS =====

/**
 * Get user's favorite instruments
 * @returns {Promise} List of favorites
 */
export const getFavorites = () => api.get('/favorites');

/**
 * Add instrument to favorites
 * @param {number} instrumentId - Instrument ID
 * @returns {Promise} Updated favorites
 */
export const addToFavorites = (instrumentId) => api.post('/favorites', { instrumentId });

/**
 * Remove instrument from favorites
 * @param {number} instrumentId - Instrument ID
 * @returns {Promise} Confirmation
 */
export const removeFromFavorites = (instrumentId) => api.delete(`/favorites/${instrumentId}`);

export default api;


// ===== BOOKINGS =====

/**
 * Create booking for instrument
 * @param {number} instrumentId - Instrument ID
 * @param {Object} data - {renterId, startDate, endDate}
 * @returns {Promise<BookingDto>}
 */
export const createBooking = (instrumentId, data) => 
    api.post(`/instruments/${instrumentId}/bookings`, data);

/**
 * Get booking by ID
 * @param {number} id - Booking ID
 * @returns {Promise<BookingDto>}
 */
export const getBookingById = (id) => api.get(`/bookings/${id}`);

/**
 * Get all bookings for renter
 * @param {number} renterId - Renter ID
 * @returns {Promise<BookingDto[]>}
 */
export const getBookingsForRenter = (renterId) => 
    api.get(`/users/${renterId}/bookings`);

/**
 * Update booking status
 * @param {number} id - Booking ID
 * @param {string} status - New status
 * @returns {Promise<BookingDto>}
 */
export const updateBookingStatus = (id, status) => 
    api.patch(`/bookings/${id}/status`, { status });