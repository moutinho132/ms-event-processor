import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderAudit, ApiResponse, OrderStatus, OrderItem, CancelOrderResponse, StatusUpdateResponse } from '../models/order.model';

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

  // Cancelar orden con envío a Kafka
  cancelOrder(orderId: string, reason?: string): Observable<CancelOrderResponse> {
    const params: any = {};
    if (reason) params.reason = reason;
    return this.http.post<CancelOrderResponse>(`${this.apiUrl}/${orderId}/cancel`, {}, { params });
  }

  // Actualizar estado de la orden
  updateOrderStatus(orderId: string, status: OrderStatus): Observable<StatusUpdateResponse> {
    return this.http.post<StatusUpdateResponse>(`${this.apiUrl}/${orderId}/status?status=${status}`, {});
  }

  // Agregar items a una orden
  addItemsToOrder(orderId: string, items: OrderItem[]): Observable<ApiResponse<Order>> {
    // Backend espera el array directamente, no un objeto con items
    return this.http.post<ApiResponse<Order>>(`${this.apiUrl}/${orderId}/items`, items);
  }

  // Obtener transiciones válidas
  getValidTransitions(orderId: string): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.apiUrl}/${orderId}/valid-transitions`);
  }

  // Filtrar órdenes por status o customerId
  filterOrders(status?: string, customerId?: string): Observable<Order[]> {
    const params: any = {};
    if (status) params.status = status;
    if (customerId) params.customerId = customerId;
    return this.http.get<Order[]>(this.apiUrl, { params });
  }
}
