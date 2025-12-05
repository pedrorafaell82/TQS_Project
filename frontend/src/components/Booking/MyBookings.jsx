import React, { useState, useEffect } from 'react';
import { getBookingsForRenter } from '../../services/api';
import './MyBookings.css';

function MyBookings() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const RENTER_ID = 2;

    useEffect(() => {
        fetchBookings();
    }, []);

    const fetchBookings = async () => {
        try {
            setLoading(true);
            const response = await getBookingsForRenter(RENTER_ID);
            setBookings(response.data);
            setError(null);
        } catch (err) {
            setError('Cannot load bookings');
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status) => {
        const colors = {
            PENDING_PAYMENT: 'orange',
            CONFIRMED: 'green',
            COMPLETED: 'blue',
            CANCELLED: 'red'
        };
        return colors[status] || 'gray';
    };

    if (loading) return <div className="loading">Loading...</div>;
    if (error) return <div className="error">{error}</div>;

    return (
        <div className="my-bookings">
            <h2>📅 My Bookings</h2>
            {bookings.length === 0 ? (
                <div className="empty-state">
                    <h3>No bookings yet</h3>
                    <p>Start browsing instruments to make your first booking!</p>
                </div>
            ) : (
                <div className="bookings-list">
                    {bookings.map(booking => (
                        <div key={booking.id} className="booking-card">
                            <div className="booking-header">
                                <h3>Booking #{booking.id}</h3>
                                <span className={`status-badge ${getStatusColor(booking.status)}`}>
                                    {booking.status}
                                </span>
                            </div>
                            <p><strong>Instrument ID:</strong> {booking.instrumentId}</p>
                            <p><strong>Dates:</strong> {booking.startDate} to {booking.endDate}</p>
                            <p><strong>Total:</strong> €{booking.totalPrice}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default MyBookings;
