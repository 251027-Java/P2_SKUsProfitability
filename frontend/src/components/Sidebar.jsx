function Sidebar({ activeSection, onSectionChange }) {
    const menuItems = [
        { id: 'dashboard', label: 'Dashboard' },
        { id: 'calculator', label: 'Calculator' },
        { id: 'skus', label: 'My SKUs' },
        { id: 'lists', label: 'My Lists' },
    ];

    return (
        <div className="w-64 bg-white dark:bg-gray-800 shadow-lg h-screen fixed left-0 top-0 transition-colors duration-200 pt-20">
            <div className="p-4 pt-6">
                <nav className="space-y-2">
                    {menuItems.map((item) => (
                        <button
                            key={item.id}
                            onClick={() => onSectionChange(item.id)}
                            className={`w-full text-left flex items-center px-4 py-3 rounded-lg transition-all duration-200 ${
                                activeSection === item.id
                                    ? 'bg-gradient-to-r from-indigo-600 to-blue-600 dark:from-indigo-700 dark:to-blue-700 text-white shadow-md font-semibold'
                                    : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 font-medium'
                            }`}
                        >
                            {item.label}
                        </button>
                    ))}
                </nav>
            </div>
        </div>
    );
}

export default Sidebar;

