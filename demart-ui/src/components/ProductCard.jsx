import React from 'react';
import { Plus, Minus, ShoppingBag, AlertTriangle, Sparkles } from 'lucide-react';
import { useCart } from '../context/CartContext';

export const ProductCard = ({ product }) => {
  const { cartItems, addToCart, updateQuantity } = useCart();
  const cartItem = cartItems.find((item) => item.product.id === product.id);
  const currentQuantity = cartItem ? cartItem.quantity : 0;

  const isOutOfStock = !product.inStock || product.stockQuantity === 0;
  const isLowStock = product.lowStock;

  return (
    <div className="bg-slate-800/80 border border-slate-700/80 rounded-3xl overflow-hidden hover:border-emerald-500/50 hover:shadow-2xl hover:shadow-emerald-500/10 transition-all duration-300 flex flex-col justify-between group">
      {/* Image & Badges Container */}
      <div className="relative aspect-square bg-slate-900/60 flex items-center justify-center p-6 overflow-hidden">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-contain group-hover:scale-110 transition-transform duration-300"
          />
        ) : (
          <div className="w-24 h-24 bg-gradient-to-br from-emerald-500/20 to-teal-500/20 text-emerald-400 rounded-3xl flex items-center justify-center font-black text-3xl border border-emerald-500/30">
            {product.name.charAt(0)}
          </div>
        )}

        {/* Top Floating Badges */}
        <div className="absolute top-3 left-3 flex flex-col gap-1.5 items-start z-10">
          {product.discountPercent > 0 && (
            <span className="bg-gradient-to-r from-red-500 to-rose-600 text-white text-[11px] font-black px-2.5 py-0.5 rounded-full shadow-lg flex items-center gap-1">
              <Sparkles className="w-3 h-3" />
              SAVE {product.discountPercent}%
            </span>
          )}
          <span className="bg-slate-950/80 backdrop-blur-md text-emerald-300 border border-emerald-500/30 text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider">
            {product.unit}
          </span>
        </div>

        {/* Low Stock / Out of Stock Banner */}
        {isOutOfStock ? (
          <div className="absolute inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center z-20">
            <span className="bg-slate-800 text-slate-300 border border-slate-700 text-xs font-black px-4 py-1.5 rounded-full uppercase tracking-wider">
              Out of Stock
            </span>
          </div>
        ) : isLowStock ? (
          <span className="absolute bottom-3 left-3 bg-amber-500/90 text-slate-950 text-[10px] font-black px-2.5 py-0.5 rounded-full flex items-center gap-1 shadow-md border border-amber-400">
            <AlertTriangle className="w-3 h-3" />
            Only {product.stockQuantity} Left!
          </span>
        ) : null}
      </div>

      {/* Content */}
      <div className="p-5 flex-1 flex flex-col justify-between">
        <div>
          <span className="text-[10px] font-extrabold text-emerald-400 uppercase tracking-widest block mb-1">
            {product.categoryName}
          </span>
          <h3 className="text-sm font-bold text-white line-clamp-2 leading-snug group-hover:text-emerald-300 transition">
            {product.name}
          </h3>
          <p className="text-[11px] text-slate-400 line-clamp-1 mt-1 mb-4">
            {product.description || 'Fresh daily essential'}
          </p>
        </div>

        {/* Pricing & Cart Action Bar */}
        <div className="pt-3 border-t border-slate-700/60 flex items-center justify-between gap-2">
          <div>
            <div className="text-[11px] text-slate-400 font-semibold line-through">
              ₹{product.mrpPrice}
            </div>
            <div className="text-xl font-black text-white tracking-tight">
              ₹{product.sellingPrice}
            </div>
          </div>

          {/* Cart Stepper or Add Button */}
          {!isOutOfStock && (
            <div>
              {currentQuantity > 0 ? (
                <div className="flex items-center gap-1.5 bg-slate-900 border border-emerald-500/50 p-1 rounded-xl shadow-lg glow-emerald">
                  <button
                    onClick={() => updateQuantity(product.id, currentQuantity - 1)}
                    className="p-1.5 bg-slate-800 text-emerald-400 hover:bg-emerald-600 hover:text-white rounded-lg transition"
                  >
                    <Minus className="w-3.5 h-3.5" />
                  </button>
                  <span className="w-6 text-center text-xs font-black text-white">
                    {currentQuantity}
                  </span>
                  <button
                    onClick={() => {
                      if (currentQuantity < product.stockQuantity) {
                        updateQuantity(product.id, currentQuantity + 1);
                      }
                    }}
                    className="p-1.5 bg-slate-800 text-emerald-400 hover:bg-emerald-600 hover:text-white rounded-lg transition disabled:opacity-30"
                    disabled={currentQuantity >= product.stockQuantity}
                  >
                    <Plus className="w-3.5 h-3.5" />
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => addToCart(product, 1)}
                  className="flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 text-xs font-black rounded-xl shadow-lg glow-emerald transition transform active:scale-95"
                >
                  <ShoppingBag className="w-3.5 h-3.5" />
                  ADD
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
