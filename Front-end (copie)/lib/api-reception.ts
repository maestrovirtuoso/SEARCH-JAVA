// src: lib/api-reception.ts
// Service pour communiquer avec l'API de réception du backend Spring Boot

import { BACKEND_URL } from "./api-config";

export async function sendToApiReception(url: string, payload: any): Promise<any> {
  const response = await fetch(`${BACKEND_URL}/api/reception?url=${encodeURIComponent(url)}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la communication avec le backend');
  }
  return response.json();
}
