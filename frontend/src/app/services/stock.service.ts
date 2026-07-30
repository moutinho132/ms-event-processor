import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StockMovement, StockMovementDto, MovementType } from '../models/stock.model';
import { TopProduct } from '../models/product.model';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = '/api/v1/stock';

  constructor(private http: HttpClient) {}

  /**
   * Registra un movimiento de stock
   */
  registerMovement(movement: StockMovementDto): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}/movement`, movement);
  }

  /**
   * Obtiene los productos más vendidos
   */
  getTopSelling(limit: number = 10, days: number = 30): Observable<TopProduct[]> {
    const params = new HttpParams()
      .set('limit', limit.toString())
      .set('days', days.toString());
    return this.http.get<TopProduct[]>(`${this.apiUrl}/top-selling`, { params });
  }

  /**
   * Obtiene productos con bajo stock
   */
  getLowStock(threshold: number = 10): Observable<Product[]> {
    const params = new HttpParams().set('threshold', threshold.toString());
    return this.http.get<Product[]>(`${this.apiUrl}/low-stock`, { params });
  }

  /**
   * Obtiene el historial de stock de un producto
   */
  getHistory(productId: string, limit: number = 20): Observable<StockMovement[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<StockMovement[]>(`${this.apiUrl}/history/${productId}`, { params });
  }

  /**
   * Realiza un ajuste de inventario
   */
  adjustStock(productId: string, newQuantity: number, notes: string, userId?: string): Observable<Product> {
    let params = new HttpParams()
      .set('newQuantity', newQuantity.toString())
      .set('notes', notes);
    if (userId) {
      params = params.set('userId', userId);
    }
    return this.http.post<Product>(`${this.apiUrl}/adjust/${productId}`, null, { params });
  }

  /**
   * Entrada de stock
   */
  stockIn(productId: string, quantity: number, referenceId?: string, notes?: string, userId?: string): Observable<Product> {
    let params = new HttpParams().set('quantity', quantity.toString());
    if (referenceId) params = params.set('referenceId', referenceId);
    if (notes) params = params.set('notes', notes);
    if (userId) params = params.set('userId', userId);
    return this.http.post<Product>(`${this.apiUrl}/in/${productId}`, null, { params });
  }

  /**
   * Salida de stock
   */
  stockOut(productId: string, quantity: number, referenceId?: string, notes?: string, userId?: string): Observable<Product> {
    let params = new HttpParams().set('quantity', quantity.toString());
    if (referenceId) params = params.set('referenceId', referenceId);
    if (notes) params = params.set('notes', notes);
    if (userId) params = params.set('userId', userId);
    return this.http.post<Product>(`${this.apiUrl}/out/${productId}`, null, { params });
  }
}
