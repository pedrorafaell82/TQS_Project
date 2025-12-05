/**
 * BookingForm Component
 * User Story: SOUN-18 - Book equipment for specific dates
 * 
 * Features:
 * - Date selection (start and end)
 * - Validation (end > start, future dates)
 * - Total price calculation
 * - Conflict detection (409 error)
 * - Creates booking with status PENDING_PAYMENT
 * 
 * Scenarios:
 * - Successful booking of available equipment
 * - Prevent double booking (shows error)
 */

import React, { useState } from 'react';
import { createBooking } from '../../services/api';
import './BookingForm.css';

function BookingForm({ instrument, onClose, onSuccess }) {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    // Hardcoded renter ID (temporary until auth is implemented)
    const RENTER_ID = 2;

    /**
     * Handle form submission
     * Creates booking request via API
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // Client-side validation
        if (new Date(startDate) >= new Date(endDate)) {
            setError('End date must be after start date');
            return;
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (new Date(startDate) < today) {
            setError('Start date cannot be in the past');
            return;
        }

        try {
            setLoading(true);
            setError(null);

            // Call API
            const response = await createBooking(instrument.id, {
                renterId: RENTER_ID,
                startDate,
                endDate
            });

            console.log('Booking created:', response.data);
            setSuccess(true);
            
            // Close after 2 seconds
            setTimeout(() => {
                onSuccess && onSuccess();
                onClose && onClose();
            }, 2000);

        } catch (err) {
            console.error('Booking error:', err);
            
            // Handle conflict (409) - double booking
            if (err.response?.status === 409) {
                setError('Equipment not available for the selected dates');
            } else if (err.response?.status === 404) {
                setError('Instrument or user not found');
            } else {
                setError('Failed to create booking. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    /**
     * Calculate total price based on dates
     * @returns {string} Total price formatted
     */
    const calculateTotal = () => {
        if (!startDate || !endDate) return '0.00';
        
        const start = new Date(startDate);
        const end = new Date(endDate);
        const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        
        if (days <= 0) return '0.00';
        
        const total = days * Number(instrument.dailyPrice);
        return total.toFixed(2);
    };

    /**
     * Get number of days
     */
    const getDays = () => {
        if (!startDate || !endDate) return 0;
        const start = new Date(startDate);
        const end = new Date(endDate);
        const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        return days > 0 ? days : 0;
    };

    return (
        <div className="booking-form-overlay" onClick={onClose}>
            <div className="booking-form-modal" onClick={(e) => e.stopPropagation()}>
                <button className="close-btn" onClick={onClose} type="button">
                    ✕
                </button>
                
                <h2>📅 Book: {instrument.name}</h2>
                <p className="instrument-price">€{instrument.dailyPrice}/day</p>

                {success ? (
                    <div className="success-message">
                        <div className="success-icon">✓</div>
                        <h3>Booking Request Created!</h3>
                        <p>Status: <strong>PENDING_PAYMENT</strong></p>
                        <p>The owner will be notified.</p>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="startDate">Start Date:</label>
                            <input
                                id="startDate"
                                type="date"
                                value={startDate}
                                onChange={(e) => setStartDate(e.target.value)}
                                min={new Date().toISOString().split('T')[0]}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="endDate">End Date:</label>
                            <input
                                id="endDate"
                                type="date"
                                value={endDate}
                                onChange={(e) => setEndDate(e.target.value)}
                                min={startDate || new Date().toISOString().split('T')[0]}
                                required
                            />
                        </div>

                        {startDate && endDate && getDays() > 0 && (
                            <div className="booking-summary">
                                <div className="summary-row">
                                    <span>Duration:</span>
                                    <strong>{getDays()} day{getDays() !== 1 ? 's' : ''}</strong>
                                </div>
                                <div className="summary-row">
                                    <span>Daily rate:</span>
                                    <strong>€{instrument.dailyPrice}</strong>
                                </div>
                                <div className="summary-row total">
                                    <span>Total:</span>
                                    <strong>€{calculateTotal()}</strong>
                                </div>
                            </div>
                        )}

                        {error && (
                            <div className="error-message">
                                ⚠️ {error}
                            </div>
                        )}

                        <button 
                            type="submit" 
                            className="btn-submit" 
                            disabled={loading || !startDate || !endDate}
                        >
                            {loading ? 'Creating...' : 'Request Booking'}
                        </button>

                        <p className="info-text">
                            Your booking will have status <strong>PENDING_PAYMENT</strong> until confirmed.
                        </p>
                    </form>
                )}
            </div>
        </div>
    );
}

export default BookingForm;