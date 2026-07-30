import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  isLoginMode = true;
  email = '';
  password = '';
  name = '';
  phone = '';
  loading = false;
  error: string | null = null;
  success: string | null = null;
  private returnUrl: string = '/dashboard';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Obtener la URL de retorno de los query params
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.error = null;
    this.success = null;
  }

  onSubmit(): void {
    this.loading = true;
    this.error = null;
    this.success = null;

    if (this.isLoginMode) {
      this.authService.login(this.email, this.password).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.status === 'SUCCESS') {
            // Redirigir a la URL de retorno o dashboard
            this.router.navigate([this.returnUrl]);
          } else {
            this.error = response.message;
          }
        },
        error: (err) => {
          this.loading = false;
          this.error = err.error?.message || 'Error al iniciar sesión';
        }
      });
    } else {
      this.authService.register(this.name, this.email, this.password, this.phone).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.status === 'SUCCESS') {
            // Redirigir a la URL de retorno o dashboard
            this.router.navigate([this.returnUrl]);
          } else {
            this.error = response.message;
          }
        },
        error: (err) => {
          this.loading = false;
          this.error = err.error?.message || 'Error al registrar usuario';
        }
      });
    }
  }
}
