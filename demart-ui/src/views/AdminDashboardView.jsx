import React, { useState, useEffect } from 'react';
import { Plus, Trash2, UserPlus, RefreshCw } from 'lucide-react';
import apiClient from '../api/client';

export const AdminDashboardView = () => {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [activeSubTab, setActiveSubTab] = useState('products'); // 'categories' | 'products' | 'staff'

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Category Form
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [catName, setCatName] = useState('');
  const [catDesc, setCatDesc] = useState('');
  const [catImage, setCatImage] = useState('');

  // Product Form
  const [showProductModal, setShowProductModal] = useState(false);
  const [prodName, setProdName] = useState('');
  const [prodDesc, setProdDesc] = useState('');
  const [prodCategoryId, setProdCategoryId] = useState('');
  const [prodUnit, setProdUnit] = useState('KG');
  const [prodMrp, setProdMrp] = useState(100);
  const [prodSelling, setProdSelling] = useState(85);
  const [prodStock, setProdStock] = useState(50);
  const [prodImage, setProdImage] = useState('');

  // Staff Creation
  const [staffName, setStaffName] = useState('');
  const [staffEmail, setStaffEmail] = useState('');
  const [staffPassword, setStaffPassword] = useState('');
  const [staffPhone, setStaffPhone] = useState('');
  const [staffRole, setStaffRole] = useState('STAFF');

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    setLoading(true);
    setError('');
    try {
      const [catRes, prodRes] = await Promise.all([
        apiClient.get('/api/v1/admin/categories'),
        apiClient.get('/api/v1/admin/products?size=100'),
      ]);
      setCategories(catRes.data || []);
      setProducts(prodRes.data?.content || []);
      if (catRes.data?.length > 0) setProdCategoryId(catRes.data[0].id);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();
    try {
      await apiClient.post('/api/v1/admin/categories', {
        name: catName,
        description: catDesc,
        imageUrl: catImage,
      });
      setShowCategoryModal(false);
      setCatName('');
      setCatDesc('');
      setCatImage('');
      fetchAdminData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleCreateProduct = async (e) => {
    e.preventDefault();
    try {
      await apiClient.post('/api/v1/admin/products', {
        name: prodName,
        description: prodDesc,
        categoryId: Number(prodCategoryId),
        unit: prodUnit,
        mrpPrice: Number(prodMrp),
        sellingPrice: Number(prodSelling),
        stockQuantity: Number(prodStock),
        minStockAlert: 10,
        imageUrl: prodImage,
      });
      setShowProductModal(false);
      setProdName('');
      setProdDesc('');
      setProdImage('');
      fetchAdminData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleCreateStaff = async (e) => {
    e.preventDefault();
    try {
      await apiClient.post(`/api/v1/auth/admin/users?role=${staffRole}`, {
        name: staffName,
        email: staffEmail,
        password: staffPassword,
        phone: staffPhone,
      });
      alert(`Account created successfully with role ${staffRole}`);
      setStaffName('');
      setStaffEmail('');
      setStaffPassword('');
      setStaffPhone('');
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Deactivate this product?')) return;
    try {
      await apiClient.delete(`/api/v1/admin/products/${id}`);
      fetchAdminData();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-16 text-xs">
      {/* Banner */}
      <div className="bg-gradient-to-r from-purple-950 via-slate-900 to-indigo-950 border border-purple-500/30 p-6 sm:p-8 rounded-3xl text-white shadow-2xl glow-indigo flex flex-wrap justify-between items-center gap-4">
        <div>
          <span className="bg-purple-500/20 text-purple-300 border border-purple-500/30 text-[10px] font-black px-3 py-1 rounded-full uppercase tracking-wider">
            Admin Governance Console
          </span>
          <h2 className="text-2xl font-black mt-2">Supermarket Management</h2>
          <p className="text-xs text-slate-300 mt-1">Manage categories, product catalog, pricing, and staff credentials.</p>
        </div>

        <button onClick={fetchAdminData} className="p-3 bg-purple-700 hover:bg-purple-600 text-white rounded-2xl transition shadow-lg flex items-center gap-2 font-black">
          <RefreshCw className="w-4 h-4" /> Refresh Data
        </button>
      </div>

      {/* Tabs */}
      <div className="bg-slate-800/80 p-2 rounded-2xl border border-slate-700/80 flex gap-2 font-bold text-xs">
        <button
          onClick={() => setActiveSubTab('products')}
          className={`flex-1 py-3 rounded-xl transition ${
            activeSubTab === 'products' ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-black shadow-md glow-indigo' : 'text-slate-400 hover:text-white'
          }`}
        >
          Product Catalog ({products.length})
        </button>
        <button
          onClick={() => setActiveSubTab('categories')}
          className={`flex-1 py-3 rounded-xl transition ${
            activeSubTab === 'categories' ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-black shadow-md glow-indigo' : 'text-slate-400 hover:text-white'
          }`}
        >
          Categories ({categories.length})
        </button>
        <button
          onClick={() => setActiveSubTab('staff')}
          className={`flex-1 py-3 rounded-xl transition flex items-center justify-center gap-2 ${
            activeSubTab === 'staff' ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-black shadow-md glow-indigo' : 'text-slate-400 hover:text-white'
          }`}
        >
          <UserPlus className="w-4 h-4" />
          Provision Staff Account
        </button>
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-400 font-bold">Loading admin data...</div>
      ) : error ? (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-2xl text-center">{error}</div>
      ) : activeSubTab === 'products' ? (
        <div className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 space-y-4 shadow-xl">
          <div className="flex justify-between items-center">
            <h3 className="font-black text-base text-white">All Products Catalog</h3>
            <button
              onClick={() => setShowProductModal(true)}
              className="px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-black rounded-xl shadow-lg glow-indigo transition flex items-center gap-2"
            >
              <Plus className="w-4 h-4" /> Add Product
            </button>
          </div>

          <div className="divide-y divide-slate-700/60">
            {products.map((p) => (
              <div key={p.id} className="py-3.5 flex justify-between items-center">
                <div>
                  <span className="font-bold text-white text-sm">{p.name}</span>
                  <span className="text-slate-400 ml-2">({p.categoryName})</span>
                  <div className="text-slate-300 text-xs mt-1">
                    MRP: ₹{p.mrpPrice} | Selling: <strong className="text-emerald-400">₹{p.sellingPrice}</strong> ({p.discountPercent}% OFF) | Stock: {p.stockQuantity} {p.unit}
                  </div>
                </div>

                <button onClick={() => handleDeleteProduct(p.id)} className="p-2 text-red-400 hover:bg-red-500/10 rounded-xl transition">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>
      ) : activeSubTab === 'categories' ? (
        <div className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 space-y-4 shadow-xl">
          <div className="flex justify-between items-center">
            <h3 className="font-black text-base text-white">Categories List</h3>
            <button
              onClick={() => setShowCategoryModal(true)}
              className="px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-black rounded-xl shadow-lg glow-indigo transition flex items-center gap-2"
            >
              <Plus className="w-4 h-4" /> Add Category
            </button>
          </div>

          <div className="divide-y divide-slate-700/60">
            {categories.map((c) => (
              <div key={c.id} className="py-3.5 flex justify-between items-center">
                <div>
                  <span className="font-bold text-white text-sm">{c.name}</span>
                  <p className="text-slate-400 text-xs">{c.description || 'No description'}</p>
                </div>
                <span className="bg-purple-500/20 text-purple-300 border border-purple-500/30 px-3 py-1 rounded-full font-bold">
                  {c.productCount} Products
                </span>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="bg-slate-800/80 rounded-3xl border border-slate-700/80 p-6 max-w-md mx-auto space-y-4 shadow-xl text-white">
          <h3 className="font-black text-base text-white">Provision Staff Account</h3>
          <form onSubmit={handleCreateStaff} className="space-y-3 text-xs">
            <div>
              <label className="block text-slate-300 font-bold mb-1">Role</label>
              <select
                value={staffRole}
                onChange={(e) => setStaffRole(e.target.value)}
                className="w-full p-3 bg-slate-900 border border-slate-700 rounded-xl text-white"
              >
                <option value="STAFF">STAFF (Store Operations)</option>
                <option value="ADMIN">ADMIN (Full Admin Access)</option>
              </select>
            </div>

            <div>
              <label className="block text-slate-300 font-bold mb-1">Full Name</label>
              <input type="text" required value={staffName} onChange={(e) => setStaffName(e.target.value)} className="w-full p-3 bg-slate-900 border border-slate-700 rounded-xl text-white" />
            </div>

            <div>
              <label className="block text-slate-300 font-bold mb-1">Email</label>
              <input type="email" required value={staffEmail} onChange={(e) => setStaffEmail(e.target.value)} className="w-full p-3 bg-slate-900 border border-slate-700 rounded-xl text-white" />
            </div>

            <div>
              <label className="block text-slate-300 font-bold mb-1">Password</label>
              <input type="password" required minLength={8} value={staffPassword} onChange={(e) => setStaffPassword(e.target.value)} className="w-full p-3 bg-slate-900 border border-slate-700 rounded-xl text-white" />
            </div>

            <div>
              <label className="block text-slate-300 font-bold mb-1">Phone</label>
              <input type="tel" required pattern="[6-9][0-9]{9}" value={staffPhone} onChange={(e) => setStaffPhone(e.target.value)} className="w-full p-3 bg-slate-900 border border-slate-700 rounded-xl text-white" />
            </div>

            <button type="submit" className="w-full py-3.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-black text-sm rounded-xl shadow-lg glow-indigo">
              Create Account
            </button>
          </form>
        </div>
      )}

      {/* Add Product Modal */}
      {showProductModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/75 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-3 text-white">
            <h3 className="font-black text-sm text-white">Add New Product</h3>
            <form onSubmit={handleCreateProduct} className="space-y-2 text-xs">
              <input type="text" placeholder="Product Name" required value={prodName} onChange={(e) => setProdName(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
              <input type="text" placeholder="Description" value={prodDesc} onChange={(e) => setProdDesc(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
              <select value={prodCategoryId} onChange={(e) => setProdCategoryId(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white">
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              <select value={prodUnit} onChange={(e) => setProdUnit(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white">
                <option value="KG">KG</option>
                <option value="GRAMS">GRAMS</option>
                <option value="LITERS">LITERS</option>
                <option value="ML">ML</option>
                <option value="PIECES">PIECES</option>
                <option value="PACK">PACK</option>
              </select>
              <div className="grid grid-cols-2 gap-2">
                <input type="number" placeholder="MRP Price" required value={prodMrp} onChange={(e) => setProdMrp(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
                <input type="number" placeholder="Selling Price" required value={prodSelling} onChange={(e) => setProdSelling(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
              </div>
              <input type="number" placeholder="Initial Stock Qty" required value={prodStock} onChange={(e) => setProdStock(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
              <input type="text" placeholder="Image URL (Optional)" value={prodImage} onChange={(e) => setProdImage(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />

              <div className="flex gap-2 pt-2">
                <button type="button" onClick={() => setShowProductModal(false)} className="flex-1 py-2.5 bg-slate-800 text-slate-300 font-bold rounded-xl">Cancel</button>
                <button type="submit" className="flex-1 py-2.5 bg-purple-600 text-white font-black rounded-xl shadow glow-indigo">Save Product</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Category Modal */}
      {showCategoryModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/75 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-sm w-full p-6 space-y-3 text-white">
            <h3 className="font-black text-sm text-white">Add New Category</h3>
            <form onSubmit={handleCreateCategory} className="space-y-2 text-xs">
              <input type="text" placeholder="Category Name" required value={catName} onChange={(e) => setCatName(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
              <input type="text" placeholder="Description" value={catDesc} onChange={(e) => setCatDesc(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />
              <input type="text" placeholder="Image URL (Optional)" value={catImage} onChange={(e) => setCatImage(e.target.value)} className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white" />

              <div className="flex gap-2 pt-2">
                <button type="button" onClick={() => setShowCategoryModal(false)} className="flex-1 py-2.5 bg-slate-800 text-slate-300 font-bold rounded-xl">Cancel</button>
                <button type="submit" className="flex-1 py-2.5 bg-purple-600 text-white font-black rounded-xl shadow glow-indigo">Save Category</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
