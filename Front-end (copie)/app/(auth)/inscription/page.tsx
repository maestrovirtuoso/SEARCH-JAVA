import { RegisterForm } from '@/components/auth/register-form';
import { Header } from '@/components/layout/header';
import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Inscription',
  description: 'Créez un nouveau compte',
};

export default function RegisterPage() {
  return (
    <div className="min-h-screen bg-[#f5faff]">
      <Header />
      <main className="container mx-auto px-4 py-16 flex flex-col items-center">
        <RegisterForm />
      </main>
    </div>
  );
}