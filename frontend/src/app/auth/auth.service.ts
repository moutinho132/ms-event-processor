import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';

interface AuthResponse {
  status: string;
  message: string;
  token?: string;
  tokenType?: string;
  expiresIn?: number;
  customer?: {
    customerId: string;
    name: string;
    email: string;
    phone?: string;
    role: string;
  };
}

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = '/api/v1/auth';
  
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor() {
    this.checkStoredAuth();
  }

  private checkStoredAuth(): void {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    if (token && user) {
      // Verificar si el token ha expirado
      if (this.isTokenExpired(token)) {
        this.logout();
        return;
      }
      
      this.currentUserSubject.next(JSON.parse(user));
      this.isAuthenticatedSubject.next(true);
    }
  }

  /**
   * Verifica si el token JWT ha expirado
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload || !payload.exp) {
        return false;
      }
      
      // exp está en segundos desde epoch
      const expirationDate = new Date(payload.exp * 1000);
      return expirationDate <= new Date();
    } catch (e) {
      return true;
    }
  }

  /**
   * Decodifica el payload del token JWT
   */
  private decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }

  login(email: string, password: string): Observable<AuthResponse> {
    const request: LoginRequest = { email, password };
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        if (response.status === 'SUCCESS' && response.token) {
          this.setSession(response);
        }
      })
    );
  }

  register(name: string, email: string, password: string, phone?: string): Observable<AuthResponse> {
    const request: RegisterRequest = { name, email, password, phone };
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => {
        if (response.status === 'SUCCESS' && response.token) {
          this.setSession(response);
        }
      })
    );
  }

  private setSession(authResult: AuthResponse): void {
    if (authResult.token) {
      localStorage.setItem('token', authResult.token);
    }
    if (authResult.customer) {
      localStorage.setItem('user', JSON.stringify(authResult.customer));
      this.currentUserSubject.next(authResult.customer);
    }
    this.isAuthenticatedSubject.next(true);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const token = localStorage.getItem('token');
    
    // Verificar expiración cada vez que se solicita el token
    if (token && this.isTokenExpired(token)) {
      this.logout();
      return null;
    }
    
    return token;
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    
    // Si hay token, verificar que no haya expirado
    if (token && this.isTokenExpired(token)) {
      this.logout();
      return false;
    }
    
    return this.isAuthenticatedSubject.value;
  }

  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  /**
   * Verifica si el usuario tiene uno de los roles especificados
   */
  hasAnyRole(roles: string[]): boolean {
    const user = this.getCurrentUser();
    return user?.role && roles.includes(user.role);
  }

  /**
   * Obtiene el nombre del rol actual
   */
  getRoleName(): string {
    const user = this.getCurrentUser();
    switch (user?.role) {
      case 'ADMIN': return 'Administrador';
      case 'SUPERVISOR': return 'Supervisor';
      case 'CUSTOMER': return 'Cliente';
      default: return 'Usuario';
    }
  }
}
