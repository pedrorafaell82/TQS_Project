/**
 * InstrumentList Component
 * User Story: SOUN-19 - Browse music equipment
 * User Story: SOUN-21 - Mark equipment as favorite
 */
import React, { useState, useEffect } from 'react';
import { getInstruments } from '../../services/api';
import { addToFavorites, removeFromFavorites, isFavorite } from '../../services/favoritesService';
import './InstrumentList.css';

function InstrumentList() {
    const [instruments, setInstruments] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [favorites, setFavorites] = useState([]);
    
    const [searchKeyword, setSearchKeyword] = useState('');
    const [maxPrice, setMaxPrice] = useState('');
    const [category, setCategory] = useState('ALL');

    useEffect(() => {
        fetchInstruments();
    }, []);

    useEffect(() => {
        applyFilters();
    }, [searchKeyword, maxPrice, category, instruments]);

    const fetchInstruments = async () => {
        try {
            setLoading(true);
            const response = await getInstruments();
            setInstruments(response.data);
            setFiltered(response.data);
            setError(null);
        } catch (err) {
            console.error('Error:', err);
            setError('Cannot connect to backend');
        } finally {
            setLoading(false);
        }
    };

    const applyFilters = () => {
        let result = [...instruments];

        if (searchKeyword) {
            result = result.filter(i => 
                i.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                i.description.toLowerCase().includes(searchKeyword.toLowerCase())
            );
        }

        if (maxPrice) {
            result = result.filter(i => Number(i.dailyPrice) <= Number(maxPrice));
        }

        if (category !== 'ALL') {
            result = result.filter(i => i.category === category);
        }

        setFiltered(result);
    };

    /**
     * Toggle favorite status for instrument
     * Prevents duplicates automatically
     */
    const toggleFavorite = (instrumentId) => {
        if (isFavorite(instrumentId)) {
            removeFromFavorites(instrumentId);
        } else {
            addToFavorites(instrumentId);
        }
        // Force re-render
        setFavorites([...favorites, Math.random()]);
    };

    if (loading) return <div className="loading">Loading...</div>;
    if (error) return <div className="error">{error}</div>;

    return (
        <div className="instrument-list">
            <h2>🎸 Browse Instruments</h2>

            <div className="filters">
                <input
                    type="text"
                    placeholder="Search..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                />
                <input
                    type="number"
                    placeholder="Max price €"
                    value={maxPrice}
                    onChange={(e) => setMaxPrice(e.target.value)}
                />
                <select value={category} onChange={(e) => setCategory(e.target.value)}>
                    <option value="ALL">All</option>
                    <option value="GUITAR">Guitar</option>
                    <option value="DRUMS">Drums</option>
                    <option value="KEYBOARD">Keyboard</option>
                    <option value="BASS">Bass</option>
                </select>
            </div>

            <p>Showing {filtered.length} of {instruments.length}</p>

            <div className="grid">
                {filtered.map(inst => (
                    <div key={inst.id} className="card">
                        <div className="card-header">
                            <h3>{inst.name}</h3>
                            <button 
                                className={`btn-favorite ${isFavorite(inst.id) ? 'active' : ''}`}
                                onClick={() => toggleFavorite(inst.id)}
                                title={isFavorite(inst.id) ? 'Remove from favorites' : 'Add to favorites'}
                            >
                                {isFavorite(inst.id) ? '⭐' : '☆'}
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
    );
}

export default InstrumentList;
