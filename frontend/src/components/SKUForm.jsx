import { useState } from 'react';
import { searchSKU } from '../services/SKUService';

function SKUForm({ onSave, onCancel, initialData = null }) {
    const [formData, setFormData] = useState({
        sku: initialData?.sku || '',
        productName: initialData?.productName || '',
        length: initialData?.length || '',
        width: initialData?.width || '',
        height: initialData?.height || '',
        weight: initialData?.weight || '',
        category: initialData?.category || '',
        sellingPrice: initialData?.sellingPrice || '',
        cost: initialData?.cost || '',
        targetROI: initialData?.targetROI || '',
    });

    const [errors, setErrors] = useState({});
    const [searchTerm, setSearchTerm] = useState('');
    const [searching, setSearching] = useState(false);
    const [searchError, setSearchError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    const handleSearch = async () => {
        if (!searchTerm.trim()) {
            setSearchError('Please enter a SKU to search');
            return;
        }

        setSearching(true);
        setSearchError('');

        try {
            const foundSKU = await searchSKU(searchTerm.trim());
            if (foundSKU) {
                setFormData({
                    sku: foundSKU.sku || '',
                    productName: foundSKU.productName || '',
                    length: foundSKU.length || '',
                    width: foundSKU.width || '',
                    height: foundSKU.height || '',
                    weight: foundSKU.weight || '',
                    category: foundSKU.category || '',
                    sellingPrice: foundSKU.sellingPrice || '',
                    cost: foundSKU.cost || '',
                    targetROI: foundSKU.targetROI || '',
                });
                setSearchTerm('');
                setSearchError('');
            } else {
                setSearchError('Product not found. Please check the SKU.');
            }
        } catch (err) {
            setSearchError('Failed to search. Please try again.');
        } finally {
            setSearching(false);
        }
    };

    const handleSearchKeyPress = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSearch();
        }
    };

    const validate = () => {
        const newErrors = {};
        
        if (!formData.sku.trim()) newErrors.sku = 'SKU is required';
        if (!formData.length || formData.length <= 0) newErrors.length = 'Length is required';
        if (!formData.width || formData.width <= 0) newErrors.width = 'Width is required';
        if (!formData.height || formData.height <= 0) newErrors.height = 'Height is required';
        if (!formData.weight || formData.weight <= 0) newErrors.weight = 'Weight is required';
        if (!formData.sellingPrice || formData.sellingPrice <= 0) newErrors.sellingPrice = 'Selling price is required';
        
        if (!formData.cost && !formData.targetROI) {
            newErrors.cost = 'Either Cost or Target ROI is required';
            newErrors.targetROI = 'Either Cost or Target ROI is required';
        }
        if (formData.cost && formData.cost < 0) newErrors.cost = 'Cost cannot be negative';
        if (formData.targetROI && formData.targetROI <= 0) newErrors.targetROI = 'Target ROI must be greater than 0';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;

        const submitData = {
            ...formData,
            length: parseFloat(formData.length),
            width: parseFloat(formData.width),
            height: parseFloat(formData.height),
            weight: parseFloat(formData.weight),
            sellingPrice: parseFloat(formData.sellingPrice),
            cost: formData.cost ? parseFloat(formData.cost) : null,
            targetROI: formData.targetROI ? parseFloat(formData.targetROI) : null,
        };

        onSave(submitData);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-5 border border-blue-200">
                <label className="block text-sm font-semibold text-gray-700 mb-3">
                    Search by SKU
                </label>
                <div className="flex gap-3">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyPress={handleSearchKeyPress}
                        placeholder="Enter SKU to auto-fill fields..."
                        className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                    />
                    <button
                        type="button"
                        onClick={handleSearch}
                        disabled={searching}
                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                    >
                        {searching ? (
                            <>
                                <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Searching...
                            </>
                        ) : (
                            <>
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                </svg>
                                Search
                            </>
                        )}
                    </button>
                </div>
                {searchError && (
                    <p className="text-red-600 text-sm mt-2 font-medium">{searchError}</p>
                )}
                <p className="text-xs text-gray-600 mt-2">
                    Search your saved products to auto-fill all fields
                </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        SKU <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        name="sku"
                        value={formData.sku}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.sku ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="ABC-123"
                    />
                    {errors.sku && <p className="text-red-600 text-sm mt-1 font-medium">{errors.sku}</p>}
                </div>

                <div className="md:col-span-2">
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Product Name
                    </label>
                    <input
                        type="text"
                        name="productName"
                        value={formData.productName}
                        onChange={handleChange}
                        className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                        placeholder="Widget Pro"
                    />
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Length (inches) <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="length"
                        value={formData.length}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.length ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="10.5"
                    />
                    {errors.length && <p className="text-red-600 text-sm mt-1 font-medium">{errors.length}</p>}
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Width (inches) <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="width"
                        value={formData.width}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.width ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="8.0"
                    />
                    {errors.width && <p className="text-red-600 text-sm mt-1 font-medium">{errors.width}</p>}
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Height (inches) <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="height"
                        value={formData.height}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.height ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="2.5"
                    />
                    {errors.height && <p className="text-red-600 text-sm mt-1 font-medium">{errors.height}</p>}
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Weight (pounds) <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="weight"
                        value={formData.weight}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.weight ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="1.2"
                    />
                    {errors.weight && <p className="text-red-600 text-sm mt-1 font-medium">{errors.weight}</p>}
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Category
                    </label>
                    <input
                        type="text"
                        name="category"
                        value={formData.category}
                        onChange={handleChange}
                        className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                        placeholder="Electronics"
                    />
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Selling Price ($) <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="sellingPrice"
                        value={formData.sellingPrice}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.sellingPrice ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="29.99"
                    />
                    {errors.sellingPrice && <p className="text-red-600 text-sm mt-1 font-medium">{errors.sellingPrice}</p>}
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Cost ($) <span className="text-gray-500 text-xs">(optional)</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="cost"
                        value={formData.cost}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.cost ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="15.00"
                    />
                    {errors.cost && <p className="text-red-600 text-sm mt-1 font-medium">{errors.cost}</p>}
                    <p className="text-xs text-gray-500 mt-1">If provided, calculates actual ROI</p>
                </div>

                <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Target ROI (%) <span className="text-gray-500 text-xs">(optional)</span>
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        name="targetROI"
                        value={formData.targetROI}
                        onChange={handleChange}
                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white ${
                            errors.targetROI ? 'border-red-500' : 'border-gray-300'
                        }`}
                        placeholder="30.00"
                    />
                    {errors.targetROI && <p className="text-red-600 text-sm mt-1 font-medium">{errors.targetROI}</p>}
                    <p className="text-xs text-gray-500 mt-1">If provided, calculates max purchase cost</p>
                </div>
            </div>

            <div className="bg-blue-50 border-l-4 border-blue-500 rounded-r-lg p-4">
                <p className="text-sm text-blue-800 font-medium">
                    Provide either Cost (to see actual ROI) or Target ROI (to see max purchase cost). 
                    You can provide both to see both calculations.
                </p>
            </div>

            <div className="flex gap-4 pt-2">
                <button
                    type="submit"
                    className="flex-1 inline-flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    {initialData ? 'Update SKU' : 'Create SKU'}
                </button>
                {onCancel && (
                    <button
                        type="button"
                        onClick={onCancel}
                        className="px-6 py-3 border border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                    >
                        Cancel
                    </button>
                )}
            </div>
        </form>
    );
}

export default SKUForm;

