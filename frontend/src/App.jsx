import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import InstrumentList from './components/Catalog/InstrumentList';
import MyBookings from './components/Booking/MyBookings';
import BookingHistory from './components/Booking/BookingHistory';
import PendingRequests from './components/Booking/PendingRequests';
import { getBookingsForRenter } from './services/api';
import './App.css';
import LoginPage from './components/Auth/Login';
import RegisterPage from './components/Auth/Register';
import Navbar from './components/Navbar';
import Layout from './components/Layout';

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
            <Routes>
                {/* Routes WITHOUT navbar */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* Routes WITH navbar */}
                <Route element={<Layout />}>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/instruments" element={<InstrumentList />} />
                <Route path="/my-bookings" element={<MyBookings />} />
                <Route path="/booking-history" element={<BookingHistory />} />
                <Route path="/pending-requests" element={<PendingRequests />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;