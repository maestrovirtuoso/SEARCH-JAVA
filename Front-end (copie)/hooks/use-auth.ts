'use client';

import { useState } from 'react';
import { User, loginUser, registerUser } from '@/lib/auth-utils';

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = async (email: string, password: string) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await loginUser({ email, password });
      
      if (result.success && result.user) {
        setUser(result.user);
        return true;
      } else {
        setError(result.error || 'Échec de la connexion');
        return false;
      }
    } catch (err) {
      setError('Une erreur est survenue');
      return false;
    } finally {
      setLoading(false);
    }
  };

  const register = async (name: string, email: string, password: string) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await registerUser({ name, email, password });
      
      if (result.success && result.user) {
        setUser(result.user);
        return true;
      } else {
        setError(result.error || 'Échec de l\'inscription');
        return false;
      }
    } catch (err) {
      setError('Une erreur est survenue');
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
  };

  return {
    user,
    loading,
    error,
    login,
    register,
    logout,
    isAuthenticated: !!user,
  };
}