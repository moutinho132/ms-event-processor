import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderAudit, ApiResponse } from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = '/api/v1/orders';

  constructor(private http: HttpClient) {}

  // Listar todas las órdenes
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  // Verificar órdenes en BD (equivalente a check-orders.sh)
  checkOrders(): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.apiUrl}/database/check`);
  }

  // Obtener orden por ID
  getOrderById(orderId: string): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${orderId}`);
  }

  // Verificar orden específica con auditoría
  checkOrderWithAudit(orderId: string): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.apiUrl}/database/check/${orderId}`);
  }

  // Enviar mensaje de prueba a Kafka (equivalente a test-producer.sh)
  sendTestMessage(): Observable<ApiResponse<Order>> {
    return this.http.post<ApiResponse<Order>>(`${this.apiUrl}/kafka/send-test`, {});
  }

  // Crear orden de prueba rápidamente
  createTestOrder(): Observable<ApiResponse<Order>> {
    return this.http.post<ApiResponse<Order>>(`${this.apiUrl}/create-test`, {});
  }

  // Cancelar orden
  cancelOrder(orderId: string): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/${orderId}/cancel`, {});
  }

  // Filtrar órdenes por status o customerId
  filterOrders(status?: string, customerId?: string): Observable<Order[]> {
    const params: any = {};
    if (status) params.status = status;
    if (customerId) params.customerId = customerId;
    return this.http.get<Order[]>(this.apiUrl, { params });
  }
}
