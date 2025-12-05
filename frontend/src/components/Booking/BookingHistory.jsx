/**
 * BookingHistory Component
 * User Story: SOUN-29 - View Booking History
 * 
 * Displays completed bookings for the renter with filtering options
 * 
 * Features:
 * - Lists all completed bookings
 * - Ordered by end date (most recent first)
 * - Filter by date range (30 days, 3 months, all time)
 * - "Book Again" functionality
 * - Download receipt placeholder
 * 
 * Story Points: 3
 */

import React, { useState, useEffect } from 'react';
import { getBookingsForRenter, getInstrumentById } from '../../services/api';
import './BookingHistory.css';

function BookingHistory() {
    const [bookings, setBookings] = useState([]);
    const [filteredBookings, setFilteredBookings] = useState([]);
    const [instruments, setInstruments] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [dateFilter, setDateFilter] = useState('all');
    const [currentPage, setCurrentPage] = useState(1);
    
    const RENTER_ID = 2;
    const ITEMS_PER_PAGE = 20;

    useEffect(() => {
        fetchBookingHistory();
    }, []);

    useEffect(() => {
        applyDateFilter();
    }, [dateFilter, bookings]);

    /**
     * Fetch all bookings and corresponding instrument details
     */
    const fetchBookingHistory = async () => {
        try {
            setLoading(true);
            const response = await getBookingsForRenter(RENTER_ID);
            const allBookings = response.data;
            
            // Filter only COMPLETED bookings
            const completedBookings = allBookings.filter(
                booking => booking.status === 'COMPLETED'
            );
            
            // Sort by end date (most recent first)
            completedBookings.sort((a, b) => 
                new Date(b.endDate) - new Date(a.endDate)
            );
            
            setBookings(completedBookings);
            
            // Fetch instrument details for each booking
            await fetchInstrumentDetails(completedBookings);
            
            setError(null);
        } catch (err) {
            console.error('Error fetching booking history:', err);
            setError('Failed to load booking history');
        } finally {
            setLoading(false);
        }
    };

    /**
     * Fetch instrument details for display
     */
    const fetchInstrumentDetails = async (bookingsList) => {
        const instrumentMap = {};
        const uniqueInstrumentIds = [...new Set(bookingsList.map(b => b.instrumentId))];
        
        try {
            await Promise.all(
                uniqueInstrumentIds.map(async (id) => {
                    const response = await getInstrumentById(id);
                    instrumentMap[id] = response.data;
                })
            );
            setInstruments(instrumentMap);
        } catch (err) {
            console.error('Error fetching instruments:', err);
        }
    };

    /**
     * Apply date range filter
     */
    const applyDateFilter = () => {
        const now = new Date();
        let filtered = [...bookings];

        if (dateFilter === '30days') {
            const thirtyDaysAgo = new Date(now.setDate(now.getDate() - 30));
            filtered = bookings.filter(
                booking => new Date(booking.endDate) >= thirtyDaysAgo
            );
        } else if (dateFilter === '3months') {
            const threeMonthsAgo = new Date(now.setMonth(now.getMonth() - 3));
            filtered = bookings.filter(
                booking => new Date(booking.endDate) >= threeMonthsAgo
            );
        }

        setFilteredBookings(filtered);
        setCurrentPage(1);
    };

    /**
     * Handle "Book Again" - opens instrument page or booking form
     */
    const handleBookAgain = (instrumentId) => {
        // Navigate to instrument or open booking form
        window.location.href = `/instruments?bookAgain=${instrumentId}`;
    };

    /**
     * Download receipt placeholder
     */
    const handleDownloadReceipt = (bookingId) => {
        alert(`Download receipt for booking #${bookingId} (feature coming soon)`);
    };

    /**
     * Pagination logic
     */
    const indexOfLastItem = currentPage * ITEMS_PER_PAGE;
    const indexOfFirstItem = indexOfLastItem - ITEMS_PER_PAGE;
    const currentBookings = filteredBookings.slice(indexOfFirstItem, indexOfLastItem);
    const totalPages = Math.ceil(filteredBookings.length / ITEMS_PER_PAGE);

    if (loading) return <div className="loading">⏳ Loading booking history...</div>;
    if (error) return <div className="error">❌ {error}</div>;

    return (
        <div className="booking-history">
            <div className="history-header">
                <h2>📜 Booking History</h2>
                <p className="subtitle">Review your past rentals</p>
            </div>

            {/* Date Range Filters */}
            <div className="filters-section">
                <h3>📅 Filter by Date Range</h3>
                <div className="filter-buttons">
                    <button 
                        className={`filter-btn ${dateFilter === 'all' ? 'active' : ''}`}
                        onClick={() => setDateFilter('all')}
                    >
                        All Time
                    </button>
                    <button 
                        className={`filter-btn ${dateFilter === '3months' ? 'active' : ''}`}
                        onClick={() => setDateFilter('3months')}
                    >
                        Last 3 Months
                    </button>
                    <button 
                        className={`filter-btn ${dateFilter === '30days' ? 'active' : ''}`}
                        onClick={() => setDateFilter('30days')}
                    >
                        Last 30 Days
                    </button>
                </div>
            </div>

            {/* Results Info */}
            <div className="results-info">
                <p>
                    Showing <strong>{currentBookings.length}</strong> of <strong>{filteredBookings.length}</strong> completed bookings
                </p>
            </div>

            {/* Empty State */}
            {filteredBookings.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">📭</div>
                    <h3>No completed bookings</h3>
                    <p>Your booking history will appear here once you complete rentals.</p>
                    <a href="/instruments" className="btn-primary">Browse Instruments</a>
                </div>
            ) : (
                <>
                    {/* Bookings List */}
                    <div className="history-list">
                        {currentBookings.map(booking => {
                            const instrument = instruments[booking.instrumentId];
                            
                            return (
                                <div key={booking.id} className="history-card">
                                    {/* Instrument Image Placeholder */}
                                    <div className="history-image">
                                        {instrument ? (
                                            <div className="instrument-icon">
                                                {instrument.category === 'GUITAR' && '🎸'}
                                                {instrument.category === 'DRUMS' && '🥁'}
                                                {instrument.category === 'KEYBOARD' && '🎹'}
                                                {instrument.category === 'BASS' && '🎸'}
                                                {!['GUITAR','DRUMS','KEYBOARD','BASS'].includes(instrument.category) && '🎵'}
                                            </div>
                                        ) : (
                                            <div className="instrument-icon">🎵</div>
                                        )}
                                    </div>

                                    {/* Booking Info */}
                                    <div className="history-info">
                                        <h3>{instrument?.name || `Instrument #${booking.instrumentId}`}</h3>
                                        {instrument && (
                                            <p className="instrument-details">
                                                {instrument.category} · {instrument.brand || 'N/A'}
                                            </p>
                                        )}
                                        
                                        <div className="booking-dates">
                                            <span className="date-label">📅 Rental Period:</span>
                                            <span className="date-value">
                                                {booking.startDate} → {booking.endDate}
                                            </span>
                                        </div>

                                        <div className="booking-price">
                                            <span className="price-label">💰 Total Paid:</span>
                                            <span className="price-value">€{booking.totalPrice}</span>
                                        </div>
                                    </div>

                                    {/* Actions */}
                                    <div className="history-actions">
                                        <button 
                                            className="btn-book-again"
                                            onClick={() => handleBookAgain(booking.instrumentId)}
                                        >
                                            🔄 Book Again
                                        </button>
                                        <button 
                                            className="btn-receipt"
                                            onClick={() => handleDownloadReceipt(booking.id)}
                                        >
                                            📄 Receipt
                                        </button>
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="pagination">
                            <button 
                                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                                disabled={currentPage === 1}
                                className="pagination-btn"
                            >
                                ← Previous
                            </button>
                            
                            <span className="pagination-info">
                                Page {currentPage} of {totalPages}
                            </span>
                            
                            <button 
                                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                                disabled={currentPage === totalPages}
                                className="pagination-btn"
                            >
                                Next →
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

export default BookingHistory;