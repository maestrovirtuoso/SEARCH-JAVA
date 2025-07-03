// This file will contain authentication-related utility functions
// In a real application, these would interact with your backend

export interface User {
  id: string;
  name: string;
  email: string;
}

// Mock function to simulate registration
export async function registerUser(data: { 
  name: string; 
  email: string; 
  password: string; 
}): Promise<{ success: boolean; user?: User; error?: string }> {
  // This would be an API call in a real application
  try {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    return {
      success: true,
      user: {
        id: '1',
        name: data.name,
        email: data.email,
      }
    };
  } catch (error) {
    return {
      success: false,
      error: 'Une erreur est survenue lors de l\'inscription.',
    };
  }
}

// Mock function to simulate login
export async function loginUser(data: { 
  email: string; 
  password: string; 
}): Promise<{ success: boolean; user?: User; error?: string }> {
  // This would be an API call in a real application
  try {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // In a real app, you would validate credentials here
    return {
      success: true,
      user: {
        id: '1',
        name: 'Utilisateur',
        email: data.email,
      }
    };
  } catch (error) {
    return {
      success: false,
      error: 'Email ou mot de passe incorrect.',
    };
  }
}