import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { Order, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  loading = false;
  error: string | null = null;
  totalOrders = 0;

  // Stats
  pendingCount = 0;
  processedCount = 0;
  cancelledCount = 0;

  // Filtros
  statusFilter: string = '';
  customerIdFilter: string = '';

  // Status options for filter
  statusOptions = Object.values(OrderStatus);

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;

    this.orderService.checkOrders().subscribe({
      next: (response) => {
        this.orders = response.orders || [];
        this.totalOrders = response.count || 0;
        this.updateStats();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar las órdenes';
        this.loading = false;
        console.error('Error loading orders:', err);
      }
    });
  }

  updateStats(): void {
    this.pendingCount = this.orders.filter(o => o.status === OrderStatus.PENDING).length;
    this.processedCount = this.orders.filter(o => o.status === OrderStatus.PROCESSED).length;
    this.cancelledCount = this.orders.filter(o => o.status === OrderStatus.CANCELLED).length;
  }

  sendTestMessage(): void {
    this.loading = true;
    this.orderService.sendTestMessage().subscribe({
      next: (response) => {
        console.log('Test message sent:', response);
        // Recargar después de 2 segundos para dar tiempo al procesamiento
        setTimeout(() => this.loadOrders(), 2000);
      },
      error: (err) => {
        this.error = 'Error al enviar mensaje de prueba';
        this.loading = false;
        console.error('Error sending test message:', err);
      }
    });
  }

  createTestOrder(): void {
    this.loading = true;
    this.orderService.createTestOrder().subscribe({
      next: (response) => {
        console.log('Test order created:', response);
        this.loadOrders();
      },
      error: (err) => {
        this.error = 'Error al crear orden de prueba';
        this.loading = false;
        console.error('Error creating test order:', err);
      }
    });
  }

  getStatusClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PENDING:
        return 'status-pending';
      case OrderStatus.PROCESSING:
        return 'status-processing';
      case OrderStatus.PROCESSED:
        return 'status-processed';
      case OrderStatus.CANCELLED:
        return 'status-cancelled';
      case OrderStatus.FAILED:
        return 'status-failed';
      default:
        return '';
    }
  }

  getStatusLabel(status: OrderStatus): string {
    const labels: Record<OrderStatus, string> = {
      [OrderStatus.PENDING]: 'Pendiente',
      [OrderStatus.PROCESSING]: 'Procesando',
      [OrderStatus.PROCESSED]: 'Procesado',
      [OrderStatus.CANCELLED]: 'Cancelado',
      [OrderStatus.FAILED]: 'Fallido'
    };
    return labels[status] || status;
  }

  formatDate(date: Date | string): string {
    if (!date) return '-';
    return new Date(date).toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCurrency(amount: number, currency: string): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(amount);
  }
}
