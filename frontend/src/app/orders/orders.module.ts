import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { OrdersRoutingModule } from './orders-routing.module';
import { OrderListComponent } from './order-list/order-list.component';
import { OrderDetailComponent } from './order-detail/order-detail.component';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule,
    OrdersRoutingModule,
    OrderListComponent,
    OrderDetailComponent
  ]
})
export class OrdersModule { }
