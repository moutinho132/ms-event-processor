import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WarehouseService } from '../../services/warehouse.service';
import { LocalService } from '../../services/local.service';
import { NotificationService } from '../../services/notification.service';
import { Warehouse } from '../../models/warehouse.model';

@Component({
  selector: 'app-warehouse-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './warehouse-list.component.html',
  styleUrls: ['./warehouse-list.component.scss']
})
export class WarehouseListComponent implements OnInit {
  warehouses: Warehouse[] = [];
  loading = false;

  constructor(
    private warehouseService: WarehouseService,
    private localService: LocalService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadWarehouses();
  }

  loadWarehouses(): void {
    this.loading = true;
    this.warehouseService.getAll().subscribe({
      next: (warehouses) => {
        this.warehouses = warehouses;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading warehouses:', error);
        this.notificationService.showError('Error al cargar almacenes');
        this.loading = false;
      }
    });
  }

  createWarehouse(): void {
    const name = prompt('Nombre del almacén:');
    if (!name) return;
    
    const code = prompt('Código (ej: ALM-001):');
    const location = prompt('Ubicación:');
    const capacityStr = prompt('Capacidad:');
    const capacity = capacityStr ? parseInt(capacityStr) : undefined;
    
    const tempControlled = confirm('¿Tiene control de temperatura?');
    let minTemp: number | undefined;
    let maxTemp: number | undefined;
    if (tempControlled) {
      minTemp = parseFloat(prompt('Temperatura mínima (°C):') || '0');
      maxTemp = parseFloat(prompt('Temperatura máxima (°C):') || '0');
    }
    
    // Get local ID from available locals
    this.localService.getAll().subscribe({
      next: (locals) => {
        if (locals.length === 0) {
          this.notificationService.showError('No hay locales disponibles. Crea un local primero.');
          return;
        }
        
        const localOptions = locals.map((l, i) => `${i + 1}. ${l.name}`).join('\n');
        const selection = prompt(`Selecciona el local:\n${localOptions}\n\nIngresa el número:`);
        const index = parseInt(selection || '0') - 1;
        
        if (index < 0 || index >= locals.length) {
          this.notificationService.showError('Selección inválida');
          return;
        }
        
        const formData: any = {
          name,
          code: code || undefined,
          location: location || undefined,
          capacity,
          temperatureControlled: tempControlled,
          minTemperature: minTemp,
          maxTemperature: maxTemp,
          localId: locals[index].localId
        };
        
        this.warehouseService.create(formData).subscribe({
          next: () => {
            this.notificationService.showSuccess('Almacén creado exitosamente');
            this.loadWarehouses();
          },
          error: (error) => {
            console.error('Error creating warehouse:', error);
            this.notificationService.showError('Error al crear almacén');
          }
        });
      },
      error: (error) => {
        console.error('Error loading locals:', error);
        this.notificationService.showError('Error al cargar locales');
      }
    });
  }

  editWarehouse(warehouseId: string): void {
    const warehouse = this.warehouses.find(w => w.warehouseId === warehouseId);
    if (!warehouse) return;
    
    const name = prompt('Nombre:', warehouse.name);
    if (!name) return;
    
    const code = prompt('Código:', warehouse.code || '');
    const location = prompt('Ubicación:', warehouse.location || '');
    const capacityStr = prompt('Capacidad:', warehouse.capacity?.toString() || '');
    const capacity = capacityStr ? parseInt(capacityStr) : undefined;
    
    const formData: any = {
      name,
      code: code || undefined,
      location: location || undefined,
      capacity,
      localId: warehouse.localId
    };
    
    this.warehouseService.update(warehouseId, formData).subscribe({
      next: () => {
        this.notificationService.showSuccess('Almacén actualizado');
        this.loadWarehouses();
      },
      error: (error) => {
        console.error('Error updating warehouse:', error);
        this.notificationService.showError('Error al actualizar almacén');
      }
    });
  }

  activateWarehouse(warehouseId: string): void {
    this.warehouseService.activate(warehouseId).subscribe({
      next: () => {
        this.notificationService.showSuccess('Almacén activado');
        this.loadWarehouses();
      },
      error: (error) => {
        console.error('Error activating warehouse:', error);
        this.notificationService.showError('Error al activar almacén');
      }
    });
  }

  deactivateWarehouse(warehouseId: string): void {
    if (confirm('¿Está seguro de desactivar este almacén?')) {
      this.warehouseService.deactivate(warehouseId).subscribe({
        next: () => {
          this.notificationService.showSuccess('Almacén desactivado');
          this.loadWarehouses();
        },
        error: (error) => {
          console.error('Error deactivating warehouse:', error);
          this.notificationService.showError('Error al desactivar almacén');
        }
      });
    }
  }
}
