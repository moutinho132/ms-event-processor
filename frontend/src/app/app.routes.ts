import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './auth/login.component';
import { authGuard, noAuthGuard } from './auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [noAuthGuard]
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'orders',
    loadChildren: () => import('./orders/orders.module').then(m => m.OrdersModule),
    canActivate: [authGuard]
  },
  {
    path: 'users',
    loadChildren: () => import('./users/users.module').then(m => m.UsersModule),
    canActivate: [authGuard]
  },
  {
    path: 'locals',
    loadChildren: () => import('./locals/locals.module').then(m => m.LocalsModule),
    canActivate: [authGuard]
  },
  {
    path: 'warehouses',
    loadChildren: () => import('./warehouses/warehouses.module').then(m => m.WarehousesModule),
    canActivate: [authGuard]
  },
  {
    path: 'products',
    loadChildren: () => import('./products/products.module').then(m => m.ProductsModule),
    canActivate: [authGuard]
  },
  {
    path: 'stock',
    loadChildren: () => import('./stock/stock.module').then(m => m.StockModule),
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
