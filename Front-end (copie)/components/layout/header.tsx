'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';

export function Header() {
  const pathname = usePathname();
  
  return (
    <header className="w-full py-4 px-4 bg-[#f5faff] border-b border-[#e0eeff]">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <Link href="/" className="flex items-center space-x-2 text-blue-600 hover:text-blue-700 transition-colors">
          <Home className="h-5 w-5" />
          <span className="text-xl font-medium">Accueil</span>
        </Link>
        
        <div className="flex items-center space-x-2">
          {pathname !== '/connexion' && (
            <Link href="/connexion">
             
            </Link>
          )}
          
          {pathname !== '/inscription' && (
            <Link href="/inscription">
             
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}