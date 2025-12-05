import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import InstrumentList from './components/Catalog/InstrumentList';
import MyBookings from './components/Booking/MyBookings';
import BookingHistory from './components/Booking/BookingHistory';
import PendingRequests from './components/Booking/PendingRequests';
import { getBookingsForRenter } from './services/api';
import './App.css';

function App() {
    const [pendingCount, setPendingCount] = useState(0);
    const RENTER_ID = 2;

    useEffect(() => {
        fetchPendingCount();
        const interval = setInterval(fetchPendingCount, 30000);
        return () => clearInterval(interval);
    }, []);

    const fetchPendingCount = async () => {
        try {
            const response = await getBookingsForRenter(RENTER_ID);
            const pending = response.data.filter(
                b => ['PENDING_PAYMENT', 'CONFIRMED'].includes(b.status)
            );
            setPendingCount(pending.length);
        } catch (err) {
            console.error('Error fetching pending count:', err);
        }
    };

    return (
        <BrowserRouter>
            <div className="app">
                <nav className="navbar">
                    <Link to="/" className="logo">🎸 SoundShop</Link>
                    <div className="nav-links">
                        <Link to="/instruments">Browse</Link>
                        <Link to="/pending" className="nav-link-with-badge">
                            Pending
                            {pendingCount > 0 && (
                                <span className="nav-badge">{pendingCount}</span>
                            )}
                        </Link>
                        <Link to="/bookings">My Bookings</Link>
                        <Link to="/history">History</Link>
                    </div>
                </nav>

                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<InstrumentList />} />
                        <Route path="/instruments" element={<InstrumentList />} />
                        <Route path="/pending" element={<PendingRequests />} />
                        <Route path="/bookings" element={<MyBookings />} />
                        <Route path="/history" element={<BookingHistory />} />
                    </Routes>
                </main>

                <footer className="footer">
                    <p>SoundShop © 2024 - TQS Project</p>
                </footer>
            </div>
        </BrowserRouter>
    );
}

export default App;