import React, { useState } from 'react';
import { X, Lock, Mail, User, Phone, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const AuthModal = ({ isOpen, onClose }) => {
  const { login, register } = useAuth();
  const [isLoginView, setIsLoginView] = useState(true);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLoginView) {
        await login(email, password);
      } else {
        await register(name, email, password, phone);
      }
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full overflow-hidden shadow-2xl animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="bg-gradient-to-r from-emerald-600 to-teal-700 p-6 text-slate-950 text-center relative">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 text-slate-900/80 hover:text-slate-950 rounded-full hover:bg-black/10 transition"
          >
            <X className="w-5 h-5" />
          </button>
          <div className="w-12 h-12 bg-slate-950 text-emerald-400 rounded-2xl mx-auto flex items-center justify-center font-black text-2xl mb-2 shadow-lg glow-emerald">
            DM
          </div>
          <h2 className="text-xl font-black">
            {isLoginView ? 'Welcome Back!' : 'Join Mini D-Mart'}
          </h2>
          <p className="text-xs text-slate-900 font-semibold mt-1">
            {isLoginView
              ? 'Sign in to access your orders, cart, and returns'
              : 'Create an account to start ordering groceries'}
          </p>
        </div>

        {/* Toggle Pills */}
        <div className="p-1.5 bg-slate-850 border border-slate-800 flex gap-1 m-5 rounded-2xl text-xs font-bold bg-slate-950">
          <button
            onClick={() => {
              setIsLoginView(true);
              setError('');
            }}
            className={`flex-1 py-2.5 rounded-xl transition ${
              isLoginView ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 font-black shadow-md glow-emerald' : 'text-slate-400'
            }`}
          >
            Sign In
          </button>
          <button
            onClick={() => {
              setIsLoginView(false);
              setError('');
            }}
            className={`flex-1 py-2.5 rounded-xl transition ${
              !isLoginView ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-slate-950 font-black shadow-md glow-emerald' : 'text-slate-400'
            }`}
          >
            Register
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mx-6 mb-2 bg-red-500/10 text-red-400 p-3 rounded-2xl text-xs flex items-center gap-2 border border-red-500/20">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="px-6 pb-6 space-y-3.5 text-xs text-slate-300">
          {!isLoginView && (
            <div>
              <label className="block font-bold mb-1 text-slate-300">Full Name</label>
              <div className="relative">
                <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                <input
                  type="text"
                  required
                  placeholder="Eknath Katole"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full pl-10 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white focus:border-emerald-400 focus:outline-none"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block font-bold mb-1 text-slate-300">Email Address</label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                type="email"
                required
                placeholder="customer@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full pl-10 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white focus:border-emerald-400 focus:outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold mb-1 text-slate-300">Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                type="password"
                required
                minLength={8}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-10 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white focus:border-emerald-400 focus:outline-none"
              />
            </div>
          </div>

          {!isLoginView && (
            <div>
              <label className="block font-bold mb-1 text-slate-300">10-Digit Mobile Phone</label>
              <div className="relative">
                <Phone className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                <input
                  type="tel"
                  required
                  pattern="[6-9][0-9]{9}"
                  placeholder="9876543210"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full pl-10 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white focus:border-emerald-400 focus:outline-none"
                />
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-2 py-3.5 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 font-black text-xs rounded-xl shadow-lg glow-emerald transition disabled:opacity-50"
          >
            {loading
              ? 'Processing...'
              : isLoginView
              ? 'Sign In to Account'
              : 'Create Customer Account'}
          </button>
        </form>
      </div>
    </div>
  );
};
