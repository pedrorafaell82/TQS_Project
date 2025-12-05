/**
 * InstrumentList Component
 * User Story: SOUN-19 - Browse music equipment
 * User Story: SOUN-18 - Book equipment (added booking button)
 */

import React, { useState, useEffect } from 'react';
import { getInstruments } from '../../services/api';
import BookingForm from '../Booking/BookingForm';
import './InstrumentList.css';

function InstrumentList() {
    const [instruments, setInstruments] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedInstrument, setSelectedInstrument] = useState(null);
    
    const [searchKeyword, setSearchKeyword] = useState('');
    const [city, setCity] = useState('');
    const [maxPrice, setMaxPrice] = useState('');
    const [category, setCategory] = useState('ALL');

    useEffect(() => {
        fetchInstruments();
    }, []);

    useEffect(() => {
        applyFilters();
    }, [searchKeyword, city, maxPrice, category, instruments]);

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

        if (city) {
            result = result.filter(i => 
                i.city && i.city.toLowerCase().includes(city.toLowerCase())
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

    if (loading) return <div className="loading">⏳ Loading instruments...</div>;
    if (error) return <div className="error">❌ {error}</div>;

    return (
        <div className="instrument-list">
            <div className="list-header">
                <h2>🎸 Browse Musical Instruments</h2>
                <p className="subtitle">Find the perfect instrument for your needs</p>
            </div>

            <div className="filters-section">
                <h3>🔍 Search & Filters</h3>
                <div className="filters-grid">
                    <div className="filter-group">
                        <label>Search by keyword:</label>
                        <input
                            type="text"
                            placeholder="e.g., guitar, drums..."
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                        />
                    </div>

                    <div className="filter-group">
                        <label>City:</label>
                        <input
                            type="text"
                            placeholder="e.g., Aveiro, Lisboa..."
                            value={city}
                            onChange={(e) => setCity(e.target.value)}
                        />
                    </div>

                    <div className="filter-group">
                        <label>Max daily price (€):</label>
                        <input
                            type="number"
                            placeholder="e.g., 35"
                            value={maxPrice}
                            onChange={(e) => setMaxPrice(e.target.value)}
                        />
                    </div>

                    <div className="filter-group">
                        <label>Category:</label>
                        <select value={category} onChange={(e) => setCategory(e.target.value)}>
                            <option value="ALL">All Categories</option>
                            <option value="GUITAR">Guitar</option>
                            <option value="DRUMS">Drums</option>
                            <option value="KEYBOARD">Keyboard</option>
                            <option value="BASS">Bass</option>
                            <option value="WIND">Wind</option>
                        </select>
                    </div>
                </div>

                <button onClick={() => {
                    setSearchKeyword('');
                    setCity('');
                    setMaxPrice('');
                    setCategory('ALL');
                }} className="btn-clear">
                    Clear All Filters
                </button>
            </div>

            <p className="results-info">
                Showing <strong>{filtered.length}</strong> of <strong>{instruments.length}</strong> instruments
            </p>

            {filtered.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">🔍</div>
                    <h3>No instruments found</h3>
                    <p>Try adjusting your filters</p>
                </div>
            ) : (
                <div className="instruments-grid">
                    {filtered.map(inst => (
                        <div key={inst.id} className="instrument-card">
                            <div className="card-content">
                                <div className="card-header-row">
                                    <h3>{inst.name}</h3>
                                    {inst.conditionGrade && (
                                        <span className="condition-badge">{inst.conditionGrade}</span>
                                    )}
                                </div>

                                {inst.brand && <p className="brand">🏷️ {inst.brand}</p>}
                                <p className="category">📂 {inst.category}</p>
                                <p className="description">{inst.description}</p>
                                
                                <div className="card-footer">
                                    <p className="price">€{inst.dailyPrice}<span>/day</span></p>
                                    <button 
                                        className="btn-book"
                                        onClick={() => setSelectedInstrument(inst)}
                                    >
                                        📅 Book Now
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {selectedInstrument && (
                <BookingForm
                    instrument={selectedInstrument}
                    onClose={() => setSelectedInstrument(null)}
                    onSuccess={() => {
                        console.log('Booking successful!');
                        fetchInstruments();
                    }}
                />
            )}
        </div>
    );
}

export default InstrumentList;