import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { StockService } from '../../services/stock.service';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../services/notification.service';
import { Product } from '../../models/product.model';
import { MovementType } from '../../models/stock.model';

@Component({
  selector: 'app-stock-movement',
  templateUrl: './stock-movement.component.html',
  styleUrls: ['./stock-movement.component.scss']
})
export class StockMovementComponent implements OnInit {
  movementForm: FormGroup;
  products: Product[] = [];
  loading = false;
  submitting = false;
  
  movementTypes = [
    { value: MovementType.IN, label: 'Entrada', icon: 'fa-arrow-down', color: 'success' },
    { value: MovementType.OUT, label: 'Salida', icon: 'fa-arrow-up', color: 'danger' },
    { value: MovementType.ADJUSTMENT, label: 'Ajuste', icon: 'fa-balance-scale', color: 'info' }
  ];

  selectedProduct: Product | null = null;

  constructor(
    private fb: FormBuilder,
    private stockService: StockService,
    private productService: ProductService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.movementForm = this.fb.group({
      productId: ['', [Validators.required]],
      type: [MovementType.IN, [Validators.required]],
      quantity: [0, [Validators.required, Validators.min(1)]],
      referenceId: [''],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    
    // Watch for product selection
    this.movementForm.get('productId')?.valueChanges.subscribe(productId => {
      this.selectedProduct = this.products.find(p => p.productId === productId) || null;
    });
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

  onSubmit(): void {
    if (this.movementForm.invalid) {
      this.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.movementForm.value;
    
    this.stockService.registerMovement({
      productId: formValue.productId,
      quantity: formValue.quantity,
      type: formValue.type,
      referenceId: formValue.referenceId,
      notes: formValue.notes
    }).subscribe({
      next: (product) => {
        this.notificationService.showSuccess(`Stock actualizado: ${product.stock} unidades`);
        this.submitting = false;
        this.router.navigate(['/stock']);
      },
      error: (error) => {
        console.error('Error registering movement:', error);
        this.notificationService.showError(error.error?.message || 'Error al registrar movimiento');
        this.submitting = false;
      }
    });
  }

  markAllAsTouched(): void {
    Object.values(this.movementForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }

  get productId() { return this.movementForm.get('productId'); }
  get type() { return this.movementForm.get('type'); }
  get quantity() { return this.movementForm.get('quantity'); }

  cancel(): void {
    this.router.navigate(['/stock']);
  }

  getMovementTypeInfo(type: MovementType) {
    return this.movementTypes.find(t => t.value === type);
  }
}
