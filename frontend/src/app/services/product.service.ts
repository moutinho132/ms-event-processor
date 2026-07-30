import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductDto, TopProduct } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = '/api/v1/products';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los productos activos
   */
  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  /**
   * Obtiene un producto por ID
   */
  getById(productId: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${productId}`);
  }

  /**
   * Obtiene productos por categoría
   */
  getByCategory(category: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/category/${category}`);
  }

  /**
   * Crea un nuevo producto
   */
  create(product: ProductDto): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  /**
   * Crea un producto con imagen
   */
  createWithImage(product: ProductDto, image: File): Observable<Product> {
    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }));
    if (image) {
      formData.append('image', image);
    }
    return this.http.post<Product>(`${this.apiUrl}/with-image`, formData);
  }

  /**
   * Actualiza un producto
   */
  update(productId: string, product: ProductDto): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${productId}`, product);
  }

  /**
   * Actualiza la imagen de un producto
   */
  updateImage(productId: string, image: File): Observable<Product> {
    const formData = new FormData();
    formData.append('image', image);
    return this.http.post<Product>(`${this.apiUrl}/${productId}/image`, formData);
  }

  /**
   * Elimina un producto
   */
  delete(productId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${productId}`);
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
}
