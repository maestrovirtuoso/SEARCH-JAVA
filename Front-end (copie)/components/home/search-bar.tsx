'use client';

import { useState } from 'react';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { searchSimple } from '@/lib/api-search';

export function SearchBar() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await searchSimple(query);
      setResults(data?.results || []);
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la recherche');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="w-full max-w-7xl mx-auto mt-5">
      <div className="flex rounded-full overflow-hidden shadow-lg border border-gray-300">
        <Input
          type="text"
          placeholder="Recherche"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="w-full py-4 pl-6 pr-10 text-lg text-gray-900 bg-white border-none focus:outline-none placeholder-gray-500"
        />
        <Button 
          type="submit" 
          className="bg-blue-500 hover:bg-blue-600 rounded-r-full px-6"
          disabled={loading}
        >
          <Search className="h-6 w-6 mr-2" />
          {loading ? 'Recherche...' : 'Rechercher'}
        </Button>
      </div>
      {error && <div className="text-red-500 mt-2">{error}</div>}
      <div className="mt-4">
        {results.length === 0 && !loading && !error && (
          <div className="text-gray-500 text-center">Aucun résultat trouvé pour votre recherche.</div>
        )}
        {results.length > 0 && (
          <div className="bg-white rounded-lg shadow-sm divide-y divide-gray-200">
            {results.map((item, idx) => (
              <div key={idx} className="result-item p-4 hover:bg-gray-50 transition">
                <div className="text-sm text-gray-500 mb-1">{item.document?.category}</div>
                <h3 className="text-xl text-blue-700 font-medium mb-1">
                  <a href={item.document?.url} className="hover:underline">{item.document?.title}</a>
                </h3>
                <div className="text-gray-600">{item.document?.content}</div>
                <div className="mt-1 text-sm text-green-700">{item.document?.url}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </form>
  );
}
