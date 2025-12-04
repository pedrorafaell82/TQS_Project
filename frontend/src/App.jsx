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
                    <Link to="/instruments">Browse</Link>
                </nav>
                
                <main>
                    <Routes>
                        <Route path="/" element={<InstrumentList />} />
                        <Route path="/instruments" element={<InstrumentList />} />
                    </Routes>
                </main>
            </div>
        </BrowserRouter>
    );
}

export default App;
