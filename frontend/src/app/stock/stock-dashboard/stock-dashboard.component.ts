import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StockService } from '../../services/stock.service';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../services/notification.service';
import { TopProduct } from '../../models/product.model';
import { Product } from '../../models/product.model';
import { StockMovement } from '../../models/stock.model';

@Component({
  selector: 'app-stock-dashboard',
  templateUrl: './stock-dashboard.component.html',
  styleUrls: ['./stock-dashboard.component.scss']
})
export class StockDashboardComponent implements OnInit {
  topProducts: TopProduct[] = [];
  lowStockProducts: Product[] = [];
  recentMovements: StockMovement[] = [];
  loading = false;
  
  // Stats
  totalProducts = 0;
  totalLowStock = 0;
  totalSales = 0;

  constructor(
    private stockService: StockService,
    private productService: ProductService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    
    // Load all data in parallel
    this.stockService.getTopSelling(5, 30).subscribe({
      next: (products) => {
        this.topProducts = products;
        this.totalSales = products.reduce((sum, p) => sum + p.totalSold, 0);
      },
      error: (error) => console.error('Error loading top products:', error)
    });

    this.stockService.getLowStock(10).subscribe({
      next: (products) => {
        this.lowStockProducts = products;
        this.totalLowStock = products.length;
      },
      error: (error) => console.error('Error loading low stock:', error)
    });

    this.productService.getAll().subscribe({
      next: (products) => {
        this.totalProducts = products.length;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.loading = false;
      }
    });
  }

  goToMovement(): void {
    this.router.navigate(['/stock/movement']);
  }

  viewProductHistory(productId: string): void {
    this.stockService.getHistory(productId, 10).subscribe({
      next: (movements) => {
        this.recentMovements = movements;
      },
      error: (error) => {
        console.error('Error loading history:', error);
        this.notificationService.showError('Error al cargar historial');
      }
    });
  }

  getMovementIcon(type: string): string {
    switch (type) {
      case 'IN': return 'fa-arrow-down text-success';
      case 'OUT': return 'fa-arrow-up text-danger';
      case 'SALE': return 'fa-shopping-cart text-primary';
      case 'RETURN': return 'fa-undo text-warning';
      case 'ADJUSTMENT': return 'fa-balance-scale text-info';
      default: return 'fa-exchange-alt';
    }
  }

  getMovementLabel(type: string): string {
    switch (type) {
      case 'IN': return 'Entrada';
      case 'OUT': return 'Salida';
      case 'SALE': return 'Venta';
      case 'RETURN': return 'Devolución';
      case 'ADJUSTMENT': return 'Ajuste';
      default: return type;
    }
  }
}
