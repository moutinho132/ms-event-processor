import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LocalService } from '../../services/local.service';
import { NotificationService } from '../../services/notification.service';
import { Local } from '../../models/local.model';

@Component({
  selector: 'app-local-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './local-list.component.html',
  styleUrls: ['./local-list.component.scss']
})
export class LocalListComponent implements OnInit {
  locals: Local[] = [];
  loading = false;
  showModal = false;
  editingLocal: Local | null = null;

  constructor(
    private localService: LocalService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadLocals();
  }

  loadLocals(): void {
    this.loading = true;
    this.localService.getAll().subscribe({
      next: (locals) => {
        this.locals = locals;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading locals:', error);
        this.notificationService.showError('Error al cargar locales');
        this.loading = false;
      }
    });
  }

  createLocal(): void {
    const name = prompt('Nombre del local:');
    if (!name) return;
    
    const address = prompt('Dirección:');
    if (!address) return;
    
    const city = prompt('Ciudad:');
    const phone = prompt('Teléfono:');
    const email = prompt('Email:');
    
    const formData: any = {
      name,
      address,
      city: city || undefined,
      phone: phone || undefined,
      email: email || undefined
    };
    
    this.localService.create(formData).subscribe({
      next: () => {
        this.notificationService.showSuccess('Local creado exitosamente');
        this.loadLocals();
      },
      error: (error) => {
        console.error('Error creating local:', error);
        this.notificationService.showError('Error al crear local');
      }
    });
  }

  editLocal(localId: string): void {
    const local = this.locals.find(l => l.localId === localId);
    if (!local) return;
    
    const name = prompt('Nombre:', local.name);
    if (!name) return;
    
    const address = prompt('Dirección:', local.address);
    if (!address) return;
    
    const city = prompt('Ciudad:', local.city || '');
    const phone = prompt('Teléfono:', local.phone || '');
    const email = prompt('Email:', local.email || '');
    
    const formData: any = {
      name,
      address,
      city: city || undefined,
      phone: phone || undefined,
      email: email || undefined
    };
    
    this.localService.update(localId, formData).subscribe({
      next: () => {
        this.notificationService.showSuccess('Local actualizado');
        this.loadLocals();
      },
      error: (error) => {
        console.error('Error updating local:', error);
        this.notificationService.showError('Error al actualizar local');
      }
    });
  }

  viewWarehouses(localId: string): void {
    // Navigate to warehouses filtered by local
    this.notificationService.showInfo('Navegando a almacenes del local...');
  }

  activateLocal(localId: string): void {
    this.localService.activate(localId).subscribe({
      next: () => {
        this.notificationService.showSuccess('Local activado');
        this.loadLocals();
      },
      error: (error) => {
        console.error('Error activating local:', error);
        this.notificationService.showError('Error al activar local');
      }
    });
  }

  deactivateLocal(localId: string): void {
    if (confirm('¿Está seguro de desactivar este local?')) {
      this.localService.deactivate(localId).subscribe({
        next: () => {
          this.notificationService.showSuccess('Local desactivado');
          this.loadLocals();
        },
        error: (error) => {
          console.error('Error deactivating local:', error);
          this.notificationService.showError('Error al desactivar local');
        }
      });
    }
  }
}
