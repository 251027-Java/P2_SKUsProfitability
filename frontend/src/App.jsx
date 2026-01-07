import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SKUDetailPage from './pages/SKUDetailPage';
import { isAuthenticated } from './services/AuthService';

function ProtectedRoute({ children }) {
    const [authChecked, setAuthChecked] = useState(false);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        setAuthenticated(isAuthenticated());
        setAuthChecked(true);
    }, []);

    if (!authChecked) {
        return <div className="text-white text-center mt-20">Loading...</div>;
    }

    return authenticated ? children : <Navigate to="/login" replace />;
}

function App() {
    return (
        <Router>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 transition-colors duration-200">
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<LoginPage />} />
                    <Route path="/" element={<Navigate to="/login" replace />} />

                    <Route 
                        path="/dashboard" 
                        element={
                            <ProtectedRoute>
                                <Navbar />
                                <HomePage />
                            </ProtectedRoute>
                        } 
                    />
                    
                    <Route 
                        path="/sku/:skuId" 
                        element={
                            <ProtectedRoute>
                                <Navbar />
                                <SKUDetailPage />
                            </ProtectedRoute>
                        } 
                    />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;