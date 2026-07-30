import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../services/notification.service';
import { ProductDto } from '../../models/product.model';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  productForm: FormGroup;
  isEditMode = false;
  productId: string | null = null;
  loading = false;
  submitting = false;
  
  // Image upload
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  existingImageUrl: string | null = null;

  categories: string[] = ['Electrónica', 'Ropa', 'Hogar', 'Deportes', 'Alimentos', 'Otros'];
  currencies: string[] = ['USD', 'EUR', 'MXN', 'COP', 'ARS'];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.maxLength(500)]],
      price: [0, [Validators.required, Validators.min(0)]],
      currency: ['USD', [Validators.required]],
      stock: [0, [Validators.min(0)]],
      category: ['Otros'],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('id');
    if (this.productId) {
      this.isEditMode = true;
      this.loadProduct();
    }
  }

  loadProduct(): void {
    this.loading = true;
    this.productService.getById(this.productId!).subscribe({
      next: (product) => {
        this.productForm.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
          currency: product.currency,
          stock: product.stock,
          category: product.category,
          active: product.active
        });
        if (product.imageUrl) {
          this.existingImageUrl = product.imageUrl;
          this.imagePreview = product.imageUrl;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.notificationService.showError('Error al cargar producto');
        this.loading = false;
        this.router.navigate(['/products']);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.notificationService.showError('Por favor seleccione una imagen válida');
        return;
      }
      
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.notificationService.showError('La imagen no puede exceder 5MB');
        return;
      }
      
      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
    this.existingImageUrl = null;
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.productForm.value;
    
    const productData: ProductDto = {
      name: formValue.name,
      description: formValue.description,
      price: formValue.price,
      currency: formValue.currency,
      stock: formValue.stock,
      category: formValue.category,
      active: formValue.active,
      imageUrl: this.existingImageUrl || undefined
    };

    if (this.isEditMode) {
      // Update existing product
      this.productService.update(this.productId!, productData).subscribe({
        next: (product) => {
          // If there's a new image, upload it
          if (this.selectedFile) {
            this.productService.updateImage(this.productId!, this.selectedFile!).subscribe({
              next: () => {
                this.notificationService.showSuccess('Producto actualizado con imagen');
                this.submitting = false;
                this.router.navigate(['/products']);
              },
              error: (error) => {
                console.error('Error uploading image:', error);
                this.notificationService.showWarning('Producto actualizado, pero error al subir imagen');
                this.submitting = false;
                this.router.navigate(['/products']);
              }
            });
          } else {
            this.notificationService.showSuccess('Producto actualizado exitosamente');
            this.submitting = false;
            this.router.navigate(['/products']);
          }
        },
        error: (error) => {
          console.error('Error updating product:', error);
          this.notificationService.showError(error.error?.message || 'Error al actualizar producto');
          this.submitting = false;
        }
      });
    } else {
      // Create new product
      if (this.selectedFile) {
        // Create with image
        this.productService.createWithImage(productData, this.selectedFile).subscribe({
          next: () => {
            this.notificationService.showSuccess('Producto creado exitosamente');
            this.submitting = false;
            this.router.navigate(['/products']);
          },
          error: (error) => {
            console.error('Error creating product:', error);
            this.notificationService.showError(error.error?.message || 'Error al crear producto');
            this.submitting = false;
          }
        });
      } else {
        // Create without image
        this.productService.create(productData).subscribe({
          next: () => {
            this.notificationService.showSuccess('Producto creado exitosamente');
            this.submitting = false;
            this.router.navigate(['/products']);
          },
          error: (error) => {
            console.error('Error creating product:', error);
            this.notificationService.showError(error.error?.message || 'Error al crear producto');
            this.submitting = false;
          }
        });
      }
    }
  }

  markAllAsTouched(): void {
    Object.values(this.productForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }

  get name() { return this.productForm.get('name'); }
  get price() { return this.productForm.get('price'); }
  get stock() { return this.productForm.get('stock'); }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}
