import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'orders',
    loadChildren: () => import('./orders/orders.module').then(m => m.OrdersModule)
  },
  {
    path: '',
    redirectTo: '/orders',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/orders'
  }
];
