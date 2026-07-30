import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { StockRoutingModule } from './stock-routing.module';
import { StockDashboardComponent } from './stock-dashboard/stock-dashboard.component';
import { StockMovementComponent } from './stock-movement/stock-movement.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    StockDashboardComponent,
    StockMovementComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    StockRoutingModule,
    SharedModule
  ]
})
export class StockModule { }
