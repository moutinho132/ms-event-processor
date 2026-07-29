import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { NotificationService } from '../../services/notification.service';
import { Order, OrderAudit, OrderStatus, OrderItem } from '../../models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  auditLog: OrderAudit[] = [];
  loading = false;
  error: string | null = null;
  orderId: string = '';

  // Modal de cancelación
  showCancelModal = false;
  cancelReason = '';
  cancelling = false;

  // Modal de agregar items
  showAddItemModal = false;
  newItem: OrderItem = {
    productId: '',
    productName: '',
    quantity: 1,
    unitPrice: 0
  };
  addingItem = false;

  // Transiciones válidas
  validTransitions: OrderStatus[] = [];
  transitioningStatus = false;

  // Status options
  statusOptions = Object.values(OrderStatus);

  // Notificaciones
  notificationsEnabled = false;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    public notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('id') || '';
    if (this.orderId) {
      this.loadOrderDetails();
    }
    this.checkNotifications();
  }

  async checkNotifications(): Promise<void> {
    this.notificationsEnabled = this.notificationService.isSupported();
    if (this.notificationsEnabled) {
      const permission = this.notificationService.checkPermission();
      this.notificationsEnabled = permission === 'granted';
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
        
        // Cargar transiciones válidas
        if (this.order) {
          this.loadValidTransitions();
        }
      },
      error: (err) => {
        this.error = 'Error al cargar los detalles de la orden';
        this.loading = false;
        console.error('Error loading order details:', err);
      }
    });
  }

  loadValidTransitions(): void {
    this.orderService.getValidTransitions(this.orderId).subscribe({
      next: (response) => {
        this.validTransitions = response.validTransitions || [];
      },
      error: (err) => {
        console.error('Error loading valid transitions:', err);
      }
    });
  }

  // ==================== MODAL DE CANCELACIÓN ====================

  openCancelModal(): void {
    this.showCancelModal = true;
    this.cancelReason = '';
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelReason = '';
  }

  confirmCancel(): void {
    if (!this.order) return;
    
    this.cancelling = true;
    
    this.orderService.cancelOrder(this.order.orderId, this.cancelReason).subscribe({
      next: (response) => {
        this.order = response.order;
        this.showCancelModal = false;
        this.cancelling = false;
        this.loadOrderDetails();
        
        // Notificación push
        if (this.notificationsEnabled) {
          this.notificationService.notifyOrderCancelled(this.order!.orderId);
        }
        
        // Notificación de éxito
        this.notificationService.notifySuccess('Orden cancelada y evento enviado a Kafka');
      },
      error: (err) => {
        this.error = 'Error al cancelar la orden';
        this.cancelling = false;
        console.error('Error cancelling order:', err);
      }
    });
  }

  // ==================== CAMBIO DE ESTADO ====================

  canTransitionTo(status: OrderStatus): boolean {
    return this.validTransitions.includes(status);
  }

  transitionToStatus(status: OrderStatus): void {
    if (!this.canTransitionTo(status) || !this.order) return;
    
    this.transitioningStatus = true;
    
    this.orderService.updateOrderStatus(this.order.orderId, status).subscribe({
      next: (response) => {
        this.order = response.order;
        this.transitioningStatus = false;
        this.loadOrderDetails();
        
        // Notificación push
        if (this.notificationsEnabled) {
          this.notificationService.notifyOrderUpdated(this.order!.orderId, status);
        }
        
        // Si completó, mostrar mensaje especial
        if (status === OrderStatus.PROCESSED && response.snsNotification) {
          this.notificationService.notifySuccess('¡Orden completada! Email enviado al cliente via SNS');
        } else {
          this.notificationService.notifySuccess(`Estado actualizado a ${this.getStatusLabel(status)}`);
        }
      },
      error: (err) => {
        this.error = 'Error al actualizar el estado';
        this.transitioningStatus = false;
        console.error('Error updating status:', err);
      }
    });
  }

  // ==================== AGREGAR ITEMS ====================

  openAddItemModal(): void {
    this.showAddItemModal = true;
    this.newItem = {
      productId: '',
      productName: '',
      quantity: 1,
      unitPrice: 0
    };
  }

  closeAddItemModal(): void {
    this.showAddItemModal = false;
  }

  addItem(): void {
    if (!this.newItem.productName || this.newItem.quantity <= 0 || this.newItem.unitPrice <= 0) {
      return;
    }

    // Generar productId si no existe
    if (!this.newItem.productId) {
      this.newItem.productId = this.generateUUID();
    }

    this.addingItem = true;

    this.orderService.addItemsToOrder(this.orderId, [this.newItem]).subscribe({
      next: (response) => {
        this.order = response.order || null;
        this.showAddItemModal = false;
        this.addingItem = false;
        this.loadOrderDetails();
        this.notificationService.notifySuccess('Item agregado a la orden');
      },
      error: (err) => {
        this.error = 'Error al agregar item';
        this.addingItem = false;
        console.error('Error adding item:', err);
      }
    });
  }

  generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  // ==================== HELPERS ====================

  getStatusClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PENDING:
        return 'status-pending';
      case OrderStatus.CONFIRMED:
        return 'status-confirmed';
      case OrderStatus.PROCESSING:
        return 'status-processing';
      case OrderStatus.SHIPPED:
        return 'status-shipped';
      case OrderStatus.DELIVERED:
        return 'status-delivered';
      case OrderStatus.CANCELLED:
        return 'status-cancelled';
      case OrderStatus.REFUNDED:
        return 'status-refunded';
      default:
        return '';
    }
  }

  getStatusLabel(status: OrderStatus): string {
    const labels: Record<OrderStatus, string> = {
      [OrderStatus.PENDING]: 'Pendiente',
      [OrderStatus.CONFIRMED]: 'Confirmada',
      [OrderStatus.PROCESSING]: 'Procesando',
      [OrderStatus.SHIPPED]: 'Enviada',
      [OrderStatus.DELIVERED]: 'Entregada',
      [OrderStatus.CANCELLED]: 'Cancelada',
      [OrderStatus.REFUNDED]: 'Reembolsada'
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

  getTransitionButtonClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PROCESSING:
        return 'btn-warning';
      case OrderStatus.PROCESSED:
        return 'btn-success';
      case OrderStatus.CANCELLED:
        return 'btn-danger';
      case OrderStatus.FAILED:
        return 'btn-secondary';
      default:
        return 'btn-outline';
    }
  }
}
