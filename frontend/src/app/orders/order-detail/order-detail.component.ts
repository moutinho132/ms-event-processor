import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { Order, OrderAudit, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  auditLog: OrderAudit[] = [];
  loading = false;
  error: string | null = null;
  orderId: string = '';

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('id') || '';
    if (this.orderId) {
      this.loadOrderDetails();
    }
  }

  loadOrderDetails(): void {
    this.loading = true;
    this.error = null;

    this.orderService.checkOrderWithAudit(this.orderId).subscribe({
      next: (response) => {
        this.order = response.order || null;
        this.auditLog = response.auditLog || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los detalles de la orden';
        this.loading = false;
        console.error('Error loading order details:', err);
      }
    });
  }

  cancelOrder(): void {
    if (!this.order || this.order.status === OrderStatus.CANCELLED) return;
    
    if (confirm('¿Estás seguro de que deseas cancelar esta orden?')) {
      this.orderService.cancelOrder(this.order.orderId).subscribe({
        next: (updatedOrder) => {
          this.order = updatedOrder;
          this.loadOrderDetails();
        },
        error: (err) => {
          this.error = 'Error al cancelar la orden';
          console.error('Error cancelling order:', err);
        }
      });
    }
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

  formatDate(date: Date | string | undefined): string {
    if (!date) return '-';
    return new Date(date).toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  formatCurrency(amount: number | undefined, currency: string | undefined): string {
    if (amount === undefined) return '-';
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(amount);
  }

  getEventTypeLabel(eventType: string): string {
    const labels: Record<string, string> = {
      'CREATED': 'Creado',
      'UPDATED': 'Actualizado',
      'CANCELLED': 'Cancelado'
    };
    return labels[eventType] || eventType;
  }
}
