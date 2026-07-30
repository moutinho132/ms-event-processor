import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../services/notification.service';
import { Product, TopProduct } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  topProducts: TopProduct[] = [];
  lowStockProducts: Product[] = [];
  loading = false;
  showTopSelling = false;
  showLowStock = false;
  
  // Filter
  selectedCategory: string | null = null;
  categories: string[] = ['Electrónica', 'Ropa', 'Hogar', 'Deportes', 'Otros'];

  constructor(
    private productService: ProductService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    
    this.productService.getAll().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.notificationService.showError('Error al cargar productos');
        this.loading = false;
      }
    });
  }

  loadTopSelling(): void {
    this.showTopSelling = !this.showTopSelling;
    if (this.showTopSelling && this.topProducts.length === 0) {
      this.productService.getTopSelling(10, 30).subscribe({
        next: (products) => this.topProducts = products,
        error: (error) => console.error('Error loading top products:', error)
      });
    }
  }

  loadLowStock(): void {
    this.showLowStock = !this.showLowStock;
    if (this.showLowStock && this.lowStockProducts.length === 0) {
      this.productService.getLowStock(10).subscribe({
        next: (products) => this.lowStockProducts = products,
        error: (error) => console.error('Error loading low stock:', error)
      });
    }
  }

  filterByCategory(category: string | null): void {
    this.selectedCategory = category;
    
    if (category) {
      this.loading = true;
      this.productService.getByCategory(category).subscribe({
        next: (products) => {
          this.products = products;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error filtering products:', error);
          this.notificationService.showError('Error al filtrar productos');
          this.loading = false;
        }
      });
    } else {
      this.loadProducts();
    }
  }

  editProduct(productId: string): void {
    this.router.navigate(['/products/edit', productId]);
  }

  createProduct(): void {
    this.router.navigate(['/products/new']);
  }

  deleteProduct(productId: string): void {
    if (confirm('¿Está seguro de eliminar este producto?')) {
      this.productService.delete(productId).subscribe({
        next: () => {
          this.notificationService.showSuccess('Producto eliminado');
          this.loadProducts();
        },
        error: (error) => {
          console.error('Error deleting product:', error);
          this.notificationService.showError('Error al eliminar producto');
        }
      });
    }
  }

  getStockClass(stock: number): string {
    if (stock === 0) return 'text-danger fw-bold';
    if (stock < 10) return 'text-warning fw-bold';
    return 'text-success';
  }
}
