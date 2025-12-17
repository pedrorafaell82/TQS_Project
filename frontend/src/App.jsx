import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';

import InstrumentList from './components/Catalog/InstrumentList';
import FavoritesList from './components/Catalog/FavoritesList';
import UserManagement from './components/Admin/UserManagement';

import './App.css';

function App() {
    return (
        <BrowserRouter>
            <div className="app">
                <nav className="navbar">
                    <Link to="/" className="logo">🎸 SoundShop</Link>

                    <div className="nav-links">
                        <Link to="/instruments">Browse</Link>
                        <Link to="/favorites">⭐ Favorites</Link>

                        {/* Admin */}
                        <Link to="/admin/users">👤 Users</Link>
                    </div>
                </nav>

                <main>
                    <Routes>
                        {/* Renter */}
                        <Route path="/" element={<InstrumentList />} />
                        <Route path="/instruments" element={<InstrumentList />} />
                        <Route path="/favorites" element={<FavoritesList />} />

                        {/* Admin */}
                        <Route path="/admin/users" element={<UserManagement />} />
                    </Routes>
                </main>
            </div>
        </BrowserRouter>
    );
}

export default App;
