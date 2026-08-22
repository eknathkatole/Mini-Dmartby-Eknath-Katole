import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { Navbar } from './components/Navbar';
import { CartDrawer } from './components/CartDrawer';
import { AuthModal } from './components/AuthModal';
import { ShopCatalogView } from './views/ShopCatalogView';
import { MyOrdersView } from './views/MyOrdersView';
import { StaffDashboardView } from './views/StaffDashboardView';
import { AdminDashboardView } from './views/AdminDashboardView';

const MainApp = () => {
  const [activeTab, setActiveTab] = useState('shop'); // 'shop' | 'orders' | 'staff' | 'admin'
  const [searchTerm, setSearchTerm] = useState('');
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [lastPlacedOrder, setLastPlacedOrder] = useState(null);

  const { isStaff, isAdmin } = useAuth();

  const handleOrderPlaced = (order) => {
    setLastPlacedOrder(order);
    setActiveTab('orders');
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 flex flex-col font-sans">
      <Navbar
        onOpenAuth={() => setIsAuthOpen(true)}
        onOpenCart={() => setIsCartOpen(true)}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {activeTab === 'shop' && <ShopCatalogView searchTerm={searchTerm} />}
        {activeTab === 'orders' && <MyOrdersView />}
        {activeTab === 'staff' && isStaff && <StaffDashboardView />}
        {activeTab === 'admin' && isAdmin && <AdminDashboardView />}
      </main>

      <footer className="bg-emerald-950 text-emerald-200 text-xs py-8 border-t border-emerald-900 mt-auto">
        <div className="max-w-7xl mx-auto px-4 text-center space-y-2">
          <div className="font-bold text-white text-sm">Mini D-Mart — Grocery Store Application</div>
          <div>Full Stack Developer Assessment · Connected to Live Render Backend API</div>
          <div className="text-[10px] text-emerald-400">© 2026 Eknath Katole. All rights reserved.</div>
        </div>
      </footer>

      <CartDrawer
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        onOrderPlaced={handleOrderPlaced}
        onOpenAuth={() => {
          setIsCartOpen(false);
          setIsAuthOpen(true);
        }}
      />

      <AuthModal
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
      />
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <MainApp />
      </CartProvider>
    </AuthProvider>
  );
}
