/**
 * FavoritesList Component
 * Displays user's favorite instruments
 * 
 * User Story: SOUN-21 - Mark equipment as favorite
 * 
 * Acceptance Criteria:
 * - Shows only favorite instruments
 * - Can remove from favorites
 * - Shows empty state when no favorites
 */
import React, { useState, useEffect } from 'react';
import { getInstruments } from '../../services/api';
import { getFavorites, removeFromFavorites } from '../../services/favoritesService';
import './FavoritesList.css';

function FavoritesList() {
    const [favoriteInstruments, setFavoriteInstruments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchFavorites();
    }, []);

    const fetchFavorites = async () => {
        try {
            setLoading(true);
            
            // Get favorite IDs
            const favoriteIds = getFavorites();
            
            if (favoriteIds.length === 0) {
                setFavoriteInstruments([]);
                setLoading(false);
                return;
            }
            
            // Fetch all instruments
            const response = await getInstruments();
            const allInstruments = response.data;
            
            // Filter only favorites
            const favorites = allInstruments.filter(inst => 
                favoriteIds.includes(inst.id)
            );
            
            setFavoriteInstruments(favorites);
            setError(null);
        } catch (err) {
            console.error('Error:', err);
            setError('Cannot load favorites');
        } finally {
            setLoading(false);
        }
    };

    const removeFavorite = (instrumentId) => {
        removeFromFavorites(instrumentId);
        fetchFavorites(); // Reload list
    };

    if (loading) return <div className="loading">Loading favorites...</div>;
    if (error) return <div className="error">{error}</div>;

    return (
        <div className="favorites-list">
            <h2>⭐ My Favorites</h2>
            
            {favoriteInstruments.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">💔</div>
                    <h3>No favorites yet</h3>
                    <p>Start adding instruments to your favorites!</p>
                    <a href="/instruments" className="btn-primary">Browse Instruments</a>
                </div>
            ) : (
                <div>
                    <p>You have {favoriteInstruments.length} favorite(s)</p>
                    
                    <div className="grid">
                        {favoriteInstruments.map(inst => (
                            <div key={inst.id} className="card">
                                <div className="card-header">
                                    <h3>{inst.name}</h3>
                                    <button 
                                        className="btn-remove"
                                        onClick={() => removeFavorite(inst.id)}
                                        title="Remove from favorites"
                                    >
                                        ❌
                                    </button>
                                </div>
                                <p>{inst.description}</p>
                                <p><strong>€{inst.dailyPrice}/day</strong></p>
                                <p>Category: {inst.category}</p>
                                {inst.brand && <p>Brand: {inst.brand}</p>}
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

export default FavoritesList;
