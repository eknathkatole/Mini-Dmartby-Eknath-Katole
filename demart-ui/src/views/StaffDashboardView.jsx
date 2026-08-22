import React, { useState, useEffect } from 'react';
import { Store, AlertTriangle, RefreshCw } from 'lucide-react';
import apiClient from '../api/client';

export const StaffDashboardView = () => {
  const [orders, setOrders] = useState([]);
  const [returns, setReturns] = useState([]);
  const [lowStockProducts, setLowStockProducts] = useState([]);
  const [activeTab, setActiveTab] = useState('orders'); // 'orders' | 'returns' | 'inventory'

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Stock update state
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [stockQty, setStockQty] = useState(10);
  const [stockOp, setStockOp] = useState('ADD');

  // Return process state
  const [selectedReturn, setSelectedReturn] = useState(null);
  const [staffNote, setStaffNote] = useState('');

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    setLoading(true);
    setError('');
    try {
      const [ordersRes, returnsRes, lowStockRes] = await Promise.all([
        apiClient.get('/api/v1/staff/orders?size=50'),
        apiClient.get('/api/v1/staff/returns'),
        apiClient.get('/api/v1/staff/products/low-stock'),
      ]);
      setOrders(ordersRes.data?.content || []);
      setReturns(returnsRes.data || []);
      setLowStockProducts(lowStockRes.data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      await apiClient.patch(`/api/v1/staff/orders/${orderId}/status`, { status: newStatus });
      fetchAllData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleStockUpdate = async (e) => {
    e.preventDefault();
    if (!selectedProduct) return;
    try {
      await apiClient.patch(`/api/v1/staff/products/${selectedProduct.id}/stock`, {
        quantity: Number(stockQty),
        operation: stockOp,
        reason: 'Staff manual inventory adjustment',
      });
      setSelectedProduct(null);
      fetchAllData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleProcessReturn = async (requestId, action) => {
    if (action === 'REJECT' && !staffNote.trim()) {
      alert('Please provide a staff note explaining the rejection');
      return;
    }
    try {
      await apiClient.patch(`/api/v1/staff/returns/${requestId}/process`, {
        action,
        staffNote,
      });
      setSelectedReturn(null);
      setStaffNote('');
      fetchAllData();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-16 text-xs">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-indigo-900 via-slate-900 to-purple-900 border border-indigo-500/30 p-6 sm:p-8 rounded-3xl text-white shadow-2xl glow-indigo flex flex-wrap justify-between items-center gap-4">
        <div>
          <span className="bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-wider">
            Store Fulfillment & Operations
          </span>
          <h2 className="text-2xl font-black mt-2">Staff Operations Center</h2>
          <p className="text-xs text-slate-300 mt-1">Fulfill pickup & delivery orders, restock inventory, process customer returns.</p>
        </div>

        <button
          onClick={fetchAllData}
          className="p-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-2xl transition shadow-lg flex items-center gap-2 font-black"
        >
          <RefreshCw className="w-4 h-4" />
          Refresh Data
        </button>
      </div>

      {/* Tabs */}
      <div className="bg-slate-800/80 p-2 rounded-2xl border border-slate-700/80 flex gap-2 font-bold text-xs">
        <button
          onClick={() => setActiveTab('orders')}
          className={`flex-1 py-3 rounded-xl transition ${
            activeTab === 'orders' ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-black shadow-md glow-indigo' : 'text-slate-400 hover:text-white'
          }`}
        >
          Preparation Queue ({orders.length})
        </button>
        <button
          onClick={() => setActiveTab('returns')}
          className={`flex-1 py-3 rounded-xl transition ${
            activeTab === 'returns' ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-black shadow-md glow-indigo' : 'text-slate-400 hover:text-white'
          }`}
        >
          Return Processing ({returns.filter((r) => r.status === 'PENDING').length} Pending)
        </button>
        <button
          onClick={() => setActiveTab('inventory')}
          className={`flex-1 py-3 rounded-xl transition flex items-center justify-center gap-2 ${
            activeTab === 'inventory' ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-black shadow-md glow-indigo' : 'text-slate-400 hover:text-white'
          }`}
        >
          <AlertTriangle className="w-4 h-4 text-amber-400" />
          Low Stock Alerts ({lowStockProducts.length})
        </button>
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-400 font-bold">Loading staff data...</div>
      ) : error ? (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-2xl text-center">{error}</div>
      ) : activeTab === 'orders' ? (
        /* Order Fulfillment Queue */
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.id} className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 space-y-4 shadow-xl">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <span className="font-black text-sm text-white">Order #{order.id}</span>
                  <span className="bg-amber-500/20 text-amber-300 border border-amber-500/30 px-2.5 py-0.5 rounded-full font-bold">
                    {order.status}
                  </span>
                  <span className="bg-slate-900 text-slate-300 px-2.5 py-0.5 rounded-full font-bold uppercase text-[10px]">
                    {order.fulfillmentType}
                  </span>
                </div>
                <div className="font-black text-base text-emerald-400">₹{order.totalAmount}</div>
              </div>

              <div className="bg-slate-900/60 p-4 rounded-2xl border border-slate-700/60 text-xs grid grid-cols-2 gap-2">
                <div><strong className="text-slate-300">Customer:</strong> {order.customerName} ({order.customerEmail})</div>
                <div><strong className="text-slate-300">Placed:</strong> {new Date(order.createdAt).toLocaleString()}</div>
                {order.fulfillmentType === 'HOME_DELIVERY' && order.deliveryStreet && (
                  <div className="col-span-2"><strong className="text-emerald-400">Delivery Address:</strong> {order.deliveryStreet}, {order.deliveryCity} - {order.deliveryPincode}</div>
                )}
                {order.fulfillmentType === 'STORE_PICKUP' && order.pickupSlot && (
                  <div className="col-span-2"><strong className="text-lime-400">Pickup Slot:</strong> {new Date(order.pickupSlot).toLocaleString()}</div>
                )}
              </div>

              {/* Status Transition Action Buttons */}
              <div className="flex flex-wrap gap-2 pt-2 border-t border-slate-700/60 items-center justify-between">
                <div className="text-xs text-slate-400 font-bold">Update Status:</div>
                <div className="flex gap-2 flex-wrap">
                  {order.status === 'PLACED' && (
                    <button onClick={() => handleUpdateStatus(order.id, 'CONFIRMED')} className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white font-black rounded-xl shadow">
                      Confirm Order
                    </button>
                  )}
                  {order.status === 'CONFIRMED' && (
                    <button onClick={() => handleUpdateStatus(order.id, 'PREPARING')} className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black rounded-xl shadow">
                      Start Preparing
                    </button>
                  )}
                  {order.status === 'PREPARING' && (
                    order.fulfillmentType === 'STORE_PICKUP' ? (
                      <button onClick={() => handleUpdateStatus(order.id, 'READY_FOR_PICKUP')} className="px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white font-black rounded-xl shadow">
                        Ready for Pickup
                      </button>
                    ) : (
                      <button onClick={() => handleUpdateStatus(order.id, 'OUT_FOR_DELIVERY')} className="px-4 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 font-black rounded-xl shadow">
                        Out for Delivery
                      </button>
                    )
                  )}
                  {(order.status === 'READY_FOR_PICKUP' || order.status === 'OUT_FOR_DELIVERY') && (
                    <button onClick={() => handleUpdateStatus(order.id, 'DELIVERED')} className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black rounded-xl shadow glow-emerald">
                      Mark Delivered
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : activeTab === 'returns' ? (
        /* Return Processing List */
        <div className="space-y-4">
          {returns.map((req) => (
            <div key={req.id} className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 space-y-3 shadow-xl">
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
              </div>

              <div className="bg-slate-900/60 p-4 rounded-2xl border border-slate-700/60 text-xs">
                <div><strong className="text-emerald-400">Item:</strong> {req.productName} ({req.quantity})</div>
                <div><strong className="text-slate-300">Reason:</strong> {req.reason}</div>
                {req.targetProductName && <div><strong className="text-lime-400">Exchange Target:</strong> {req.targetProductName}</div>}
              </div>

              {req.status === 'PENDING' ? (
                <div className="space-y-3 pt-2 border-t border-slate-700/60">
                  <input
                    type="text"
                    placeholder="Staff review note (mandatory for rejection)..."
                    value={selectedReturn === req.id ? staffNote : ''}
                    onChange={(e) => {
                      setSelectedReturn(req.id);
                      setStaffNote(e.target.value);
                    }}
                    className="w-full p-3 bg-slate-900 border border-slate-700 rounded-xl text-white focus:outline-none focus:border-indigo-400"
                  />
                  <div className="flex gap-2 justify-end">
                    <button
                      onClick={() => handleProcessReturn(req.id, 'REJECT')}
                      className="px-5 py-2 bg-red-600 hover:bg-red-500 text-white font-black rounded-xl shadow"
                    >
                      Reject Request
                    </button>
                    <button
                      onClick={() => handleProcessReturn(req.id, 'APPROVE')}
                      className="px-5 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black rounded-xl shadow glow-emerald"
                    >
                      Approve & Restock
                    </button>
                  </div>
                </div>
              ) : (
                <div className="text-slate-400 italic">Processed: {req.staffNote}</div>
              )}
            </div>
          ))}
        </div>
      ) : (
        /* Inventory Alerts */
        <div className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 space-y-4 shadow-xl">
          <h3 className="font-black text-sm text-white">Low Stock Replenishment List</h3>
          <div className="divide-y divide-slate-700/60">
            {lowStockProducts.map((product) => (
              <div key={product.id} className="py-4 flex justify-between items-center">
                <div>
                  <span className="font-bold text-white text-sm">{product.name}</span>
                  <span className="text-slate-400 ml-2">({product.categoryName})</span>
                  <div className="text-amber-400 font-bold text-xs mt-1">
                    Stock: {product.stockQuantity} {product.unit} (Min Threshold: {product.minStockAlert})
                  </div>
                </div>

                <button
                  onClick={() => {
                    setSelectedProduct(product);
                    setStockQty(20);
                  }}
                  className="px-5 py-2.5 bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 font-black rounded-xl shadow-lg glow-emerald"
                >
                  Adjust Stock
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Stock Modal */}
      {selectedProduct && (
        <div className="fixed inset-0 z-50 bg-slate-950/75 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-sm w-full p-6 space-y-4 text-white">
            <h3 className="font-black text-sm text-white">Adjust Stock: {selectedProduct.name}</h3>
            <form onSubmit={handleStockUpdate} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-bold mb-1">Operation</label>
                <select
                  value={stockOp}
                  onChange={(e) => setStockOp(e.target.value)}
                  className="w-full p-3 bg-slate-800 border border-slate-700 rounded-xl text-white"
                >
                  <option value="ADD">ADD (Receive Stock)</option>
                  <option value="SUBTRACT">SUBTRACT (Write-off / Damage)</option>
                  <option value="SET">SET (Exact Reconcile)</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-bold mb-1">Quantity</label>
                <input
                  type="number"
                  min={1}
                  required
                  value={stockQty}
                  onChange={(e) => setStockQty(e.target.value)}
                  className="w-full p-3 bg-slate-800 border border-slate-700 rounded-xl text-white"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedProduct(null)}
                  className="flex-1 py-3 bg-slate-800 text-slate-300 font-bold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 py-3 bg-emerald-500 text-slate-950 font-black rounded-xl shadow glow-emerald"
                >
                  Save Stock
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
