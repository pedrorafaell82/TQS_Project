import React, { useState } from 'react';
import { createBooking } from '../../services/api';
import './BookingForm.css';

function BookingForm({ instrument, onClose, onSuccess }) {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const RENTER_ID = 2;

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (new Date(startDate) >= new Date(endDate)) {
            setError('End date must be after start date');
            return;
        }
        try {
            setLoading(true);
            setError(null);
            await createBooking(instrument.id, { renterId: RENTER_ID, startDate, endDate });
            setSuccess(true);
            setTimeout(() => {
                onSuccess && onSuccess();
                onClose && onClose();
            }, 2000);
        } catch (err) {
            if (err.response?.status === 409) {
                setError('Equipment not available for the selected dates');
            } else {
                setError('Failed to create booking');
            }
        } finally {
            setLoading(false);
        }
    };

    const calculateTotal = () => {
        if (!startDate || !endDate) return '0.00';
        const days = Math.ceil((new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24));
        return days > 0 ? (days * Number(instrument.dailyPrice)).toFixed(2) : '0.00';
    };

    const getDays = () => {
        if (!startDate || !endDate) return 0;
        const days = Math.ceil((new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24));
        return days > 0 ? days : 0;
    };

    return (
        <div className="booking-form-overlay" onClick={onClose}>
            <div className="booking-form-modal" onClick={(e) => e.stopPropagation()}>
                <button className="close-btn" onClick={onClose}>✕</button>
                <h2>📅 Book: {instrument.name}</h2>
                <p className="instrument-price">€{instrument.dailyPrice}/day</p>
                {success ? (
                    <div className="success-message">
                        <div className="success-icon">✓</div>
                        <h3>Booking Request Created!</h3>
                        <p>Status: <strong>PENDING_PAYMENT</strong></p>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Start Date:</label>
                            <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} min={new Date().toISOString().split('T')[0]} required />
                        </div>
                        <div className="form-group">
                            <label>End Date:</label>
                            <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} min={startDate || new Date().toISOString().split('T')[0]} required />
                        </div>
                        {startDate && endDate && getDays() > 0 && (
                            <div className="booking-summary">
                                <div className="summary-row"><span>Duration:</span><strong>{getDays()} day(s)</strong></div>
                                <div className="summary-row"><span>Daily rate:</span><strong>€{instrument.dailyPrice}</strong></div>
                                <div className="summary-row total"><span>Total:</span><strong>€{calculateTotal()}</strong></div>
                            </div>
                        )}
                        {error && <div className="error-message">⚠️ {error}</div>}
                        <button type="submit" className="btn-submit" disabled={loading || !startDate || !endDate}>
                            {loading ? 'Creating...' : 'Request Booking'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}

export default BookingForm;