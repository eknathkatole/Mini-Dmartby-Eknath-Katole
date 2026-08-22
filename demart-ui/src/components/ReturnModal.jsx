import React, { useState } from 'react';
import { X, RotateCcw, RefreshCw, AlertCircle } from 'lucide-react';
import apiClient from '../api/client';

export const ReturnModal = ({ isOpen, onClose, order, products, onRequestSubmitted }) => {
  const [selectedItemId, setSelectedItemId] = useState('');
  const [type, setType] = useState('RETURN');
  const [reason, setReason] = useState('');
  const [targetProductId, setTargetProductId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen || !order) return null;

  const eligibleItems = order.items.filter((item) => !item.returned);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedItemId) {
      setError('Please select an order item to return or exchange');
      return;
    }
    if (reason.trim().length < 10) {
      setError('Please provide a detailed reason (at least 10 characters)');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const payload = {
        orderItemId: Number(selectedItemId),
        type,
        reason,
      };

      if (type === 'EXCHANGE' && targetProductId) {
        payload.targetProductId = Number(targetProductId);
      }

      const res = await apiClient.post(`/api/v1/orders/${order.id}/returns`, payload);
      onClose();
      onRequestSubmitted(res.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="bg-amber-700 p-5 text-white flex justify-between items-center">
          <div className="flex items-center gap-2">
            <RotateCcw className="w-5 h-5" />
            <h3 className="font-bold text-base">Request Return / Exchange</h3>
          </div>
          <button onClick={onClose} className="p-1 hover:bg-amber-600 rounded-full transition">
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="m-4 bg-red-50 text-red-700 p-3 rounded-xl text-xs flex items-center gap-2 border border-red-200">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          <div>
            <label className="block font-bold text-gray-700 mb-1">
              Select Item to Return / Exchange (Order #{order.id})
            </label>
            <select
              required
              value={selectedItemId}
              onChange={(e) => setSelectedItemId(e.target.value)}
              className="w-full p-2.5 border border-gray-300 rounded-xl bg-white focus:border-amber-600 focus:outline-none"
            >
              <option value="">-- Choose an Item --</option>
              {eligibleItems.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.productName} ({item.quantity} {item.productUnit}) - ₹{item.totalPrice}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-bold text-gray-700 mb-1">Request Type</label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setType('RETURN')}
                className={`py-2 rounded-xl font-bold flex items-center justify-center gap-1 border transition ${
                  type === 'RETURN'
                    ? 'bg-amber-700 text-white border-amber-700'
                    : 'bg-white text-gray-700 border-gray-300'
                }`}
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Return & Refund
              </button>
              <button
                type="button"
                onClick={() => setType('EXCHANGE')}
                className={`py-2 rounded-xl font-bold flex items-center justify-center gap-1 border transition ${
                  type === 'EXCHANGE'
                    ? 'bg-amber-700 text-white border-amber-700'
                    : 'bg-white text-gray-700 border-gray-300'
                }`}
              >
                <RefreshCw className="w-3.5 h-3.5" />
                Product Exchange
              </button>
            </div>
          </div>

          {type === 'EXCHANGE' && (
            <div>
              <label className="block font-bold text-gray-700 mb-1">
                Select Target Exchange Product (Optional — leave blank for replacement)
              </label>
              <select
                value={targetProductId}
                onChange={(e) => setTargetProductId(e.target.value)}
                className="w-full p-2.5 border border-gray-300 rounded-xl bg-white focus:border-amber-600 focus:outline-none"
              >
                <option value="">-- Same Product Replacement --</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} (₹{p.sellingPrice})
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="block font-bold text-gray-700 mb-1">Reason for Request</label>
            <textarea
              required
              minLength={10}
              rows={3}
              placeholder="Describe the issue (e.g. Expired product, damaged seal, wrong item delivered)..."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="w-full p-2.5 border border-gray-300 rounded-xl focus:border-amber-600 focus:outline-none"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-amber-700 hover:bg-amber-800 text-white font-bold text-xs rounded-xl shadow transition disabled:opacity-50"
          >
            {loading ? 'Submitting Request...' : 'Submit Request for Staff Review'}
          </button>
        </form>
      </div>
    </div>
  );
};
