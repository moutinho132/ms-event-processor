// Product model for frontend
export interface Product {
  id: number;
  productId: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  stock: number;
  category?: string;
  imageUrl?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductDto {
  productId?: string;
  name: string;
  description?: string;
  price: number;
  currency?: string;
  stock?: number;
  category?: string;
  imageUrl?: string;
  active?: boolean;
}

export interface TopProduct {
  productId: string;
  productName: string;
  category?: string;
  imageUrl?: string;
  price: number;
  totalSold: number;
  totalRevenue: number;
  currentStock: number;
  rank: number;
}
