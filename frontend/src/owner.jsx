import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import InstrumentList from './components/Catalog/InstrumentList';
import MyBookings from './components/Booking/MyBookings';
import './App.css';

function OwnerPage() {
    return (
        <div>
            <div className="app">
                <nav className="navbar">
                    <Link to="/" className="logo">🎸 SoundShop Owner</Link>
                    <div className="nav-links">
                        <Link to="/bookings">My Bookings</Link>
                    </div>
                </nav>

                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<InstrumentList />} />
                        <Route path="/bookings" element={<MyBookings />} />
                    </Routes>
                </main>

                <footer className="footer">
                    <p>SoundShop © 2024 - TQS Project</p>
                </footer>
            </div>
        </div>
    );
}

export default OwnerPage;