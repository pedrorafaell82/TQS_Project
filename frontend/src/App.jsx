import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import InstrumentList from './components/Catalog/InstrumentList';
import FavoritesList from './components/Catalog/FavoritesList';
import PlatformOverviewDashboard from './components/Admin/PlatformOverviewDashboard';
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
                        <Link to="/admin/overview">📊 Dashboard</Link>
                    </div>
                </nav>
                
                <main>
                    <Routes>
                        <Route path="/" element={<InstrumentList />} />
                        <Route path="/instruments" element={<InstrumentList />} />
                        <Route path="/favorites" element={<FavoritesList />} />
                        <Route path="/admin/overview" element={<PlatformOverviewDashboard />} />
                    </Routes>
                </main>
            </div>
        </BrowserRouter>
    );
}

export default App;
