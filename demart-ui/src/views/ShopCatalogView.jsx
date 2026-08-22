import React, { useState, useEffect } from 'react';
import { ProductCard } from '../components/ProductCard';
import { Sparkles, Tag, ArrowRight, ShieldCheck, Truck, Store } from 'lucide-react';
import apiClient from '../api/client';

export const ShopCatalogView = ({ searchTerm }) => {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [inStockOnly, setInStockOnly] = useState(false);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchProducts();
  }, [selectedCategoryId, inStockOnly, sortBy, sortDir, searchTerm]);

  const fetchCategories = async () => {
    try {
      const res = await apiClient.get('/api/v1/categories');
      setCategories(res.data || []);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const fetchProducts = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {
        page: 0,
        size: 50,
        inStockOnly,
        sortBy,
        sortDir,
      };
      if (selectedCategoryId) params.categoryId = selectedCategoryId;
      if (searchTerm) params.search = searchTerm;

      const res = await apiClient.get('/api/v1/products', { params });
      setProducts(res.data?.content || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-8 pb-16">
      {/* Eye-Catching Hero Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-emerald-900 via-slate-900 to-teal-900 border border-emerald-500/20 p-8 sm:p-10 shadow-2xl glow-emerald">
        <div className="relative z-10 max-w-2xl space-y-4">
          <div className="inline-flex items-center gap-2 bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-black px-3.5 py-1 rounded-full uppercase tracking-wider">
            <Sparkles className="w-3.5 h-3.5 text-lime-400" />
            <span>Supermarket Fresh Guarantee</span>
          </div>

          <h1 className="text-3xl sm:text-5xl font-black text-white tracking-tight leading-none">
            Fresh Groceries, <span className="bg-gradient-to-r from-emerald-400 via-teal-300 to-lime-400 bg-clip-text text-transparent">Lowest Prices</span> Every Day.
          </h1>

          <p className="text-xs sm:text-sm text-slate-300 font-medium max-w-lg leading-relaxed">
            Shop oil, milk, staples, and packaged foods directly with up to 20% discount. Choose express Home Delivery or 30-Min Store Pickup.
          </p>

          <div className="flex flex-wrap gap-4 pt-2 text-xs font-bold text-slate-300">
            <div className="flex items-center gap-2 bg-slate-800/80 px-3.5 py-2 rounded-2xl border border-slate-700">
              <Truck className="w-4 h-4 text-emerald-400" />
              <span>Free Delivery &gt; ₹500</span>
            </div>
            <div className="flex items-center gap-2 bg-slate-800/80 px-3.5 py-2 rounded-2xl border border-slate-700">
              <Store className="w-4 h-4 text-lime-400" />
              <span>Instant Store Pickup</span>
            </div>
            <div className="flex items-center gap-2 bg-slate-800/80 px-3.5 py-2 rounded-2xl border border-slate-700">
              <ShieldCheck className="w-4 h-4 text-teal-400" />
              <span>7-Day Return Policy</span>
            </div>
          </div>
        </div>

        {/* Ambient Glow Graphic */}
        <div className="absolute right-0 top-0 bottom-0 w-1/3 bg-gradient-to-l from-emerald-500/10 to-transparent pointer-events-none" />
      </div>

      {/* Category Pills Slider */}
      <div className="space-y-3">
        <div className="flex items-center justify-between text-xs font-bold text-slate-400 px-1">
          <span className="uppercase tracking-widest flex items-center gap-1.5 text-white">
            <Tag className="w-4 h-4 text-emerald-400" />
            Explore Categories
          </span>
          <span>{categories.length} Categories Available</span>
        </div>

        <div className="flex items-center gap-2.5 overflow-x-auto pb-2 no-scrollbar">
          <button
            onClick={() => setSelectedCategoryId(null)}
            className={`px-5 py-2.5 rounded-2xl text-xs font-black whitespace-nowrap transition-all duration-200 shadow-md ${
              selectedCategoryId === null
                ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 glow-emerald scale-105'
                : 'bg-slate-800/90 text-slate-300 hover:bg-slate-700 border border-slate-700/80'
            }`}
          >
            All Products
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategoryId(cat.id)}
              className={`px-5 py-2.5 rounded-2xl text-xs font-black whitespace-nowrap transition-all duration-200 shadow-md flex items-center gap-2 ${
                selectedCategoryId === cat.id
                  ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 glow-emerald scale-105'
                  : 'bg-slate-800/90 text-slate-300 hover:bg-slate-700 border border-slate-700/80'
              }`}
            >
              <span>{cat.name}</span>
              {cat.productCount > 0 && (
                <span
                  className={`text-[10px] px-2 py-0.2 rounded-full font-extrabold ${
                    selectedCategoryId === cat.id
                      ? 'bg-slate-950 text-emerald-400'
                      : 'bg-slate-900 text-slate-400'
                  }`}
                >
                  {cat.productCount}
                </span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Filter & Sort Bar */}
      <div className="bg-slate-800/60 p-4 rounded-3xl border border-slate-700/80 flex flex-wrap items-center justify-between gap-4 text-xs font-semibold">
        <div className="flex items-center gap-6">
          <label className="flex items-center gap-2.5 cursor-pointer select-none text-slate-300 hover:text-white">
            <input
              type="checkbox"
              checked={inStockOnly}
              onChange={(e) => setInStockOnly(e.target.checked)}
              className="w-4 h-4 rounded text-emerald-500 bg-slate-900 border-slate-700 focus:ring-emerald-500"
            />
            <span>Show In-Stock Only</span>
          </label>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-slate-400">Sort by:</span>
          <select
            value={`${sortBy}-${sortDir}`}
            onChange={(e) => {
              const [by, dir] = e.target.value.split('-');
              setSortBy(by);
              setSortDir(dir);
            }}
            className="px-3 py-1.5 border border-slate-700 rounded-xl bg-slate-900 text-white focus:outline-none focus:border-emerald-400 text-xs font-bold"
          >
            <option value="createdAt-desc">Newest Additions</option>
            <option value="sellingPrice-asc">Price: Low to High</option>
            <option value="sellingPrice-desc">Price: High to Low</option>
            <option value="name-asc">Alphabetical: A to Z</option>
          </select>
        </div>
      </div>

      {/* Product Grid */}
      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((n) => (
            <div
              key={n}
              className="bg-slate-800/50 border border-slate-700/50 rounded-3xl h-72 animate-pulse"
            />
          ))}
        </div>
      ) : error ? (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-8 rounded-3xl text-center space-y-2">
          <p className="font-black text-sm">Failed to connect to catalog service</p>
          <p className="text-xs text-red-300">{error}</p>
        </div>
      ) : products.length === 0 ? (
        <div className="bg-slate-800/40 rounded-3xl p-16 text-center border border-slate-700/60 space-y-2">
          <p className="font-black text-white text-base">No products match your criteria</p>
          <p className="text-xs text-slate-400">Try adjusting your filters or category selection</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
};
