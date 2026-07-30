import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';
import { UserRole } from '../../models/user.model';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  userForm: FormGroup;
  isEditMode = false;
  userId: string | null = null;
  loading = false;
  submitting = false;

  roleOptions = [
    { value: UserRole.ADMIN, label: 'Administrador', description: 'Acceso total al sistema' },
    { value: UserRole.SUPERVISOR, label: 'Supervisor', description: 'Gestión de productos y stock' },
    { value: UserRole.CUSTOMER, label: 'Cliente', description: 'Usuario final de compras' }
  ];

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.userForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.maxLength(20)]],
      role: [UserRole.CUSTOMER, [Validators.required]],
      password: ['', [Validators.minLength(6)]],
      confirmPassword: ['']
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId) {
      this.isEditMode = true;
      this.loadUser();
    }
  }

  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    
    if (password && confirmPassword && password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  loadUser(): void {
    this.loading = true;
    this.userService.getById(this.userId!).subscribe({
      next: (user) => {
        this.userForm.patchValue({
          name: user.name,
          email: user.email,
          phone: user.phone,
          role: user.role
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading user:', error);
        this.notificationService.showError('Error al cargar usuario');
        this.loading = false;
        this.router.navigate(['/users']);
      }
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.markAllAsTouched();
      return;
    }

    const password = this.userForm.get('password')?.value;
    if (!this.isEditMode && !password) {
      this.notificationService.showError('La contraseña es requerida para nuevos usuarios');
      return;
    }

    this.submitting = true;
    const formValue = this.userForm.value;
    
    // Solo incluir contraseña si se proporcionó
    const userData = {
      name: formValue.name,
      email: formValue.email,
      phone: formValue.phone,
      role: formValue.role,
      ...(formValue.password ? { password: formValue.password } : {})
    };

    if (this.isEditMode) {
      this.userService.update(this.userId!, userData).subscribe({
        next: () => {
          this.notificationService.showSuccess('Usuario actualizado exitosamente');
          this.submitting = false;
          this.router.navigate(['/users']);
        },
        error: (error) => {
          console.error('Error updating user:', error);
          this.notificationService.showError(error.error?.message || 'Error al actualizar usuario');
          this.submitting = false;
        }
      });
    } else {
      this.userService.create(userData).subscribe({
        next: () => {
          this.notificationService.showSuccess('Usuario creado exitosamente');
          this.submitting = false;
          this.router.navigate(['/users']);
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.notificationService.showError(error.error?.message || 'Error al crear usuario');
          this.submitting = false;
        }
      });
    }
  }

  markAllAsTouched(): void {
    Object.values(this.userForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }

  get name() { return this.userForm.get('name'); }
  get email() { return this.userForm.get('email'); }
  get phone() { return this.userForm.get('phone'); }
  get role() { return this.userForm.get('role'); }
  get password() { return this.userForm.get('password'); }
  get confirmPassword() { return this.userForm.get('confirmPassword'); }

  getSelectedRoleDescription(): string | null {
    const roleValue = this.role?.value;
    const option = this.roleOptions.find(r => r.value === roleValue);
    return option ? option.description : null;
  }

  cancel(): void {
    this.router.navigate(['/users']);
  }
}
