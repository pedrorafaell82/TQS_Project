/**
 * PendingRequests Component
 * User Story: SOUN-30 - View Pending Requests
 * 
 * Displays pending booking requests awaiting owner approval
 * 
 * Features:
 * - Lists all pending requests
 * - Status badges (PENDING_PAYMENT, CONFIRMED, CANCELLED)
 * - Time since request (relative time)
 * - Cancel request functionality
 * - Auto-refresh every 30 seconds
 * - Badge counter for navigation
 * 
 * Story Points: 5
 */

import React, { useState, useEffect } from 'react';
import { getBookingsForRenter, getInstrumentById, updateBookingStatus } from '../../services/api';
import './PendingRequests.css';

function PendingRequests() {
    const [requests, setRequests] = useState([]);
    const [instruments, setInstruments] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [notification, setNotification] = useState(null);
    
    const RENTER_ID = 2;
    const AUTO_REFRESH_INTERVAL = 30000; // 30 seconds

    useEffect(() => {
        fetchPendingRequests();
        
        // Auto-refresh
        const interval = setInterval(() => {
            fetchPendingRequests(true);
        }, AUTO_REFRESH_INTERVAL);
        
        return () => clearInterval(interval);
    }, []);

    /**
     * Fetch pending booking requests
     * @param {boolean} silent - Don't show loading if true (for auto-refresh)
     */
    const fetchPendingRequests = async (silent = false) => {
        try {
            if (!silent) setLoading(true);
            
            const response = await getBookingsForRenter(RENTER_ID);
            const allBookings = response.data;
            
            // Filter pending requests (not COMPLETED or CANCELLED)
            const pendingRequests = allBookings.filter(
                booking => ['PENDING_PAYMENT', 'CONFIRMED'].includes(booking.status)
            );
            
            // Sort by creation date (most recent first - using id as proxy)
            pendingRequests.sort((a, b) => b.id - a.id);
            
            // Check for status changes
            if (silent && requests.length > 0) {
                checkForStatusChanges(requests, pendingRequests);
            }
            
            setRequests(pendingRequests);
            
            // Fetch instrument details
            await fetchInstrumentDetails(pendingRequests);
            
            setError(null);
        } catch (err) {
            console.error('Error fetching pending requests:', err);
            if (!silent) setError('Failed to load pending requests');
        } finally {
            if (!silent) setLoading(false);
        }
    };

    /**
     * Check for status changes and show notification
     */
    const checkForStatusChanges = (oldRequests, newRequests) => {
        oldRequests.forEach(oldReq => {
            const newReq = newRequests.find(r => r.id === oldReq.id);
            if (newReq && newReq.status !== oldReq.status) {
                showNotification(`Booking #${newReq.id} status changed to ${newReq.status}`);
            }
        });
    };

    /**
     * Fetch instrument details
     */
    const fetchInstrumentDetails = async (requestsList) => {
        const instrumentMap = {};
        const uniqueIds = [...new Set(requestsList.map(r => r.instrumentId))];
        
        try {
            await Promise.all(
                uniqueIds.map(async (id) => {
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
     * Cancel pending request
     */
    const handleCancelRequest = async (bookingId) => {
        if (!confirm('Are you sure you want to cancel this booking request?')) {
            return;
        }

        try {
            await updateBookingStatus(bookingId, 'CANCELLED');
            showNotification('Booking request cancelled successfully');
            fetchPendingRequests();
        } catch (err) {
            console.error('Error cancelling request:', err);
            alert('Failed to cancel request. Please try again.');
        }
    };

    /**
     * Show notification
     */
    const showNotification = (message) => {
        setNotification(message);
        setTimeout(() => setNotification(null), 5000);
    };

    /**
     * Calculate time since request
     */
    const getTimeSince = (bookingId) => {
        // Using booking ID as proxy for creation time
        // In real scenario, backend should provide createdAt timestamp
        const now = Date.now();
        const estimatedTime = now - (bookingId * 60000); // Mock calculation
        const hours = Math.floor((now - estimatedTime) / (1000 * 60 * 60));
        const days = Math.floor(hours / 24);
        
        if (days > 0) return `${days} day${days > 1 ? 's' : ''} ago`;
        if (hours > 0) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
        return 'Just now';
    };

    /**
     * Get status color
     */
    const getStatusColor = (status) => {
        const colors = {
            PENDING_PAYMENT: 'orange',
            CONFIRMED: 'green',
            CANCELLED: 'red'
        };
        return colors[status] || 'gray';
    };

    /**
     * Get status display text
     */
    const getStatusText = (status) => {
        const texts = {
            PENDING_PAYMENT: 'Pending Approval',
            CONFIRMED: 'Confirmed',
            CANCELLED: 'Cancelled'
        };
        return texts[status] || status;
    };

    if (loading) return <div className="loading">⏳ Loading pending requests...</div>;
    if (error) return <div className="error">❌ {error}</div>;

    return (
        <div className="pending-requests">
            {/* Notification */}
            {notification && (
                <div className="notification">
                    ✅ {notification}
                </div>
            )}

            <div className="requests-header">
                <h2>⏳ Pending Requests</h2>
                <p className="subtitle">Track your booking requests awaiting approval</p>
                {requests.length > 0 && (
                    <div className="badge-counter">
                        <span className="counter-badge">{requests.length}</span>
                        <span>pending request{requests.length !== 1 ? 's' : ''}</span>
                    </div>
                )}
            </div>

            {/* Auto-refresh indicator */}
            <div className="auto-refresh-notice">
                🔄 Auto-refreshing every 30 seconds
            </div>

            {/* Empty State */}
            {requests.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">✅</div>
                    <h3>No pending requests</h3>
                    <p>All your booking requests have been processed.</p>
                    <a href="/instruments" className="btn-primary">Browse Instruments</a>
                </div>
            ) : (
                /* Requests List */
                <div className="requests-list">
                    {requests.map(request => {
                        const instrument = instruments[request.instrumentId];
                        
                        return (
                            <div key={request.id} className="request-card">
                                {/* Header with Status */}
                                <div className="request-header">
                                    <div>
                                        <h3>Booking Request #{request.id}</h3>
                                        <p className="time-since">
                                            🕒 {getTimeSince(request.id)}
                                        </p>
                                    </div>
                                    <span className={`status-badge ${getStatusColor(request.status)}`}>
                                        {getStatusText(request.status)}
                                    </span>
                                </div>

                                {/* Instrument Info */}
                                <div className="request-content">
                                    <div className="instrument-preview">
                                        <div className="instrument-icon">
                                            {instrument?.category === 'GUITAR' && '🎸'}
                                            {instrument?.category === 'DRUMS' && '🥁'}
                                            {instrument?.category === 'KEYBOARD' && '🎹'}
                                            {instrument?.category === 'BASS' && '🎸'}
                                            {!['GUITAR','DRUMS','KEYBOARD','BASS'].includes(instrument?.category) && '🎵'}
                                        </div>
                                        <div className="instrument-info">
                                            <h4>{instrument?.name || `Instrument #${request.instrumentId}`}</h4>
                                            {instrument && (
                                                <p className="instrument-details">
                                                    {instrument.category} · {instrument.brand || 'N/A'}
                                                </p>
                                            )}
                                        </div>
                                    </div>

                                    <div className="request-details">
                                        <div className="detail-row">
                                            <span className="detail-label">📅 Dates:</span>
                                            <span className="detail-value">
                                                {request.startDate} → {request.endDate}
                                            </span>
                                        </div>
                                        <div className="detail-row">
                                            <span className="detail-label">💰 Total:</span>
                                            <span className="detail-value price">
                                                €{request.totalPrice}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {/* Actions */}
                                {request.status === 'PENDING_PAYMENT' && (
                                    <div className="request-actions">
                                        <button 
                                            className="btn-cancel"
                                            onClick={() => handleCancelRequest(request.id)}
                                        >
                                            ❌ Cancel Request
                                        </button>
                                        <p className="action-note">
                                            Waiting for owner approval...
                                        </p>
                                    </div>
                                )}

                                {request.status === 'CONFIRMED' && (
                                    <div className="request-actions">
                                        <div className="success-notice">
                                            ✅ Your request has been confirmed! Proceed to payment.
                                        </div>
                                        <button className="btn-pay">
                                            💳 Proceed to Payment
                                        </button>
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default PendingRequests;