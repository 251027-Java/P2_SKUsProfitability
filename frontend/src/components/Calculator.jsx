import { useState } from 'react';
import { searchSKU } from '../services/ProductService';
import { calculateFees } from '../services/CalculatorService';

function Calculator() {
    const [formData, setFormData] = useState({
        productName: '',
        description: '',
        length: '',
        width: '',
        height: '',
        weight: '',
        category: '',
        sellingPrice: '',
        timeInStorage: '1',
        freightCost: '',
        freightCostUnit: '1',
        otherCosts: '0',
        otherCostsType: 'fixed',
        fbaFeeCategory: 'Most goods',
        referralFeePercentage: '15',
    });

    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [searching, setSearching] = useState(false);
    const [searchError, setSearchError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
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
                setFormData(prev => ({
                    ...prev,
                    productName: foundSKU.productName || '',
                    description: foundSKU.description || '',
                    length: foundSKU.length || '',
                    width: foundSKU.width || '',
                    height: foundSKU.height || '',
                    weight: foundSKU.weight || '',
                    category: foundSKU.category || '',
                    sellingPrice: foundSKU.sellingPrice || '',
                }));
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

    const handleCalculate = async (e) => {
        e.preventDefault();
        setError('');
        setResults(null);
        setLoading(true);

        try {
            const requestBody = {
                length: parseFloat(formData.length),
                width: parseFloat(formData.width),
                height: parseFloat(formData.height),
                weight: parseFloat(formData.weight),
                category: formData.category || null,
                sellingPrice: parseFloat(formData.sellingPrice),
                timeInStorage: formData.timeInStorage ? parseFloat(formData.timeInStorage) : 1,
                freightCost: formData.freightCost ? parseFloat(formData.freightCost) : null,
                freightCostUnit: formData.freightCostUnit ? parseFloat(formData.freightCostUnit) : 1,
                otherCosts: formData.otherCosts ? parseFloat(formData.otherCosts) : 0,
                otherCostsType: formData.otherCostsType || 'fixed',
                fbaFeeCategory: formData.fbaFeeCategory || 'Most goods',
                referralFeePercentage: formData.referralFeePercentage ? parseFloat(formData.referralFeePercentage) : null,
            };

            const data = await calculateFees(requestBody);
            setResults(data);
        } catch (err) {
            setError(err.message || 'Failed to calculate fees');
            setResults(null);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 max-w-4xl mx-auto transition-colors duration-200">
            <div className="bg-gradient-to-r from-gray-50 via-slate-50 to-gray-50 dark:from-gray-700 dark:via-gray-800 dark:to-gray-700 py-8 border-b border-gray-200 dark:border-gray-700 transition-colors duration-200">
                <div className="text-center">
                    <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
                        Profitability Calculator
                    </h2>
                    <div className="mt-2 w-24 h-1 bg-gray-900 dark:bg-gray-100 mx-auto rounded-full"></div>
                </div>
            </div>

            <form onSubmit={handleCalculate} className="p-8">
                <div className="mb-8 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg p-5 border border-blue-200 dark:border-blue-800 transition-colors duration-200">
                    <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
                        Search by SKU
                    </label>
                    <div className="flex gap-3">
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            onKeyPress={handleSearchKeyPress}
                            placeholder="Enter SKU to auto-fill fields..."
                            className="flex-1 px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 transition-colors duration-200"
                        />
                        <button
                            type="button"
                            onClick={handleSearch}
                            disabled={searching}
                            className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 dark:from-indigo-700 dark:to-blue-700 text-white px-6 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 dark:hover:from-indigo-800 dark:hover:to-blue-800 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                        >
                            {searching ? 'Searching...' : 'Search'}
                        </button>
                    </div>
                    {searchError && (
                        <p className="text-red-600 dark:text-red-400 text-sm mt-2 font-medium">{searchError}</p>
                    )}
                </div>

                <div className="flex gap-6 mb-8 pb-6 border-b border-gray-200 dark:border-gray-700">
                    <div className="w-32 h-32 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600 flex items-center justify-center flex-shrink-0">
                        <svg className="w-16 h-16 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                    <div className="flex-1">
                        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                            {formData.productName || 'Product Name'}
                        </h3>
                        <p className="text-sm text-gray-600 dark:text-gray-300 leading-relaxed">
                            {formData.description || 'Product description will appear here when a SKU is selected.'}
                        </p>
                    </div>
                </div>

                <div className="space-y-4 mb-8">
                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700 dark:text-gray-300">Dimensions:</label>
                        <div className="flex items-center gap-2">
                            <div className="w-20 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-sm">
                                {formData.length || '-'}
                            </div>
                            <span className="text-gray-500 dark:text-gray-400">x</span>
                            <div className="w-20 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-sm">
                                {formData.width || '-'}
                            </div>
                            <span className="text-gray-500 dark:text-gray-400">x</span>
                            <div className="w-20 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-sm">
                                {formData.height || '-'}
                            </div>
                            <span className="text-gray-500 dark:text-gray-400 text-sm">in.</span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700 dark:text-gray-300">Weight:</label>
                        <div className="flex items-center gap-2">
                            <div className="w-32 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-sm">
                                {formData.weight || '-'}
                            </div>
                            <span className="text-gray-500 dark:text-gray-400 text-sm">lbs</span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700 dark:text-gray-300">Size Tier:</label>
                        <div className="text-sm font-medium text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-700 px-4 py-2 rounded-lg border border-gray-200 dark:border-gray-600">
                            {results?.sizeTier || '-'}
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700 dark:text-gray-300">Price:</label>
                        <div className="flex items-center gap-2">
                            <span className="text-gray-500 dark:text-gray-400">$</span>
                            <input
                                type="number"
                                step="0.01"
                                name="sellingPrice"
                                value={formData.sellingPrice}
                                onChange={handleChange}
                                className="w-32 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white transition-colors duration-200"
                                placeholder="22.99"
                                required
                            />
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700 dark:text-gray-300">Est. Time in Storage:</label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                step="0.1"
                                name="timeInStorage"
                                value={formData.timeInStorage}
                                onChange={handleChange}
                                className="w-32 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white transition-colors duration-200"
                                placeholder="1"
                            />
                            <span className="text-gray-500 dark:text-gray-400 text-sm">months</span>
                        </div>
                    </div>

                </div>

                <div className="space-y-4 mb-8 border-t border-gray-200 dark:border-gray-700 pt-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">Storage Fee (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {results ? `$ ${parseFloat(results.storageFeeJanSep || 0).toFixed(2)}/${parseFloat(results.storageFeeOctDec || 0).toFixed(2)}` : '$ 0.00/0.00'}
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">FBA Fee</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="text-sm font-medium text-gray-900 dark:text-white">
                                {results ? `$ ${parseFloat(results.fbaFulfillmentFee || 0).toFixed(2)}` : '$ 0.00'}
                            </div>
                            <select
                                name="fbaFeeCategory"
                                value={formData.fbaFeeCategory}
                                onChange={handleChange}
                                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm transition-colors duration-200"
                            >
                                <option>Most goods</option>
                                <option>Apparel</option>
                                <option>Electronics</option>
                            </select>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">Amazon Referral Fee (15%)</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {results ? `$ ${parseFloat(results.referralFee || 0).toFixed(2)}` : '$ 0.00'}
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">Additional Costs</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                step="0.01"
                                name="otherCosts"
                                value={formData.otherCosts}
                                onChange={handleChange}
                                className="w-20 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm transition-colors duration-200"
                                placeholder="0"
                            />
                            <select
                                name="otherCostsType"
                                value={formData.otherCostsType}
                                onChange={handleChange}
                                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm transition-colors duration-200"
                            >
                                <option value="fixed">$</option>
                                <option value="percentage">%</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div className="border-t border-gray-200 dark:border-gray-700 pt-6 space-y-4 mb-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">Net (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <p className={`text-sm font-medium ${results ? (parseFloat(results.netProfitJanSep || 0) >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400') : 'text-gray-500 dark:text-gray-400'}`}>
                            {results ? `$ ${parseFloat(results.netProfitJanSep || 0).toFixed(2)}/${parseFloat(results.netProfitOctDec || 0).toFixed(2)}` : '$ 0.00/0.00'}
                        </p>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">Margin (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <p className={`text-sm font-medium ${results ? (parseFloat(results.profitMarginJanSep || 0) >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400') : 'text-gray-500 dark:text-gray-400'}`}>
                            {results ? `${parseFloat(results.profitMarginJanSep || 0).toFixed(2)}/${parseFloat(results.profitMarginOctDec || 0).toFixed(2)} %` : '0.00/0.00 %'}
                        </p>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700 dark:text-gray-300">ROI (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400 dark:text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <p className={`text-sm font-medium ${results ? (parseFloat(results.roiJanSep || 0) >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400') : 'text-gray-500 dark:text-gray-400'}`}>
                            {results && results.otherCosts > 0 
                                ? `${parseFloat(results.roiJanSep || 0).toFixed(1)}/${parseFloat(results.roiOctDec || 0).toFixed(1)} %` 
                                : '-'}
                        </p>
                    </div>
                </div>

                <div className="mb-6">
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full inline-flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 dark:from-indigo-700 dark:to-blue-700 text-white px-6 py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 dark:hover:from-indigo-800 dark:hover:to-blue-800 transition-all duration-200 shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? (
                            <>
                                <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Calculating...
                            </>
                        ) : (
                            <>
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-5m-6 5h.01M19 10a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                Calculate
                            </>
                        )}
                    </button>
                </div>

                {error && (
                    <div className="mb-6 bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 dark:border-red-600 text-red-800 dark:text-red-300 px-5 py-4 rounded-r-lg">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                            <span className="font-medium">{error}</span>
                        </div>
                    </div>
                )}
            </form>
        </div>
    );
}

export default Calculator;
