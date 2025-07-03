import { BACKEND_URL } from "./api-config";

export async function searchSimple(query: string, page = 0, size = 10) {
  const url = `${BACKEND_URL}/api/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Erreur lors de la recherche simple");
  return res.json();
}

export async function searchInFields(query: string, fields: string[], page = 0, size = 10) {
  const url = `${BACKEND_URL}/api/search/fields?query=${encodeURIComponent(query)}&fields=${fields.map(encodeURIComponent).join(",")}&page=${page}&size=${size}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Erreur lors de la recherche dans les champs");
  return res.json();
}

export async function searchAdvanced(body: any) {
  const url = `${BACKEND_URL}/api/search/advanced`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error("Erreur lors de la recherche avancée");
  return res.json();
}

export async function searchSimilarContent(text: string, page = 0, size = 10) {
  const url = `${BACKEND_URL}/api/search/similar-content?page=${page}&size=${size}`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(text),
  });
  if (!res.ok) throw new Error("Erreur lors de la recherche de contenu similaire");
  return res.json();
}

export async function searchFullText(query: string, fields: string[], matchType = "multi_match", page = 0, size = 10) {
  const url = `${BACKEND_URL}/api/search/full-text?query=${encodeURIComponent(query)}&fields=${fields.map(encodeURIComponent).join(",")}&matchType=${matchType}&page=${page}&size=${size}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Erreur lors de la recherche full-text");
  return res.json();
}

export async function searchTerm(field: string, value: string, type = "term", page = 0, size = 10) {
  const url = `${BACKEND_URL}/api/search/term?field=${encodeURIComponent(field)}&value=${encodeURIComponent(value)}&type=${type}&page=${page}&size=${size}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Erreur lors de la recherche par terme");
  return res.json();
}
