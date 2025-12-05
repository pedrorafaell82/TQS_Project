import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import InstrumentList from './components/Catalog/InstrumentList';
import './App.css';

function App() {
    return (
        <BrowserRouter>
            <div className="app">
                <nav className="navbar">
                    <Link to="/" className="logo">🎸 SoundShop</Link>
                    <div className="nav-links">
                        <Link to="/instruments">Browse</Link>
                        <Link to="/favorites">Favorites</Link>
                        <Link to="/bookings">My Bookings</Link>
                    </div>
                </nav>
                
                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<InstrumentList />} />
                        <Route path="/instruments" element={<InstrumentList />} />
                        <Route path="/favorites" element={<div><h2>Favorites (TODO)</h2></div>} />
                        <Route path="/bookings" element={<div><h2>My Bookings (TODO)</h2></div>} />
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
