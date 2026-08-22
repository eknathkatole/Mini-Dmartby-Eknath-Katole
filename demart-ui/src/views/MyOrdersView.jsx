import React, { useState, useEffect } from 'react';
import { Package, Truck, Store, Clock, RotateCcw, CheckCircle2 } from 'lucide-react';
import apiClient from '../api/client';
import { ReturnModal } from '../components/ReturnModal';

export const MyOrdersView = () => {
  const [orders, setOrders] = useState([]);
  const [returns, setReturns] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [selectedOrderForReturn, setSelectedOrderForReturn] = useState(null);
  const [activeTab, setActiveTab] = useState('orders'); // 'orders' | 'returns'

  useEffect(() => {
    fetchOrdersAndReturns();
    fetchProducts();
  }, []);

  const fetchOrdersAndReturns = async () => {
    setLoading(true);
    setError('');
    try {
      const [ordersRes, returnsRes] = await Promise.all([
        apiClient.get('/api/v1/orders'),
        apiClient.get('/api/v1/orders/my-returns'),
      ]);
      setOrders(ordersRes.data || []);
      setReturns(returnsRes.data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await apiClient.get('/api/v1/products?size=100');
      setProducts(res.data?.content || []);
    } catch (err) {
      console.error('Failed to load products', err);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    try {
      await apiClient.patch(`/api/v1/orders/${orderId}/cancel`);
      fetchOrdersAndReturns();
    } catch (err) {
      alert(err.message);
    }
  };

  const statusColors = {
    PLACED: 'bg-blue-500/20 text-blue-300 border-blue-500/30',
    CONFIRMED: 'bg-indigo-500/20 text-indigo-300 border-indigo-500/30',
    PREPARING: 'bg-amber-500/20 text-amber-300 border-amber-500/30',
    READY_FOR_PICKUP: 'bg-purple-500/20 text-purple-300 border-purple-500/30',
    OUT_FOR_DELIVERY: 'bg-teal-500/20 text-teal-300 border-teal-500/30',
    DELIVERED: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
    CANCELLED: 'bg-red-500/20 text-red-300 border-red-500/30',
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-16 text-xs">
      {/* Header */}
      <div className="bg-slate-800/80 p-5 rounded-3xl border border-slate-700/80 flex flex-wrap justify-between items-center gap-4 shadow-xl">
        <div>
          <h2 className="text-lg font-black text-white">My Orders & Return Center</h2>
          <p className="text-slate-400 text-xs">Track order status, manage fulfillment, or request 7-day returns</p>
        </div>

        <div className="flex gap-1.5 bg-slate-900 p-1.5 rounded-2xl border border-slate-700/80 font-bold">
          <button
            onClick={() => setActiveTab('orders')}
            className={`px-4 py-2 rounded-xl transition ${
              activeTab === 'orders'
                ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 font-black shadow-md glow-emerald'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Orders ({orders.length})
          </button>
          <button
            onClick={() => setActiveTab('returns')}
            className={`px-4 py-2 rounded-xl transition ${
              activeTab === 'returns'
                ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 font-black shadow-md glow-emerald'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Returns & Exchanges ({returns.length})
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-400 font-bold">Loading history...</div>
      ) : error ? (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-2xl text-center">{error}</div>
      ) : activeTab === 'orders' ? (
        orders.length === 0 ? (
          <div className="bg-slate-800/40 rounded-3xl p-16 text-center border border-slate-700/60 text-slate-400 space-y-2">
            <Package className="w-12 h-12 mx-auto text-slate-600 stroke-1" />
            <p className="font-bold text-white">No orders placed yet</p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => {
              const isDelivered = order.status === 'DELIVERED';
              const canCancel = order.status === 'PLACED' || order.status === 'CONFIRMED';

              return (
                <div key={order.id} className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 space-y-4 shadow-xl">
                  <div className="flex flex-wrap justify-between items-center pb-3 border-b border-slate-700/60 gap-2">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-black text-sm text-white">Order #{order.id}</span>
                        <span className={`px-2.5 py-0.5 rounded-full font-bold border text-[10px] ${statusColors[order.status]}`}>
                          {order.status}
                        </span>
                        <span className="bg-slate-900 text-slate-300 border border-slate-700 px-2.5 py-0.5 rounded-full font-bold uppercase text-[10px]">
                          {order.fulfillmentType === 'HOME_DELIVERY' ? (
                            <span className="flex items-center gap-1">
                              <Truck className="w-3 h-3 text-emerald-400" /> Home Delivery
                            </span>
                          ) : (
                            <span className="flex items-center gap-1">
                              <Store className="w-3 h-3 text-lime-400" /> Store Pickup
                            </span>
                          )}
                        </span>
                      </div>
                      <div className="text-slate-400 text-[11px] mt-1 flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        Placed on {new Date(order.createdAt).toLocaleString()}
                      </div>
                    </div>

                    <div className="text-right">
                      <div className="text-lg font-black text-emerald-400">₹{order.totalAmount}</div>
                      <div className="text-slate-400 text-[11px]">{order.itemCount} Items</div>
                    </div>
                  </div>

                  {order.fulfillmentType === 'HOME_DELIVERY' && order.deliveryStreet && (
                    <div className="bg-slate-900/60 p-3.5 rounded-2xl text-slate-300 border border-slate-700/60">
                      <strong className="text-emerald-400">Delivery Address:</strong> {order.deliveryStreet}, {order.deliveryCity}, {order.deliveryState} - {order.deliveryPincode}
                    </div>
                  )}

                  {order.fulfillmentType === 'STORE_PICKUP' && order.pickupSlot && (
                    <div className="bg-slate-900/60 p-3.5 rounded-2xl text-slate-300 border border-slate-700/60">
                      <strong className="text-lime-400">Pickup Slot:</strong> {new Date(order.pickupSlot).toLocaleString()}
                    </div>
                  )}

                  <div className="space-y-2">
                    <div className="font-bold text-slate-300">Order Items:</div>
                    <div className="divide-y divide-slate-700/60">
                      {order.items.map((item) => (
                        <div key={item.id} className="py-2.5 flex justify-between items-center">
                          <div>
                            <span className="font-bold text-white">{item.productName}</span>
                            <span className="text-slate-400 ml-2">({item.quantity} {item.productUnit})</span>
                            {item.returned && (
                              <span className="ml-2 bg-amber-500/20 text-amber-300 border border-amber-500/30 px-2 py-0.5 rounded font-bold text-[10px]">
                                Returned
                              </span>
                            )}
                          </div>
                          <div className="font-black text-slate-200">₹{item.totalPrice}</div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="pt-3 border-t border-slate-700/60 flex justify-between items-center">
                    <div>
                      {isDelivered && (
                        <span className="text-emerald-400 font-bold text-[11px] flex items-center gap-1">
                          <CheckCircle2 className="w-4 h-4" />
                          Delivered on {new Date(order.deliveredAt).toLocaleDateString()}
                        </span>
                      )}
                    </div>

                    <div className="flex gap-2">
                      {canCancel && (
                        <button
                          onClick={() => handleCancelOrder(order.id)}
                          className="px-4 py-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 font-bold rounded-xl border border-red-500/30 transition"
                        >
                          Cancel Order
                        </button>
                      )}

                      {isDelivered && (
                        <button
                          onClick={() => setSelectedOrderForReturn(order)}
                          className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black rounded-xl shadow-lg transition flex items-center gap-1.5"
                        >
                          <RotateCcw className="w-3.5 h-3.5" />
                          Request Return / Exchange
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )
      ) : (
        /* Returns List */
        <div className="space-y-3">
          {returns.map((req) => (
            <div key={req.id} className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-5 space-y-3 shadow-xl">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <span className="font-black text-white">Request #{req.id} (Order #{req.orderId})</span>
                  <span className="bg-amber-500/20 text-amber-300 border border-amber-500/30 px-2.5 py-0.5 rounded-full font-bold">
                    {req.status}
                  </span>
                  <span className="bg-slate-900 text-slate-300 px-2.5 py-0.5 rounded-full font-bold uppercase">
                    {req.type}
                  </span>
                </div>
                <span className="text-slate-400">{new Date(req.requestedAt).toLocaleDateString()}</span>
              </div>

              <div className="bg-slate-900/60 p-3 rounded-2xl border border-slate-700/60">
                <div><strong className="text-emerald-400">Item:</strong> {req.productName} ({req.quantity})</div>
                <div><strong className="text-slate-300">Reason:</strong> {req.reason}</div>
                {req.targetProductName && <div><strong className="text-lime-400">Exchange Target:</strong> {req.targetProductName}</div>}
              </div>

              {req.staffNote && (
                <div className="bg-amber-500/10 border border-amber-500/30 p-3 rounded-2xl text-amber-300">
                  <strong>Staff Note:</strong> {req.staffNote}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      <ReturnModal
        isOpen={!!selectedOrderForReturn}
        onClose={() => setSelectedOrderForReturn(null)}
        order={selectedOrderForReturn}
        products={products}
        onRequestSubmitted={() => fetchOrdersAndReturns()}
      />
    </div>
  );
};
