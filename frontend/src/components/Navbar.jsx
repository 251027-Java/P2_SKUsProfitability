import { logout } from '../services/AuthService';
import { useNavigate } from 'react-router-dom';

function Navbar() {
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleDashboardClick = () => {
        navigate('/', { replace: true });
        window.dispatchEvent(new CustomEvent('dashboard-navigate'));
    };

    return (
        <nav className="bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-xl">
            <div className="container mx-auto px-6 py-4">
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2 ml-16">
                        <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                        </svg>
                        <h1 className="text-2xl font-bold">Amazon Profitability Calculator</h1>
                    </div>
                    <div className="flex items-center gap-6">
                        <button
                            onClick={handleDashboardClick}
                            className="hover:text-indigo-200 transition-colors duration-200 px-4 py-2 rounded-lg hover:bg-white/10 font-medium"
                        >
                            Dashboard
                        </button>
                        <button
                            onClick={handleLogout}
                            className="hover:text-indigo-200 transition-colors duration-200 px-4 py-2 rounded-lg hover:bg-white/10 font-medium"
                        >
                            Logout
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
}

export default Navbar;
