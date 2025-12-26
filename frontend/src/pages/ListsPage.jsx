import { useState, useEffect } from 'react';
import { getAllLists, createList, deleteList, removeSKUFromList, addSKUToList, getListById } from '../services/ListService';
import { getAllSKUs } from '../services/SKUService';

function ListsPage() {
    const [lists, setLists] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [selectedList, setSelectedList] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [formData, setFormData] = useState({ name: '', description: '' });
    const [skus, setSkus] = useState([]);
    const [showAddSKUForm, setShowAddSKUForm] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        loadLists();
        loadSKUs();
    }, []);

    const loadSKUs = async () => {
        try {
            const data = await getAllSKUs();
            setSkus(data);
        } catch (err) {
        }
    };

    const loadLists = async () => {
        try {
            setLoading(true);
            const data = await getAllLists();
            setLists(data);
            setError('');
        } catch (err) {
            setError('Failed to load lists. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateList = async (e) => {
        e.preventDefault();
        if (!formData.name.trim()) {
            setError('List name is required');
            return;
        }

        try {
            setError('');
            setSuccess('');
            await createList(formData);
            setSuccess('List created successfully!');
            setShowCreateForm(false);
            setFormData({ name: '', description: '' });
            await loadLists();
        } catch (err) {
            setError(err.message || 'Failed to create list');
        }
    };

    const handleDeleteList = async (listId) => {
        if (!window.confirm('Are you sure you want to delete this list?')) {
            return;
        }
        try {
            setError('');
            await deleteList(listId);
            setSuccess('List deleted successfully!');
            if (selectedList?.listId === listId) {
                setSelectedList(null);
            }
            await loadLists();
        } catch (err) {
            setError(err.message || 'Failed to delete list');
        }
    };

    const handleAddSKU = async (listId, skuId) => {
        try {
            setError('');
            await addSKUToList(listId, skuId);
            setSuccess('SKU added to list successfully!');
            const updatedList = await getListById(listId);
            setSelectedList(updatedList);
            await loadLists();
        } catch (err) {
            setError(err.message || 'Failed to add SKU to list');
        }
    };

    const handleRemoveSKU = async (listId, skuId) => {
        try {
            setError('');
            await removeSKUFromList(listId, skuId);
            setSuccess('SKU removed from list');
            const updatedLists = await getAllLists();
            setLists(updatedLists);
            if (selectedList?.listId === listId) {
                const updatedList = await getListById(listId);
                if (updatedList) {
                    setSelectedList(updatedList);
                } else {
                    setSelectedList(null);
                }
            }
        } catch (err) {
            setError(err.message || 'Failed to remove SKU');
        }
    };

    const getAvailableSKUs = () => {
        if (!selectedList || !selectedList.items) {
            return skus;
        }
        const skuIdsInList = new Set(selectedList.items.map(item => item.skuId));
        return skus.filter(sku => !skuIdsInList.has(sku.skuId));
    };

    const getFilteredSKUs = () => {
        const available = getAvailableSKUs();
        if (!searchTerm.trim()) {
            return available;
        }
        const term = searchTerm.toLowerCase();
        return available.filter(sku => 
            sku.sku?.toLowerCase().includes(term) ||
            sku.productName?.toLowerCase().includes(term)
        );
    };

    return (
        <div>
            <div className="mb-10">
                <div className="flex justify-between items-start mb-6">
                    <div>
                        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">My Lists</h1>
                        <p className="text-gray-600 dark:text-gray-300 text-lg">Organize your favorite SKUs into lists</p>
                    </div>
                    <button
                        onClick={() => setShowCreateForm(!showCreateForm)}
                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                        </svg>
                        {showCreateForm ? 'Cancel' : 'Create List'}
                    </button>
                </div>

                {error && (
                    <div className="mb-6 bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 dark:border-red-600 text-red-800 dark:text-red-300 px-5 py-4 rounded-r-lg shadow-sm">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                            <span className="font-medium">{error}</span>
                        </div>
                    </div>
                )}
                {success && (
                    <div className="mb-6 bg-green-50 dark:bg-green-900/20 border-l-4 border-green-500 dark:border-green-600 text-green-800 dark:text-green-300 px-5 py-4 rounded-r-lg shadow-sm">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                            </svg>
                            <span className="font-medium">{success}</span>
                        </div>
                    </div>
                )}

                {showCreateForm && (
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-8 mb-8 transition-colors duration-200">
                        <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">Create New List</h2>
                    <form onSubmit={handleCreateList} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                                List Name <span className="text-red-500 dark:text-red-400">*</span>
                            </label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                required
                                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                                placeholder="My Favorite Products"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                                Description
                            </label>
                            <textarea
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                rows="3"
                                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                                placeholder="Optional description for this list"
                            />
                        </div>
                        <div className="flex gap-4">
                            <button
                                type="submit"
                                className="bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200"
                            >
                                Create List
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    setShowCreateForm(false);
                                    setFormData({ name: '', description: '' });
                                }}
                                className="px-6 py-3 border border-gray-300 dark:border-gray-600 rounded-lg font-semibold text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200 bg-white dark:bg-gray-800"
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                    </div>
                )}
            </div>

            {loading ? (
                <div className="p-16 text-center">
                    <div className="inline-block animate-spin rounded-full h-10 w-10 border-2 border-indigo-600 dark:border-indigo-400 border-t-transparent"></div>
                    <p className="mt-4 text-gray-600 dark:text-gray-300 font-medium">Loading lists...</p>
                </div>
            ) : lists.length === 0 ? (
                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-16 text-center transition-colors duration-200">
                    <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-100 dark:bg-gray-700 rounded-full mb-4">
                        <svg className="w-10 h-10 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">No lists yet</h3>
                    <p className="text-gray-600 dark:text-gray-300 mb-6">Create your first list to organize your SKUs</p>
                    <button
                        onClick={() => setShowCreateForm(true)}
                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                        </svg>
                        Create Your First List
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div className="space-y-4">
                        <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">Your Lists</h2>
                        {lists.map((list) => (
                            <div
                                key={list.listId}
                                className={`bg-white dark:bg-gray-800 rounded-xl shadow-sm border p-6 cursor-pointer transition-all duration-200 ${
                                    selectedList?.listId === list.listId
                                        ? 'border-indigo-500 dark:border-indigo-400 ring-2 ring-indigo-500 dark:ring-indigo-400'
                                        : 'border-gray-200 dark:border-gray-700 hover:shadow-md'
                                }`}
                                onClick={() => setSelectedList(list)}
                            >
                                <div className="flex justify-between items-start mb-3">
                                    <div className="flex-1">
                                        <h3 className="text-lg font-bold text-gray-900 dark:text-white">{list.name}</h3>
                                        {list.description && (
                                            <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">{list.description}</p>
                                        )}
                                    </div>
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleDeleteList(list.listId);
                                        }}
                                        className="text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300 ml-4 transition-colors duration-150"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                                <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400">
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                    </svg>
                                    <span>{list.itemCount || 0} SKU(s)</span>
                                </div>
                            </div>
                        ))}
                    </div>

                    {selectedList && (
                        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200">
                            <div className="mb-6 pb-4 border-b border-gray-200 dark:border-gray-700">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">{selectedList.name}</h2>
                                        {selectedList.description && (
                                            <p className="text-gray-600 dark:text-gray-300 mt-2">{selectedList.description}</p>
                                        )}
                                    </div>
                                    <button
                                        onClick={() => setShowAddSKUForm(!showAddSKUForm)}
                                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-4 py-2 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg text-sm"
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                        </svg>
                                        {showAddSKUForm ? 'Cancel' : 'Add SKUs'}
                                    </button>
                                </div>
                            </div>

                            {showAddSKUForm && (
                                <div className="mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg border border-gray-200 dark:border-gray-600 transition-colors duration-200">
                                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">Add SKUs to List</h3>
                                    <div className="mb-4">
                                        <input
                                            type="text"
                                            placeholder="Search SKUs by name or SKU..."
                                            value={searchTerm}
                                            onChange={(e) => setSearchTerm(e.target.value)}
                                            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
                                        />
                                    </div>
                                    <div className="max-h-64 overflow-y-auto space-y-2">
                                        {getFilteredSKUs().length === 0 ? (
                                            <p className="text-sm text-gray-500 dark:text-gray-400 text-center py-4">
                                                {searchTerm ? 'No SKUs found matching your search' : 'All SKUs are already in this list'}
                                            </p>
                                        ) : (
                                            getFilteredSKUs().map((sku) => (
                                                <div
                                                    key={sku.skuId}
                                                    className="flex items-center justify-between p-3 bg-white dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600 hover:border-indigo-300 dark:hover:border-indigo-500 transition-colors"
                                                >
                                                    <div className="flex-1">
                                                        <p className="font-semibold text-gray-900 dark:text-white">{sku.sku}</p>
                                                        <p className="text-sm text-gray-600 dark:text-gray-300">{sku.productName || 'No name'}</p>
                                                        {sku.sellingPrice && (
                                                            <p className="text-xs text-gray-500 dark:text-gray-400">${parseFloat(sku.sellingPrice).toFixed(2)}</p>
                                                        )}
                                                    </div>
                                                    <button
                                                        onClick={() => handleAddSKU(selectedList.listId, sku.skuId)}
                                                        className="ml-4 px-3 py-1.5 bg-indigo-600 dark:bg-indigo-500 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 dark:hover:bg-indigo-600 transition-colors"
                                                    >
                                                        Add
                                                    </button>
                                                </div>
                                            ))
                                        )}
                                    </div>
                                </div>
                            )}

                            {selectedList.items && selectedList.items.length > 0 ? (
                                <div className="overflow-x-auto">
                                    <table className="w-full">
                                        <thead className="bg-gray-50 dark:bg-gray-700">
                                            <tr>
                                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">SKU</th>
                                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Product</th>
                                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Price</th>
                                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-gray-100 dark:divide-gray-700 bg-white dark:bg-gray-800">
                                            {selectedList.items.map((sku) => (
                                                <tr key={sku.skuId} className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-150">
                                                    <td className="px-4 py-3 text-sm font-semibold text-gray-900 dark:text-white">{sku.sku}</td>
                                                    <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">{sku.productName || '-'}</td>
                                                    <td className="px-4 py-3 text-sm font-medium text-gray-900 dark:text-white">${parseFloat(sku.sellingPrice || 0).toFixed(2)}</td>
                                                    <td className="px-4 py-3 text-sm">
                                                        <button
                                                            onClick={() => handleRemoveSKU(selectedList.listId, sku.skuId)}
                                                            className="text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300 font-medium transition-colors duration-150"
                                                        >
                                                            Remove
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            ) : (
                                <div className="text-center py-12">
                                    <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full mb-4">
                                        <svg className="w-8 h-8 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                                        </svg>
                                    </div>
                                    <p className="text-gray-600 dark:text-gray-300 font-medium">This list is empty</p>
                                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Add SKUs from the My SKUs section</p>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default ListsPage;

