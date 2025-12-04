/**
 * Favorites Service
 * Manages user's favorite instruments using localStorage
 * (Temporary solution until backend implements favorites API)
 * 
 * User Story: SOUN-21 - Mark equipment as favorite
 */

const FAVORITES_KEY = 'soundshop_favorites';

/**
 * Get all favorites from localStorage
 * @returns {number[]} Array of instrument IDs
 */
export const getFavorites = () => {
    try {
        const favorites = localStorage.getItem(FAVORITES_KEY);
        return favorites ? JSON.parse(favorites) : [];
    } catch (error) {
        console.error('Error reading favorites:', error);
        return [];
    }
};

/**
 * Add instrument to favorites
 * Prevents duplicates automatically
 * @param {number} instrumentId 
 * @returns {number[]} Updated favorites list
 */
export const addToFavorites = (instrumentId) => {
    const favorites = getFavorites();
    
    // Check for duplicates
    if (!favorites.includes(instrumentId)) {
        favorites.push(instrumentId);
        localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
    }
    
    return favorites;
};

/**
 * Remove instrument from favorites
 * @param {number} instrumentId 
 * @returns {number[]} Updated favorites list
 */
export const removeFromFavorites = (instrumentId) => {
    const favorites = getFavorites();
    const updated = favorites.filter(id => id !== instrumentId);
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(updated));
    return updated;
};

/**
 * Check if instrument is in favorites
 * @param {number} instrumentId 
 * @returns {boolean}
 */
export const isFavorite = (instrumentId) => {
    return getFavorites().includes(instrumentId);
};

/**
 * Clear all favorites
 * @returns {void}
 */
export const clearAllFavorites = () => {
    localStorage.removeItem(FAVORITES_KEY);
};
