'use client';

import { useState } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';

const loginSchema = z.object({
  email: z.string().email({
    message: 'Veuillez fournir une adresse email valide.',
  }),
  password: z.string().min(1, {
    message: 'Veuillez entrer votre mot de passe.',
  }),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginForm() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });
  
  function onSubmit(data: LoginFormValues) {
    setIsSubmitting(true);
    
    // In a real app, you would submit the form data to your API
    console.log(data);
    
    // Simulate API call
    setTimeout(() => {
      setIsSubmitting(false);
    }, 1000);
  }
  
  return (
    <Card className="w-full max-w-md mx-auto shadow-md">
      <CardHeader className="bg-[#e6f3ff] rounded-t-lg">
        <CardTitle className="text-2xl text-blue-700">Connexion</CardTitle>
        <CardDescription className="text-blue-600">Connectez-vous à votre compte</CardDescription>
      </CardHeader>
      <CardContent className="p-6 pt-6">
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="votre@email.com"
              {...form.register('email')}
              className="border-gray-300 focus:border-blue-500"
            />
            {form.formState.errors.email && (
              <p className="text-sm text-red-500">{form.formState.errors.email.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Mot de passe</Label>
            <Input
              id="password"
              type="password"
              placeholder="********"
              {...form.register('password')}
              className="border-gray-300 focus:border-blue-500"
            />
            {form.formState.errors.password && (
              <p className="text-sm text-red-500">{form.formState.errors.password.message}</p>
            )}
          </div>

          <Button
            type="submit"
            className="w-full bg-blue-500 hover:bg-blue-600 transition-colors"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Connexion en cours...' : 'Se connecter'}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}