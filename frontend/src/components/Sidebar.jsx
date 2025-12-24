function Sidebar({ activeSection, onSectionChange }) {
    const menuItems = [
        { id: 'dashboard', label: 'Dashboard' },
        { id: 'calculator', label: 'Calculator' },
        { id: 'skus', label: 'My SKUs' },
        { id: 'lists', label: 'My Lists' },
    ];

    return (
        <div className="w-64 bg-white shadow-lg min-h-screen fixed left-0 top-16">
            <div className="p-4 pt-6">
                <nav className="space-y-2">
                    {menuItems.map((item) => (
                        <button
                            key={item.id}
                            onClick={() => onSectionChange(item.id)}
                            className={`w-full text-left flex items-center px-4 py-3 rounded-lg transition-all duration-200 ${
                                activeSection === item.id
                                    ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-md font-semibold'
                                    : 'text-gray-700 hover:bg-gray-100 font-medium'
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

