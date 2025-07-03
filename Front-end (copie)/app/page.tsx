import { Header } from '@/components/layout/header';
import { SearchBar } from '@/components/home/search-bar';
import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Accueil',
  description: 'Bienvenue sur notre site',
};

export default function Home() {
  return (
    <div className="min-h-screen bg-[#f5faff]">
      <Header />
      <main className="container mx-auto px-4 flex flex-col items-center">
        <div className="max-w-4xl w-full mt-12 mb-16 text-center">
          <h1 className="text-3xl font-bold text-blue-700 mb-6">Bienvenue sur notre site</h1>
          <p className="text-lg text-gray-700 max-w-2xl mx-auto">
            Utilisez la barre de recherche ci-dessous pour trouver ce que vous cherchez, 
            ou créez un compte pour accéder à toutes nos fonctionnalités.
          </p>
        </div>
        
        <SearchBar />
      </main>
    </div>
  );
}