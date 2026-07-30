// Stock model for frontend
export enum MovementType {
  IN = 'IN',
  OUT = 'OUT',
  SALE = 'SALE',
  RETURN = 'RETURN',
  ADJUSTMENT = 'ADJUSTMENT'
}

export interface StockMovement {
  id: number;
  movementId: string;
  productId: string;
  type: MovementType;
  quantity: number;
  previousStock?: number;
  newStock?: number;
  unitPrice?: number;
  referenceId?: string;
  referenceType?: string;
  notes?: string;
  createdBy?: string;
  createdAt: string;
}

export interface StockMovementDto {
  productId: string;
  quantity: number;
  type: MovementType;
  referenceId?: string;
  notes?: string;
  userId?: string;
}
