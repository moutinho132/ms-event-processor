import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';
import { User, UserRole, UserStats } from '../../models/user.model';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  stats: UserStats | null = null;
  loading = false;
  searchForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  pageSize = 10;
  
  // Rol options for filter
  roleOptions = [
    { value: UserRole.ADMIN, label: 'Administrador' },
    { value: UserRole.SUPERVISOR, label: 'Supervisor' },
    { value: UserRole.CUSTOMER, label: 'Cliente' }
  ];

  selectedRole: UserRole | null = null;

  constructor(
    private userService: UserService,
    private notificationService: NotificationService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      query: ['']
    });
  }

  ngOnInit(): void {
    this.loadUsers();
    this.loadStats();
  }

  loadUsers(): void {
    this.loading = true;
    
    if (this.selectedRole) {
      this.userService.getByRole(this.selectedRole).subscribe({
        next: (users) => {
          this.users = users;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading users:', error);
          this.notificationService.showError('Error al cargar usuarios');
          this.loading = false;
        }
      });
    } else {
      this.userService.getPage(this.currentPage, this.pageSize).subscribe({
        next: (response) => {
          this.users = response.content;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading users:', error);
          this.notificationService.showError('Error al cargar usuarios');
          this.loading = false;
        }
      });
    }
  }

  loadStats(): void {
    this.userService.getStats().subscribe({
      next: (stats) => this.stats = stats,
      error: (error) => console.error('Error loading stats:', error)
    });
  }

  onSearch(): void {
    const query = this.searchForm.get('query')?.value;
    if (query && query.trim()) {
      this.loading = true;
      this.userService.search(query).subscribe({
        next: (response) => {
          this.users = response.content;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error searching users:', error);
          this.notificationService.showError('Error en la búsqueda');
          this.loading = false;
        }
      });
    } else {
      this.loadUsers();
    }
  }

  filterByRole(role: UserRole | string | null): void {
    this.selectedRole = role as UserRole | null;
    this.loadUsers();
  }

  editUser(userId: string): void {
    this.router.navigate(['/users/edit', userId]);
  }

  createUser(): void {
    this.router.navigate(['/users/new']);
  }

  deactivateUser(userId: string): void {
    if (confirm('¿Está seguro de desactivar este usuario?')) {
      this.userService.deactivate(userId).subscribe({
        next: () => {
          this.notificationService.showSuccess('Usuario desactivado');
          this.loadUsers();
          this.loadStats();
        },
        error: (error) => {
          console.error('Error deactivating user:', error);
          this.notificationService.showError('Error al desactivar usuario');
        }
      });
    }
  }

  activateUser(userId: string): void {
    this.userService.activate(userId).subscribe({
      next: () => {
        this.notificationService.showSuccess('Usuario activado');
        this.loadUsers();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error activating user:', error);
        this.notificationService.showError('Error al activar usuario');
      }
    });
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  getRoleBadgeClass(role: UserRole): string {
    switch (role) {
      case UserRole.ADMIN: return 'badge-danger';
      case UserRole.SUPERVISOR: return 'badge-warning';
      case UserRole.CUSTOMER: return 'badge-info';
      default: return 'badge-secondary';
    }
  }

  getRoleLabel(role: UserRole): string {
    const found = this.roleOptions.find(r => r.value === role);
    return found ? found.label : role;
  }
}
