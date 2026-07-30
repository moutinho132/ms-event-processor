/**
 * Modelo para Warehouse (Almacén)
 */
export interface Warehouse {
  warehouseId: string;
  name: string;
  code?: string;
  description?: string;
  location?: string;
  capacity?: number;
  temperatureControlled: boolean;
  minTemperature?: number;
  maxTemperature?: number;
  localId: string;
  localName?: string;
  active: boolean;
  totalProducts?: number;
  createdAt?: string;
}

/**
 * DTO para crear/actualizar Warehouse
 */
export interface WarehouseDto {
  warehouseId?: string;
  name: string;
  code?: string;
  description?: string;
  location?: string;
  capacity?: number;
  temperatureControlled?: boolean;
  minTemperature?: number;
  maxTemperature?: number;
  localId: string;
  active?: boolean;
}

/**
 * Respuesta paginada de Warehouses
 */
export interface WarehousePageResponse {
  content: Warehouse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
