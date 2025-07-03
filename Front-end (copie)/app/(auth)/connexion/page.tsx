import { LoginForm } from '@/components/auth/login-form';
import { Header } from '@/components/layout/header';
import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Connexion',
  description: 'Connectez-vous à votre compte',
};

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-[#f5faff]">
      <Header />
      <main className="container mx-auto px-4 py-16 flex flex-col items-center">
        <LoginForm />
      </main>
    </div>
  );
}