import React, { useState } from 'react';
import { X, Trash2, Plus, Minus, Truck, Store, AlertCircle, Sparkles, ShoppingCart } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import apiClient from '../api/client';

export const CartDrawer = ({ isOpen, onClose, onOrderPlaced, onOpenAuth }) => {
  const {
    cartItems,
    updateQuantity,
    removeFromCart,
    clearCart,
    subtotal,
    deliveryCharge,
    freeDeliveryThreshold,
    total,
  } = useCart();
  const { isAuthenticated } = useAuth();

  const [fulfillmentType, setFulfillmentType] = useState('HOME_DELIVERY');
  const [pickupSlot, setPickupSlot] = useState('');
  const [deliveryAddress, setDeliveryAddress] = useState({
    street: '',
    city: '',
    state: '',
    pincode: '',
    landmark: '',
  });
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleCheckout = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      onOpenAuth();
      return;
    }

    if (cartItems.length === 0) return;

    setError('');
    setLoading(true);

    try {
      const payload = {
        fulfillmentType,
        items: cartItems.map((item) => ({
          productId: item.product.id,
          quantity: item.quantity,
        })),
        notes,
      };

      if (fulfillmentType === 'HOME_DELIVERY') {
        payload.deliveryAddress = deliveryAddress;
      } else {
        payload.pickupSlot = pickupSlot;
      }

      const res = await apiClient.post('/api/v1/orders', payload);
      clearCart();
      onClose();
      onOrderPlaced(res.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const progressPercent = Math.min(100, (subtotal / freeDeliveryThreshold) * 100);

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-slate-950/70 backdrop-blur-md flex justify-end">
      <div className="w-full max-w-md bg-slate-900 border-l border-slate-800 text-white h-full flex flex-col shadow-2xl animate-in slide-in-from-right duration-200">
        {/* Header */}
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-500/20 text-emerald-400 rounded-xl border border-emerald-500/30">
              <ShoppingCart className="w-5 h-5" />
            </div>
            <div>
              <h2 className="font-black text-base tracking-tight">Shopping Cart</h2>
              <span className="text-[11px] text-slate-400 font-semibold">{cartItems.length} Unique Items</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Free Delivery Bar */}
        {cartItems.length > 0 && (
          <div className="bg-slate-800/80 p-3.5 border-b border-slate-800 text-xs">
            <div className="flex justify-between font-extrabold text-slate-200 mb-1.5">
              <span className="flex items-center gap-1 text-emerald-400">
                <Sparkles className="w-3.5 h-3.5" />
                {subtotal >= freeDeliveryThreshold
                  ? 'FREE Delivery Unlocked!'
                  : `Add ₹${(freeDeliveryThreshold - subtotal).toFixed(2)} for FREE Delivery`}
              </span>
              <span className="text-slate-400">{progressPercent.toFixed(0)}%</span>
            </div>
            <div className="w-full bg-slate-700 h-2 rounded-full overflow-hidden">
              <div
                className="bg-gradient-to-r from-emerald-400 to-lime-400 h-full transition-all duration-300 shadow-lg glow-lime"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
          </div>
        )}

        {/* Cart Items List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {error && (
            <div className="bg-red-500/10 text-red-400 p-3 rounded-2xl text-xs flex items-center gap-2 border border-red-500/20">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {cartItems.length === 0 ? (
            <div className="text-center py-20 text-slate-500 space-y-3">
              <ShoppingCart className="w-16 h-16 mx-auto text-slate-700 stroke-1" />
              <p className="font-bold text-slate-300">Your cart is currently empty</p>
              <p className="text-xs text-slate-500">Explore our catalog to add fresh groceries</p>
            </div>
          ) : (
            cartItems.map((item) => (
              <div
                key={item.product.id}
                className="flex items-center justify-between bg-slate-800/60 p-3.5 rounded-2xl border border-slate-700/60 gap-3"
              >
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-slate-900 rounded-xl p-1 border border-slate-700 shrink-0 flex items-center justify-center font-black text-emerald-400">
                    {item.product.name.charAt(0)}
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white line-clamp-1">
                      {item.product.name}
                    </h4>
                    <div className="text-[11px] text-slate-400">
                      ₹{item.product.sellingPrice} / {item.product.unit}
                    </div>
                    <div className="text-xs font-black text-emerald-400 mt-0.5">
                      ₹{(item.product.sellingPrice * item.quantity).toFixed(2)}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-1 bg-slate-900 border border-slate-700 rounded-xl p-1">
                    <button
                      onClick={() => updateQuantity(item.product.id, item.quantity - 1)}
                      className="p-1 text-slate-400 hover:text-white rounded"
                    >
                      <Minus className="w-3 h-3" />
                    </button>
                    <span className="w-5 text-center text-xs font-black text-white">
                      {item.quantity}
                    </span>
                    <button
                      onClick={() => updateQuantity(item.product.id, item.quantity + 1)}
                      className="p-1 text-slate-400 hover:text-white rounded disabled:opacity-30"
                      disabled={item.quantity >= item.product.stockQuantity}
                    >
                      <Plus className="w-3 h-3" />
                    </button>
                  </div>
                  <button
                    onClick={() => removeFromCart(item.product.id)}
                    className="p-1.5 text-red-400 hover:bg-red-500/10 rounded-xl transition"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Checkout Form */}
        {cartItems.length > 0 && (
          <form onSubmit={handleCheckout} className="p-5 bg-slate-950 border-t border-slate-800 space-y-3">
            {/* Fulfillment Toggle */}
            <div className="grid grid-cols-2 gap-2 bg-slate-900 p-1 rounded-2xl border border-slate-800">
              <button
                type="button"
                onClick={() => setFulfillmentType('HOME_DELIVERY')}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl text-xs font-black transition ${
                  fulfillmentType === 'HOME_DELIVERY'
                    ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 shadow-md glow-emerald'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <Truck className="w-3.5 h-3.5" />
                Home Delivery
              </button>
              <button
                type="button"
                onClick={() => setFulfillmentType('STORE_PICKUP')}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl text-xs font-black transition ${
                  fulfillmentType === 'STORE_PICKUP'
                    ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 shadow-md glow-emerald'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <Store className="w-3.5 h-3.5" />
                Store Pickup
              </button>
            </div>

            {/* Inputs */}
            {fulfillmentType === 'HOME_DELIVERY' ? (
              <div className="space-y-2 text-xs">
                <input
                  type="text"
                  placeholder="Street / Flat / House No."
                  required
                  value={deliveryAddress.street}
                  onChange={(e) => setDeliveryAddress({ ...deliveryAddress, street: e.target.value })}
                  className="w-full p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-emerald-400"
                />
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="text"
                    placeholder="City"
                    required
                    value={deliveryAddress.city}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, city: e.target.value })}
                    className="w-full p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-emerald-400"
                  />
                  <input
                    type="text"
                    placeholder="State"
                    required
                    value={deliveryAddress.state}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, state: e.target.value })}
                    className="w-full p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-emerald-400"
                  />
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="text"
                    placeholder="6-digit Pincode"
                    pattern="[1-9][0-9]{5}"
                    required
                    value={deliveryAddress.pincode}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, pincode: e.target.value })}
                    className="w-full p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-emerald-400"
                  />
                  <input
                    type="text"
                    placeholder="Landmark (Optional)"
                    value={deliveryAddress.landmark}
                    onChange={(e) => setDeliveryAddress({ ...deliveryAddress, landmark: e.target.value })}
                    className="w-full p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-emerald-400"
                  />
                </div>
              </div>
            ) : (
              <div className="text-xs space-y-1">
                <label className="font-bold text-slate-300">Select Pickup Time Slot:</label>
                <input
                  type="datetime-local"
                  required
                  value={pickupSlot}
                  onChange={(e) => setPickupSlot(e.target.value)}
                  className="w-full p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-emerald-400"
                />
              </div>
            )}

            {/* Total Breakdown */}
            <div className="border-t border-slate-800 pt-2 space-y-1 text-xs text-slate-400">
              <div className="flex justify-between">
                <span>Subtotal</span>
                <span>₹{subtotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Delivery Fee</span>
                <span>{deliveryCharge === 0 ? <strong className="text-emerald-400">FREE</strong> : `₹${deliveryCharge}`}</span>
              </div>
              <div className="flex justify-between text-sm font-black text-white pt-1 border-t border-slate-800">
                <span>Total Amount</span>
                <span className="text-emerald-400">₹{total.toFixed(2)}</span>
              </div>
            </div>

            {/* Submit */}
            {!isAuthenticated ? (
              <button
                type="button"
                onClick={onOpenAuth}
                className="w-full py-3 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs rounded-2xl shadow-lg transition"
              >
                Sign In to Complete Checkout
              </button>
            ) : (
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 font-black text-sm rounded-2xl shadow-xl glow-emerald transition disabled:opacity-50"
              >
                {loading ? 'Processing Order...' : `Place Order • ₹${total.toFixed(2)}`}
              </button>
            )}
          </form>
        )}
      </div>
    </div>
  );
};
