import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LocalListComponent } from './local-list/local-list.component';

const routes: Routes = [
  { path: '', component: LocalListComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LocalsRoutingModule { }
