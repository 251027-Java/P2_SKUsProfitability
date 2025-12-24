import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { getAllSKUs, createSKU, deleteSKU, importSKUsFromCSV } from '../services/SKUService';
import { getAllLists, addSKUToList } from '../services/ListService';
import SKUForm from '../components/SKUForm';
import Calculator from '../components/Calculator';
import Sidebar from '../components/Sidebar';
import ListsPage from './ListsPage';

function HomePage() {
    const location = useLocation();
    const [skus, setSkus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [activeSection, setActiveSection] = useState('dashboard');

    useEffect(() => {
        if (location.pathname === '/' || location.pathname === '/api') {
            setActiveSection('dashboard');
        }
    }, [location.pathname]);

    useEffect(() => {
        const handleDashboardNavigate = () => {
            setActiveSection('dashboard');
        };
        window.addEventListener('dashboard-navigate', handleDashboardNavigate);
        return () => window.removeEventListener('dashboard-navigate', handleDashboardNavigate);
    }, []);

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [lists, setLists] = useState([]);
    const [showAddToListMenu, setShowAddToListMenu] = useState(null);

    useEffect(() => {
        loadSKUs();
        loadLists();
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (showAddToListMenu && !event.target.closest('.relative')) {
                setShowAddToListMenu(null);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showAddToListMenu]);

    const loadLists = async () => {
        try {
            const data = await getAllLists();
            setLists(data);
        } catch (err) {
            console.error('Failed to load lists:', err);
        }
    };

    const handleAddToList = async (listId, skuId) => {
        try {
            setError('');
            await addSKUToList(listId, skuId);
            setSuccess('SKU added to list successfully!');
            setShowAddToListMenu(null);
            await loadLists();
        } catch (err) {
            setError(err.message || 'Failed to add SKU to list');
        }
    };

    const loadSKUs = async () => {
        try {
            setLoading(true);
            const data = await getAllSKUs();
            setSkus(data);
            setError('');
        } catch (err) {
            setError('Failed to load SKUs. Please try again.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateSKU = async (skuData) => {
        try {
            setError('');
            setSuccess('');
            await createSKU(skuData);
            setSuccess('SKU created successfully!');
            setShowForm(false);
            await loadSKUs();
        } catch (err) {
            setError(err.message || 'Failed to create SKU');
        }
    };

    const handleDeleteSKU = async (skuId) => {
        if (!window.confirm('Are you sure you want to delete this SKU?')) {
            return;
        }
        try {
            setError('');
            await deleteSKU(skuId);
            setSuccess('SKU deleted successfully!');
            await loadSKUs();
        } catch (err) {
            setError(err.message || 'Failed to delete SKU');
        }
    };

    const handleCSVImport = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        try {
            setError('');
            setSuccess('');
            const result = await importSKUsFromCSV(file);
            setSuccess(`Successfully imported ${result.count} SKU(s)!`);
            await loadSKUs();
        } catch (err) {
            setError(err.message || 'Failed to import SKUs');
        } finally {
            e.target.value = '';
        }
    };

    const totalSKUs = skus.length;
    const totalRevenue = skus.reduce((sum, sku) => sum + (parseFloat(sku.sellingPrice) || 0), 0);
    const totalProfit = skus.reduce((sum, sku) => sum + (parseFloat(sku.netProfit) || 0), 0);
    const avgMargin = skus.length > 0 
        ? skus.reduce((sum, sku) => sum + (parseFloat(sku.profitMargin) || 0), 0) / skus.length 
        : 0;
    const skusWithROI = skus.filter(sku => sku.roi != null);
    const avgROI = skusWithROI.length > 0
        ? skusWithROI.reduce((sum, sku) => sum + (parseFloat(sku.roi) || 0), 0) / skusWithROI.length
        : 0;

    const renderContent = () => {
        switch (activeSection) {
            case 'calculator':
                return (
                    <div>
                        <div className="mb-8">
                            <h1 className="text-4xl font-bold text-gray-900 mb-2">Calculator</h1>
                            <p className="text-gray-600 text-lg">Calculate fees and ROI for your products</p>
                        </div>
                        <Calculator />
                    </div>
                );
            case 'lists':
                return <ListsPage />;
            case 'skus':
                return (
                    <>
                        <div className="mb-8">
                            <div className="flex justify-between items-start mb-6">
                                <div>
                                    <h1 className="text-4xl font-bold text-gray-900 mb-2">My SKUs</h1>
                                    <p className="text-gray-600 text-lg">Manage your product inventory</p>
                                </div>
                                <div className="flex gap-3">
                                    <label className="inline-flex items-center gap-2 bg-white text-gray-700 px-5 py-2.5 rounded-lg font-medium hover:bg-gray-50 transition-colors duration-200 shadow-sm border border-gray-200 cursor-pointer">
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                        </svg>
                                        <input
                                            type="file"
                                            accept=".csv"
                                            onChange={handleCSVImport}
                                            className="hidden"
                                        />
                                        Import CSV
                                    </label>
                                    <button
                                        onClick={() => setShowForm(!showForm)}
                                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                        </svg>
                                        {showForm ? 'Cancel' : 'Add SKU'}
                                    </button>
                                </div>
                            </div>

                            {showForm && (
                                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8 mb-8">
                                    <h2 className="text-2xl font-bold text-gray-900 mb-6">Add New SKU</h2>
                                    <SKUForm
                                        onSave={handleCreateSKU}
                                        onCancel={() => setShowForm(false)}
                                    />
                                </div>
                            )}
                        </div>

                        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                            <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                                <h2 className="text-xl font-semibold text-gray-900">Your SKUs</h2>
                            </div>
                            
                            {loading ? (
                                <div className="p-16 text-center">
                                    <div className="inline-block animate-spin rounded-full h-10 w-10 border-2 border-indigo-600 border-t-transparent"></div>
                                    <p className="mt-4 text-gray-600 font-medium">Loading SKUs...</p>
                                </div>
                            ) : skus.length === 0 ? (
                                <div className="p-16 text-center">
                                    <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-100 rounded-full mb-4">
                                        <svg className="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                        </svg>
                                    </div>
                                    <h3 className="text-lg font-semibold text-gray-900 mb-2">No SKUs yet</h3>
                                    <p className="text-gray-600 mb-6">Get started by adding your first product</p>
                                    <button
                                        onClick={() => setShowForm(true)}
                                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                        </svg>
                                        Add Your First SKU
                                    </button>
                                </div>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full">
                                        <thead className="bg-gray-50">
                                            <tr>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">SKU</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Product</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Price</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Cost</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Fees</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Profit</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Margin</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">ROI</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Max Cost</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody className="bg-white divide-y divide-gray-100">
                                            {skus.map((sku) => (
                                                <tr key={sku.skuId} className="hover:bg-gray-50 transition-colors duration-150">
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="text-sm font-semibold text-gray-900">{sku.sku}</div>
                                                        {sku.asin && (
                                                            <div className="text-xs text-gray-500 mt-0.5">ASIN: {sku.asin}</div>
                                                        )}
                                                    </td>
                                                    <td className="px-6 py-4">
                                                        <div className="text-sm font-medium text-gray-900">{sku.productName || '-'}</div>
                                                        <div className="text-xs text-gray-500 mt-0.5">{sku.category || 'No category'}</div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <span className="text-sm font-medium text-gray-900">${parseFloat(sku.sellingPrice || 0).toFixed(2)}</span>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <span className="text-sm font-medium text-gray-900">${parseFloat(sku.cost || 0).toFixed(2)}</span>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <span className="text-sm text-gray-600">${parseFloat(sku.totalFees || 0).toFixed(2)}</span>
                                                    </td>
                                                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-semibold ${
                                                        parseFloat(sku.netProfit || 0) >= 0 ? 'text-green-600' : 'text-red-600'
                                                    }`}>
                                                        ${parseFloat(sku.netProfit || 0).toFixed(2)}
                                                    </td>
                                                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-semibold ${
                                                        parseFloat(sku.profitMargin || 0) >= 0 ? 'text-green-600' : 'text-red-600'
                                                    }`}>
                                                        {parseFloat(sku.profitMargin || 0).toFixed(1)}%
                                                    </td>
                                                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-semibold ${
                                                        sku.roi ? (parseFloat(sku.roi) >= 0 ? 'text-green-600' : 'text-red-600') : 'text-gray-400'
                                                    }`}>
                                                        {sku.roi ? `${parseFloat(sku.roi).toFixed(1)}%` : '-'}
                                                    </td>
                                                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-semibold ${
                                                        sku.maxCost ? 'text-blue-600' : 'text-gray-400'
                                                    }`}>
                                                        {sku.maxCost ? `$${parseFloat(sku.maxCost).toFixed(2)}` : '-'}
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="flex items-center gap-3">
                                                            <div className="relative">
                                                                <button
                                                                    onClick={() => setShowAddToListMenu(showAddToListMenu === sku.skuId ? null : sku.skuId)}
                                                                    className="text-sm text-indigo-600 hover:text-indigo-800 font-medium transition-colors duration-150"
                                                                >
                                                                    Add to List
                                                                </button>
                                                                {showAddToListMenu === sku.skuId && (
                                                                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-10 overflow-hidden">
                                                                        <div className="py-1">
                                                                            {lists.length === 0 ? (
                                                                                <div className="px-4 py-3 text-sm text-gray-500">
                                                                                    No lists yet. Create one in My Lists.
                                                                                </div>
                                                                            ) : (
                                                                                lists.map((list) => (
                                                                                    <button
                                                                                        key={list.listId}
                                                                                        onClick={() => handleAddToList(list.listId, sku.skuId)}
                                                                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors duration-150"
                                                                                    >
                                                                                        {list.name}
                                                                                    </button>
                                                                                ))
                                                                            )}
                                                                        </div>
                                                                    </div>
                                                                )}
                                                            </div>
                                                            <button
                                                                onClick={() => handleDeleteSKU(sku.skuId)}
                                                                className="text-sm text-red-600 hover:text-red-800 font-medium transition-colors duration-150"
                                                            >
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </>
                );
            case 'dashboard':
            default:
                const recentSKUs = [...skus].sort((a, b) => (b.skuId || 0) - (a.skuId || 0)).slice(0, 5);
                const topPerformers = [...skus]
                    .filter(sku => sku.roi != null && parseFloat(sku.roi) > 0)
                    .sort((a, b) => parseFloat(b.roi || 0) - parseFloat(a.roi || 0))
                    .slice(0, 5);
                
                return (
                    <>
                        <div className="mb-10">
                            <div className="flex justify-between items-start mb-6">
                                <div>
                                    <h1 className="text-4xl font-bold text-gray-900 mb-2">
                                        Dashboard
                                    </h1>
                                    <p className="text-gray-600 text-lg">
                                        Quick overview and recent activity
                                    </p>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-8">
                                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="p-3 bg-indigo-100 rounded-lg">
                                            <svg className="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 mb-1">Total SKUs</p>
                                    <p className="text-3xl font-bold text-gray-900">{totalSKUs}</p>
                                </div>
                                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="p-3 bg-green-100 rounded-lg">
                                            <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 mb-1">Total Profit</p>
                                    <p className={`text-3xl font-bold ${totalProfit >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                                        ${totalProfit.toFixed(2)}
                                    </p>
                                </div>
                                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="p-3 bg-purple-100 rounded-lg">
                                            <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 mb-1">Avg. Margin</p>
                                    <p className="text-3xl font-bold text-gray-900">{avgMargin.toFixed(1)}%</p>
                                </div>
                                {skusWithROI.length > 0 && (
                                    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
                                        <div className="flex items-center justify-between mb-4">
                                            <div className="p-3 bg-orange-100 rounded-lg">
                                                <svg className="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                                                </svg>
                                            </div>
                                        </div>
                                        <p className="text-sm font-medium text-gray-600 mb-1">Avg. ROI</p>
                                        <p className="text-3xl font-bold text-gray-900">{avgROI.toFixed(1)}%</p>
                                    </div>
                                )}
                            </div>

                            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                                    <div className="flex justify-between items-center mb-6">
                                        <h2 className="text-xl font-bold text-gray-900">Recent SKUs</h2>
                                        <button
                                            onClick={() => setActiveSection('skus')}
                                            className="text-sm text-indigo-600 hover:text-indigo-800 font-medium"
                                        >
                                            View All →
                                        </button>
                                    </div>
                                    {recentSKUs.length === 0 ? (
                                        <div className="text-center py-8">
                                            <p className="text-gray-500">No SKUs yet</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            {recentSKUs.map((sku) => (
                                                <div key={sku.skuId} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors duration-150">
                                                    <div className="flex-1">
                                                        <p className="font-semibold text-gray-900">{sku.sku}</p>
                                                        <p className="text-sm text-gray-600">{sku.productName || 'No name'}</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="font-semibold text-gray-900">${parseFloat(sku.sellingPrice || 0).toFixed(2)}</p>
                                                        {sku.roi != null && (
                                                            <p className={`text-sm font-medium ${parseFloat(sku.roi) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                                                                {parseFloat(sku.roi).toFixed(1)}% ROI
                                                            </p>
                                                        )}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                                    <div className="flex justify-between items-center mb-6">
                                        <h2 className="text-xl font-bold text-gray-900">Top Performers</h2>
                                        <button
                                            onClick={() => setActiveSection('skus')}
                                            className="text-sm text-indigo-600 hover:text-indigo-800 font-medium"
                                        >
                                            View All →
                                        </button>
                                    </div>
                                    {topPerformers.length === 0 ? (
                                        <div className="text-center py-8">
                                            <p className="text-gray-500">No ROI data available</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            {topPerformers.map((sku) => (
                                                <div key={sku.skuId} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors duration-150">
                                                    <div className="flex-1">
                                                        <p className="font-semibold text-gray-900">{sku.sku}</p>
                                                        <p className="text-sm text-gray-600">{sku.productName || 'No name'}</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="font-semibold text-green-600">{parseFloat(sku.roi || 0).toFixed(1)}% ROI</p>
                                                        <p className="text-sm text-gray-600">${parseFloat(sku.netProfit || 0).toFixed(2)} profit</p>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                                <h2 className="text-xl font-bold text-gray-900 mb-6">Quick Actions</h2>
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <button
                                        onClick={() => setActiveSection('calculator')}
                                        className="flex items-center gap-4 p-4 bg-gradient-to-r from-indigo-50 to-blue-50 rounded-lg border border-indigo-200 hover:from-indigo-100 hover:to-blue-100 transition-all duration-200 text-left"
                                    >
                                        <div className="p-3 bg-indigo-600 rounded-lg">
                                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900">Calculator</p>
                                            <p className="text-sm text-gray-600">Calculate fees & ROI</p>
                                        </div>
                                    </button>
                                    <button
                                        onClick={() => setActiveSection('skus')}
                                        className="flex items-center gap-4 p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg border border-green-200 hover:from-green-100 hover:to-emerald-100 transition-all duration-200 text-left"
                                    >
                                        <div className="p-3 bg-green-600 rounded-lg">
                                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900">Add SKU</p>
                                            <p className="text-sm text-gray-600">Add new product</p>
                                        </div>
                                    </button>
                                    <label className="flex items-center gap-4 p-4 bg-gradient-to-r from-purple-50 to-pink-50 rounded-lg border border-purple-200 hover:from-purple-100 hover:to-pink-100 transition-all duration-200 text-left cursor-pointer">
                                        <div className="p-3 bg-purple-600 rounded-lg">
                                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900">Import CSV</p>
                                            <p className="text-sm text-gray-600">Bulk import products</p>
                                        </div>
                                        <input
                                            type="file"
                                            accept=".csv"
                                            onChange={handleCSVImport}
                                            className="hidden"
                                        />
                                    </label>
                                </div>
                            </div>
                        </div>
                    </>
                );
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <Sidebar activeSection={activeSection} onSectionChange={setActiveSection} />
            <div className="ml-64">
                <div className="max-w-7xl mx-auto px-8 py-10">
                    {error && (
                        <div className="mb-6 bg-red-50 border-l-4 border-red-500 text-red-800 px-5 py-4 rounded-r-lg shadow-sm">
                            <div className="flex items-center">
                                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                                </svg>
                                <span className="font-medium">{error}</span>
                            </div>
                        </div>
                    )}
                    {success && (
                        <div className="mb-6 bg-green-50 border-l-4 border-green-500 text-green-800 px-5 py-4 rounded-r-lg shadow-sm">
                            <div className="flex items-center">
                                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                </svg>
                                <span className="font-medium">{success}</span>
                            </div>
                        </div>
                    )}

                    {renderContent()}
                </div>
            </div>
        </div>
    );
}

export default HomePage;
