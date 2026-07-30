/**
 * Modelo para Local (Tienda física)
 */
export interface Local {
  localId: string;
  name: string;
  address: string;
  phone?: string;
  email?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
  latitude?: number;
  longitude?: number;
  active: boolean;
  totalWarehouses?: number;
  createdAt?: string;
}

/**
 * DTO para crear/actualizar Local
 */
export interface LocalDto {
  localId?: string;
  name: string;
  address: string;
  phone?: string;
  email?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
  latitude?: number;
  longitude?: number;
  active?: boolean;
}

/**
 * Respuesta paginada de Locales
 */
export interface LocalPageResponse {
  content: Local[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
