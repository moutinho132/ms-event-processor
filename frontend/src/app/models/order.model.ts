export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  PROCESSED = 'PROCESSED',
  CANCELLED = 'CANCELLED',
  FAILED = 'FAILED'
}

export enum OrderEventType {
  CREATED = 'CREATED',
  UPDATED = 'UPDATED',
  CANCELLED = 'CANCELLED'
}

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface ShippingAddress {
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

export interface Order {
  id: number;
  orderId: string;
  customerId: string;
  status: OrderStatus;
  totalAmount: number;
  currency: string;
  items: OrderItem[];
  shippingAddress?: ShippingAddress;
  processedAt?: Date;
  cancelledAt?: Date;
  createdAt: Date;
  updatedAt: Date;
  version: number;
}

export interface OrderAudit {
  id: number;
  orderId: string;
  eventType: OrderEventType;
  status: OrderStatus;
  processedAt: Date;
  correlationId: string;
  source: string;
  notes?: string;
}

export interface ApiResponse<T> {
  status: string;
  count?: number;
  message?: string;
  orderId?: string;
  order?: Order;
  orders?: T[];
  auditLog?: OrderAudit[];
  auditCount?: number;
  processingTimeMs?: number;
}
