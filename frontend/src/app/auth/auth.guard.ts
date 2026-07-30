import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';
import { map, take } from 'rxjs/operators';

/**
 * Guard que verifica si el usuario está autenticado.
 * Si no está autenticado, redirige al login.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Guardar la URL intentada para redireccionar después del login
  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });
  return false;
};

/**
 * Guard que verifica si el usuario tiene un rol específico.
 * Uso: canActivate: [roleGuard(['ADMIN', 'SUPERVISOR'])]
 */
export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], {
        queryParams: { returnUrl: state.url }
      });
      return false;
    }

    const user = authService.getCurrentUser();
    if (user && allowedRoles.includes(user.role)) {
      return true;
    }

    // Usuario no tiene el rol requerido
    router.navigate(['/dashboard']);
    return false;
  };
};

/**
 * Guard para rutas que solo deben ser accesibles sin autenticación (como login).
 * Si ya está autenticado, redirige al dashboard.
 */
export const noAuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return router.parseUrl('/dashboard');
  }

  return true;
};
