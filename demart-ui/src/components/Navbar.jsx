import React from 'react';
import { ShoppingCart, LogIn, LogOut, Store, ShieldCheck, Search, Sparkles, Package } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar = ({
  onOpenAuth,
  onOpenCart,
  activeTab,
  setActiveTab,
  searchTerm,
  setSearchTerm,
}) => {
  const { user, isAuthenticated, logout, isStaff, isAdmin } = useAuth();
  const { itemCount } = useCart();

  return (
    <header className="sticky top-0 z-40 glass-nav transition-all duration-200">
      {/* Eye-Catching Top Announcement Bar */}
      <div className="bg-gradient-to-r from-emerald-600 via-teal-500 to-lime-500 text-slate-950 text-xs text-center py-1.5 font-extrabold tracking-wide flex items-center justify-center gap-2 shadow-sm">
        <Sparkles className="w-3.5 h-3.5 animate-spin" />
        <span>MINI D-MART EXPRESS: Free Home Delivery on orders over ₹500 | Store Pickup Ready in 30 Mins!</span>
        <Sparkles className="w-3.5 h-3.5" />
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-20 gap-4">
          {/* Logo */}
          <div
            className="flex items-center gap-3 cursor-pointer group"
            onClick={() => setActiveTab('shop')}
          >
            <div className="w-12 h-12 bg-gradient-to-br from-emerald-400 to-teal-600 rounded-2xl flex items-center justify-center text-slate-950 font-black text-2xl shadow-lg glow-emerald group-hover:scale-105 transition-transform duration-200">
              DM
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="text-2xl font-black tracking-tight text-white group-hover:text-emerald-400 transition">
                  Mini D-Mart
                </span>
                <span className="bg-emerald-500/20 text-emerald-400 text-[10px] font-bold px-2 py-0.5 rounded-full border border-emerald-500/30">
                  LIVE
                </span>
              </div>
              <span className="block text-[11px] uppercase tracking-widest font-bold text-slate-400">
                Supermarket & Grocery
              </span>
            </div>
          </div>

          {/* Search Bar (visible on shop tab) */}
          {activeTab === 'shop' && (
            <div className="flex-1 max-w-lg relative hidden md:block">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-emerald-400" />
              <input
                type="text"
                placeholder="Search fresh oil, milk, rice, snacks..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-11 pr-4 py-2.5 text-xs bg-slate-800/80 border border-slate-700 text-white placeholder-slate-400 rounded-full focus:bg-slate-900 focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/20 transition shadow-inner"
              />
            </div>
          )}

          {/* Navigation Tabs */}
          <div className="flex items-center gap-1.5 bg-slate-800/60 p-1.5 rounded-2xl border border-slate-700/60">
            <button
              onClick={() => setActiveTab('shop')}
              className={`px-3.5 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
                activeTab === 'shop'
                  ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 shadow-md glow-emerald'
                  : 'text-slate-300 hover:text-white hover:bg-slate-700/50'
              }`}
            >
              Shop Catalog
            </button>

            {isAuthenticated && (
              <button
                onClick={() => setActiveTab('orders')}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
                  activeTab === 'orders'
                    ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 shadow-md glow-emerald'
                    : 'text-slate-300 hover:text-white hover:bg-slate-700/50'
                }`}
              >
                <Package className="w-3.5 h-3.5" />
                My Orders & Returns
              </button>
            )}

            {isStaff && (
              <button
                onClick={() => setActiveTab('staff')}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
                  activeTab === 'staff'
                    ? 'bg-gradient-to-r from-amber-400 to-orange-500 text-slate-950 shadow-md glow-lime'
                    : 'text-amber-400 hover:bg-amber-500/10'
                }`}
              >
                <Store className="w-3.5 h-3.5" />
                Staff Ops
              </button>
            )}

            {isAdmin && (
              <button
                onClick={() => setActiveTab('admin')}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
                  activeTab === 'admin'
                    ? 'bg-gradient-to-r from-purple-500 to-indigo-500 text-white shadow-md glow-indigo'
                    : 'text-purple-400 hover:bg-purple-500/10'
                }`}
              >
                <ShieldCheck className="w-3.5 h-3.5" />
                Admin Console
              </button>
            )}
          </div>

          {/* User Controls & Cart Drawer Button */}
          <div className="flex items-center gap-3">
            {/* Cart Trigger */}
            <button
              onClick={onOpenCart}
              className="relative p-3 bg-slate-800 hover:bg-slate-700 text-emerald-400 rounded-2xl border border-slate-700 transition glow-emerald group"
              title="Open Shopping Cart"
            >
              <ShoppingCart className="w-5 h-5 group-hover:scale-110 transition-transform" />
              {itemCount > 0 && (
                <span className="absolute -top-1.5 -right-1.5 bg-lime-400 text-slate-950 text-xs font-black w-6 h-6 rounded-full flex items-center justify-center shadow-lg border-2 border-slate-900 animate-pulse">
                  {itemCount}
                </span>
              )}
            </button>

            {/* Auth State Button */}
            {isAuthenticated ? (
              <div className="flex items-center gap-3 border-l border-slate-700/80 pl-3">
                <div className="text-right hidden sm:block">
                  <div className="text-xs font-black text-white">{user.name}</div>
                  <span className="inline-block text-[10px] px-2 py-0.2 rounded-full font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    {user.role}
                  </span>
                </div>
                <button
                  onClick={logout}
                  className="p-2.5 bg-red-500/10 text-red-400 hover:bg-red-500 hover:text-white rounded-2xl border border-red-500/20 transition"
                  title="Logout"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <button
                onClick={onOpenAuth}
                className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 text-xs font-black rounded-2xl shadow-lg glow-emerald transition transform hover:-translate-y-0.5"
              >
                <LogIn className="w-4 h-4" />
                Login / Register
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
