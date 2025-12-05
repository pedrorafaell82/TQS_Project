/**
 * InstrumentList Component
 * Displays searchable and filterable list of musical instruments
 * 
 * Features:
 * - Search by keyword
 * - Filter by price range
 * - Filter by city
 * - View instrument details
 * 
 * User Story: SOUN-19 - Browse music equipment
 * Scenarios covered:
 * - Renter filters equipment by keyword
 * - Renter filters equipment by price range
 * - Renter views equipment details
 * 
 * @component
 */

import React, { useState, useEffect } from 'react';
import { getInstruments, searchInstruments, filterByPrice } from '../../services/api';
import './InstrumentList.css';

function InstrumentList() {
    // State management
    const [instruments, setInstruments] = useState([]);
    const [filteredInstruments, setFilteredInstruments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Filter states
    const [searchKeyword, setSearchKeyword] = useState('');
    const [city, setCity] = useState('');
    const [maxPrice, setMaxPrice] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('ALL');

    /**
     * Fetch instruments on component mount
     */
    useEffect(() => {
        fetchInstruments();
    }, []);

    /**
     * Apply filters whenever filter values change
     */
    useEffect(() => {
        applyFilters();
    }, [searchKeyword, city, maxPrice, selectedCategory, instruments]);

    /**
     * Fetches all instruments from API
     * Falls back to mock data if backend unavailable
     */
    const fetchInstruments = async () => {
        try {
            setLoading(true);
            const response = await getInstruments();
            setInstruments(response.data);
            setFilteredInstruments(response.data);
            setError(null);
        } catch (err) {
            console.error('Error fetching instruments:', err);
            setError('Unable to connect to backend. Showing demo data.');
            
            // Mock data for testing without backend
            const mockData = [
                {
                    id: 1,
                    name: 'Fender Stratocaster',
                    category: 'GUITAR',
                    description: 'Classic electric guitar',
                    dailyRate: 25,
                    city: 'Aveiro',
                    status: 'AVAILABLE',
                    photoUrl: 'https://images.unsplash.com/photo-1564186763535-ebb21ef5277f?w=400',
                    owner: { fullName: 'João Silva' }
                },
                {
                    id: 2,
                    name: 'Yamaha P-125',
                    category: 'KEYBOARD',
                    description: 'Digital piano 88 keys',
                    dailyRate: 40,
                    city: 'Aveiro',
                    status: 'AVAILABLE',
                    photoUrl: 'https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=400',
                    owner: { fullName: 'Maria Santos' }
                },
                {
                    id: 3,
                    name: 'Pearl Export Drums',
                    category: 'DRUMS',
                    description: 'Complete drum kit',
                    dailyRate: 50,
                    city: 'Lisboa',
                    status: 'AVAILABLE',
                    photoUrl: 'https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?w=400',
                    owner: { fullName: 'Pedro Costa' }
                },
                {
                    id: 4,
                    name: 'Gibson Les Paul',
                    category: 'GUITAR',
                    description: 'Premium electric guitar',
                    dailyRate: 30,
                    city: 'Porto',
                    status: 'AVAILABLE',
                    photoUrl: 'https://images.unsplash.com/photo-1516924962500-2b4b3b99ea02?w=400',
                    owner: { fullName: 'Ana Silva' }
                },
                {
                    id: 5,
                    name: 'Yamaha Saxophone',
                    category: 'WIND',
                    description: 'Alto saxophone',
                    dailyRate: 35,
                    city: 'Aveiro',
                    status: 'AVAILABLE',
                    photoUrl: 'https://images.unsplash.com/photo-1551600265-5493d0b55be1?w=400',
                    owner: { fullName: 'Carlos Mendes' }
                }
            ];
            
            setInstruments(mockData);
            setFilteredInstruments(mockData);
        } finally {
            setLoading(false);
        }
    };

    /**
     * Apply all active filters to instrument list
     */
    const applyFilters = () => {
        let filtered = [...instruments];

        // Filter by keyword (name or description)
        if (searchKeyword) {
            filtered = filtered.filter(inst => 
                inst.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                inst.description.toLowerCase().includes(searchKeyword.toLowerCase()) ||
                inst.category.toLowerCase().includes(searchKeyword.toLowerCase())
            );
        }

        // Filter by city
        if (city) {
            filtered = filtered.filter(inst => 
                inst.city.toLowerCase().includes(city.toLowerCase())
            );
        }

        // Filter by max price
        if (maxPrice) {
            filtered = filtered.filter(inst => 
                inst.dailyRate <= parseFloat(maxPrice)
            );
        }

        // Filter by category
        if (selectedCategory !== 'ALL') {
            filtered = filtered.filter(inst => 
                inst.category === selectedCategory
            );
        }

        setFilteredInstruments(filtered);
    };

    /**
     * Clear all filters
     */
    const clearFilters = () => {
        setSearchKeyword('');
        setCity('');
        setMaxPrice('');
        setSelectedCategory('ALL');
    };

    /**
     * Navigate to instrument details
     */
    const viewDetails = (instrumentId) => {
        // TODO: Navigate to details page
        console.log('View details for instrument:', instrumentId);
        alert(`View details for instrument ${instrumentId} (navigation to be implemented)`);
    };

    // Loading state
    if (loading) {
        return <div className="loading">🎵 Loading instruments...</div>;
    }

    return (
        <div className="instrument-list">
            {/* Header */}
            <div className="list-header">
                <h2>🎸 Browse Musical Instruments</h2>
                <p className="subtitle">Find the perfect instrument for your needs</p>
            </div>

            {/* Error banner */}
            {error && (
                <div className="error-banner">
                    ⚠️ {error}
                </div>
            )}

            {/* Filters Section */}
            <div className="filters-section">
                <h3>🔍 Search & Filters</h3>
                
                <div className="filters-grid">
                    {/* Keyword search */}
                    <div className="filter-group">
                        <label>Search by keyword:</label>
                        <input
                            type="text"
                            placeholder="e.g., guitar, drums..."
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            className="filter-input"
                        />
                    </div>

                    {/* City filter */}
                    <div className="filter-group">
                        <label>City:</label>
                        <input
                            type="text"
                            placeholder="e.g., Aveiro, Lisboa..."
                            value={city}
                            onChange={(e) => setCity(e.target.value)}
                            className="filter-input"
                        />
                    </div>

                    {/* Price filter */}
                    <div className="filter-group">
                        <label>Max daily price (€):</label>
                        <input
                            type="number"
                            placeholder="e.g., 35"
                            value={maxPrice}
                            onChange={(e) => setMaxPrice(e.target.value)}
                            className="filter-input"
                        />
                    </div>

                    {/* Category filter */}
                    <div className="filter-group">
                        <label>Category:</label>
                        <select
                            value={selectedCategory}
                            onChange={(e) => setSelectedCategory(e.target.value)}
                            className="filter-input"
                        >
                            <option value="ALL">All Categories</option>
                            <option value="GUITAR">Guitar</option>
                            <option value="DRUMS">Drums</option>
                            <option value="KEYBOARD">Keyboard/Piano</option>
                            <option value="BASS">Bass</option>
                            <option value="WIND">Wind Instruments</option>
                            <option value="PERCUSSION">Percussion</option>
                        </select>
                    </div>
                </div>

                <button onClick={clearFilters} className="btn-clear">
                    Clear All Filters
                </button>
            </div>

            {/* Results count */}
            <div className="results-info">
                <p>
                    Showing <strong>{filteredInstruments.length}</strong> of <strong>{instruments.length}</strong> instruments
                </p>
            </div>

            {/* Empty state */}
            {filteredInstruments.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">🔍</div>
                    <h3>No instruments found</h3>
                    <p>Try adjusting your filters</p>
                    <button onClick={clearFilters} className="btn-primary">
                        Clear Filters
                    </button>
                </div>
            ) : (
                // Instruments grid
                <div className="instruments-grid">
                    {filteredInstruments.map(instrument => (
                        <div key={instrument.id} className="instrument-card">
                            {/* Image */}
                            <div className="card-image-wrapper">
                                <img 
                                    src={instrument.photoUrl} 
                                    alt={instrument.name}
                                    className="card-image"
                                />
                                <span className="category-badge">
                                    {instrument.category}
                                </span>
                            </div>

                            {/* Content */}
                            <div className="card-content">
                                <h3>{instrument.name}</h3>
                                <p className="description">{instrument.description}</p>
                                
                                <div className="card-details">
                                    <p className="price">
                                        <strong>€{instrument.dailyRate}</strong>/day
                                    </p>
                                    <p className="location">
                                        📍 {instrument.city}
                                    </p>
                                    <p className="owner">
                                        👤 {instrument.owner.fullName}
                                    </p>
                                </div>

                                <div className="card-actions">
                                    <button 
                                        onClick={() => viewDetails(instrument.id)}
                                        className="btn-primary"
                                    >
                                        View Details
                                    </button>
                                    <button className="btn-favorite">
                                        ⭐ Add to Favorites
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default InstrumentList;
