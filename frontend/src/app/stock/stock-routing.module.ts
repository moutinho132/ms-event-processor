import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StockDashboardComponent } from './stock-dashboard/stock-dashboard.component';
import { StockMovementComponent } from './stock-movement/stock-movement.component';

const routes: Routes = [
  { path: '', component: StockDashboardComponent },
  { path: 'movement', component: StockMovementComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StockRoutingModule { }
