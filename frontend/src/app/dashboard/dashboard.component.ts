import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ChartConfiguration, ChartType } from 'chart.js';
import { ChartsModule } from '../charts.module';
import { Order, OrderStatus } from '../models/order.model';
import { StatsService } from '../services/stats.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, ChartsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  orders: Order[] = [];
  loading = true;

  // Estadísticas
  totalOrders = 0;
  pendingCount = 0;
  processingCount = 0;
  completedCount = 0;
  cancelledCount = 0;
  totalRevenue = 0;

  // Gráfica de estados (Doughnut)
  public statusChartType: ChartType = 'doughnut';
  public statusChartData: ChartConfiguration['data'] = {
    labels: ['Pendientes', 'Confirmadas', 'Procesando', 'Enviadas', 'Entregadas', 'Canceladas'],
    datasets: [{
      data: [0, 0, 0, 0, 0, 0],
      backgroundColor: [
        '#fbbf24', // Pendiente - amarillo
        '#818cf8', // Confirmada - índigo
        '#60a5fa', // Procesando - azul
        '#f472b6', // Enviada - rosa
        '#34d399', // Entregada - verde
        '#f87171'  // Cancelada - rojo
      ],
      borderWidth: 2,
      borderColor: '#fff'
    }]
  };
  public statusChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          padding: 20,
          usePointStyle: true,
          font: { size: 12 }
        }
      }
    }
  };

  // Gráfica de tendencia (Line)
  public trendChartType: ChartType = 'line';
  public trendChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{
      label: 'Órdenes por día',
      data: [],
      borderColor: '#667eea',
      backgroundColor: 'rgba(102, 126, 234, 0.1)',
      fill: true,
      tension: 0.4,
      pointBackgroundColor: '#667eea',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      pointRadius: 4
    }]
  };
  public trendChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { stepSize: 1 }
      }
    }
  };

  // Gráfica de ingresos (Bar)
  public revenueChartType: ChartType = 'bar';
  public revenueChartData: ChartConfiguration['data'] = {
    labels: ['Pendiente', 'En proceso', 'Entregado', 'Cancelado'],
    datasets: [{
      label: 'Monto ($)',
      data: [0, 0, 0, 0],
      backgroundColor: [
        'rgba(251, 191, 36, 0.8)',
        'rgba(96, 165, 250, 0.8)',
        'rgba(52, 211, 153, 0.8)',
        'rgba(248, 113, 113, 0.8)'
      ],
      borderRadius: 8
    }]
  };
  public revenueChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.statsService.getOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.calculateStats();
        this.updateCharts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading stats:', err);
        this.loading = false;
      }
    });
  }

  private calculateStats(): void {
    this.totalOrders = this.orders.length;
    this.pendingCount = this.orders.filter(o => o.status === OrderStatus.PENDING || o.status === OrderStatus.CONFIRMED).length;
    this.processingCount = this.orders.filter(o => o.status === OrderStatus.PROCESSING || o.status === OrderStatus.SHIPPED).length;
    this.completedCount = this.orders.filter(o => o.status === OrderStatus.DELIVERED).length;
    this.cancelledCount = this.orders.filter(o => o.status === OrderStatus.CANCELLED).length;
    
    this.totalRevenue = this.orders
      .filter(o => o.status === OrderStatus.DELIVERED)
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);
  }

  private updateCharts(): void {
    // Actualizar gráfica de estados
    const pending = this.orders.filter(o => o.status === OrderStatus.PENDING).length;
    const confirmed = this.orders.filter(o => o.status === OrderStatus.CONFIRMED).length;
    const processing = this.orders.filter(o => o.status === OrderStatus.PROCESSING).length;
    const shipped = this.orders.filter(o => o.status === OrderStatus.SHIPPED).length;
    const delivered = this.orders.filter(o => o.status === OrderStatus.DELIVERED).length;
    const cancelled = this.orders.filter(o => o.status === OrderStatus.CANCELLED).length;

    this.statusChartData.datasets[0].data = [pending, confirmed, processing, shipped, delivered, cancelled];

    // Actualizar gráfica de tendencia (últimos 7 días)
    const last7Days = this.getLast7Days();
    const ordersByDay = last7Days.map(date => {
      return this.orders.filter(o => {
        const orderDate = new Date(o.createdAt).toDateString();
        return orderDate === date.toDateString();
      }).length;
    });

    this.trendChartData.labels = last7Days.map(d => d.toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric' }));
    this.trendChartData.datasets[0].data = ordersByDay;

    // Actualizar gráfica de ingresos
    const pendingRevenue = this.orders
      .filter(o => o.status === OrderStatus.PENDING || o.status === OrderStatus.CONFIRMED)
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);
    const processingRevenue = this.orders
      .filter(o => o.status === OrderStatus.PROCESSING || o.status === OrderStatus.SHIPPED)
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);
    const deliveredRevenue = this.orders
      .filter(o => o.status === OrderStatus.DELIVERED)
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);
    const cancelledRevenue = this.orders
      .filter(o => o.status === OrderStatus.CANCELLED)
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

    this.revenueChartData.datasets[0].data = [pendingRevenue, processingRevenue, deliveredRevenue, cancelledRevenue];
  }

  private getLast7Days(): Date[] {
    const days: Date[] = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      days.push(date);
    }
    return days;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  refreshStats(): void {
    this.loadStats();
  }
}
