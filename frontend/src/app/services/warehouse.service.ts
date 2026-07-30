import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Warehouse, WarehouseDto, WarehousePageResponse } from '../models/warehouse.model';

/**
 * Servicio para gestión de Almacenes
 */
@Injectable({
  providedIn: 'root'
})
export class WarehouseService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/warehouses';

  getAll(): Observable<Warehouse[]> {
    return this.http.get<Warehouse[]>(this.apiUrl);
  }

  getPage(page: number, size: number): Observable<WarehousePageResponse> {
    return this.http.get<WarehousePageResponse>(`${this.apiUrl}/page?page=${page}&size=${size}`);
  }

  getById(warehouseId: string): Observable<Warehouse> {
    return this.http.get<Warehouse>(`${this.apiUrl}/${warehouseId}`);
  }

  create(dto: WarehouseDto): Observable<Warehouse> {
    return this.http.post<Warehouse>(this.apiUrl, dto);
  }

  update(warehouseId: string, dto: WarehouseDto): Observable<Warehouse> {
    return this.http.put<Warehouse>(`${this.apiUrl}/${warehouseId}`, dto);
  }

  delete(warehouseId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${warehouseId}`);
  }

  activate(warehouseId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${warehouseId}/activate`, {});
  }

  deactivate(warehouseId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${warehouseId}/deactivate`, {});
  }

  searchByName(name: string): Observable<Warehouse[]> {
    return this.http.get<Warehouse[]>(`${this.apiUrl}/search?name=${encodeURIComponent(name)}`);
  }

  getByLocal(localId: string): Observable<Warehouse[]> {
    return this.http.get<Warehouse[]>(`${this.apiUrl}/local/${localId}`);
  }

  getTemperatureControlled(): Observable<Warehouse[]> {
    return this.http.get<Warehouse[]>(`${this.apiUrl}/temperature-controlled`);
  }
}
