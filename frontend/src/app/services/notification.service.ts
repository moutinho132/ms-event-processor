import { Injectable } from '@angular/core';

export interface NotificationOptions {
  title: string;
  body: string;
  icon?: string;
  tag?: string;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private permission: NotificationPermission = 'default';

  constructor() {
    this.checkPermission();
  }

  /**
   * Verifica y solicita permisos para notificaciones
   */
  async requestPermission(): Promise<boolean> {
    if (!this.isSupported()) {
      console.warn('Las notificaciones no son soportadas en este navegador');
      return false;
    }

    if (this.permission === 'granted') {
      return true;
    }

    this.permission = await Notification.requestPermission();
    return this.permission === 'granted';
  }

  /**
   * Verifica si las notificaciones son soportadas
   */
  isSupported(): boolean {
    return 'Notification' in window;
  }

  /**
   * Verifica el permiso actual
   */
  checkPermission(): NotificationPermission {
    if (this.isSupported()) {
      this.permission = Notification.permission;
    }
    return this.permission;
  }

  /**
   * Verifica si tenemos permiso para enviar notificaciones
   */
  hasPermission(): boolean {
    return this.permission === 'granted';
  }

  /**
   * Envia una notificacion push
   */
  async notify(options: NotificationOptions): Promise<Notification | null> {
    if (!this.isSupported()) {
      console.warn('Notificaciones no soportadas');
      return null;
    }

    if (!this.hasPermission()) {
      const granted = await this.requestPermission();
      if (!granted) {
        console.warn('Permiso de notificacion denegado');
        return null;
      }
    }

    const notification = new Notification(options.title, {
      body: options.body,
      icon: options.icon || '/favicon.ico',
      tag: options.tag,
      data: options.data
    });

    notification.onclick = (event) => {
      window.focus();
      notification.close();
    };

    return notification;
  }

  /**
   * Notificacion de orden creada
   */
  notifyOrderCreated(orderId: string, customerName?: string): void {
    this.notify({
      title: 'Nueva Orden Creada',
      body: customerName 
        ? `Orden ${orderId.substring(0, 8)}... creada para ${customerName}`
        : `Orden ${orderId.substring(0, 8)}... creada exitosamente`,
      tag: `order-created-${orderId}`,
      data: { orderId, type: 'created' }
    });
  }

  /**
   * Notificacion de orden actualizada
   */
  notifyOrderUpdated(orderId: string, status: string): void {
    this.notify({
      title: 'Orden Actualizada',
      body: `Orden ${orderId.substring(0, 8)}... cambio a estado: ${status}`,
      tag: `order-updated-${orderId}`,
      data: { orderId, type: 'updated', status }
    });
  }

  /**
   * Notificacion de orden cancelada
   */
  notifyOrderCancelled(orderId: string): void {
    this.notify({
      title: 'Orden Cancelada',
      body: `Orden ${orderId.substring(0, 8)}... ha sido cancelada`,
      tag: `order-cancelled-${orderId}`,
      data: { orderId, type: 'cancelled' }
    });
  }

  /**
   * Notificacion de error
   */
  notifyError(message: string): void {
    this.notify({
      title: 'Error',
      body: message,
      tag: 'error'
    });
  }

  /**
   * Notificacion de exito
   */
  notifySuccess(message: string): void {
    this.notify({
      title: 'Exito',
      body: message,
      tag: 'success'
    });
  }
}
