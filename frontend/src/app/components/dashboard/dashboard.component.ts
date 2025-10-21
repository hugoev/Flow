import { CommonModule, DatePipe, DecimalPipe, TitleCasePipe } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { BackendDataService, VehicleSummary } from '../../services/backend-data.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    DecimalPipe,
    TitleCasePipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  vehicles: VehicleSummary[] = [];
  loading = true;
  error: string | null = null;
  lastUpdate: Date = new Date();
  simulationRunning = false;
  simulationStatus = 'Unknown';
  simulationInfo = '';
  private subscription: Subscription = new Subscription();

  constructor(private backendDataService: BackendDataService) {
    console.log('DashboardComponent constructor called');
  }

  ngOnInit(): void {
    this.loadVehicles();
    this.checkSimulationStatus();
    this.getSimulationInfo();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  loadVehicles(): void {
    this.subscription.add(
      this.backendDataService.vehicles$.subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          this.loading = false;
          this.error = null;
          this.lastUpdate = new Date();
        },
        error: (error) => {
          console.error('Error loading vehicles:', error);
          this.error = 'Failed to load vehicle data';
          this.loading = false;
        }
      })
    );
  }

  getStatusColor(status: string): string {
    return status === 'online' ? 'primary' : 'warn';
  }

  getSpeedColor(speed: number): string {
    if (speed > 80) return 'warn';
    if (speed > 60) return 'accent';
    return 'primary';
  }

  getFuelColor(fuelLevel: number): string {
    if (fuelLevel < 20) return 'warn';
    if (fuelLevel < 50) return 'accent';
    return 'primary';
  }

  getTempColor(temp: number): string {
    if (temp > 100) return 'warn';
    if (temp > 90) return 'accent';
    return 'primary';
  }

  refreshData(): void {
    this.loading = true;
    this.backendDataService.getAllVehicles().subscribe();
  }

  getAverageSpeed(): number {
    if (this.vehicles.length === 0) return 0;
    const totalSpeed = this.vehicles.reduce((sum, vehicle) => sum + vehicle.currentSpeed, 0);
    return Math.round(totalSpeed / this.vehicles.length);
  }

  getOnlineVehiclesCount(): number {
    return this.vehicles.filter(v => v.status === 'online').length;
  }

  getOfflineVehiclesCount(): number {
    return this.vehicles.filter(v => v.status === 'offline').length;
  }

  getTotalDistance(): number {
    return this.vehicles.reduce((sum, vehicle) => sum + vehicle.totalDistance, 0);
  }

  getAverageFuelLevel(): number {
    if (this.vehicles.length === 0) return 0;
    const totalFuel = this.vehicles.reduce((sum, vehicle) => sum + vehicle.fuelLevel, 0);
    return Math.round(totalFuel / this.vehicles.length);
  }

  getAverageEngineTemp(): number {
    if (this.vehicles.length === 0) return 0;
    const totalTemp = this.vehicles.reduce((sum, vehicle) => sum + vehicle.engineTemp, 0);
    return Math.round(totalTemp / this.vehicles.length);
  }

  clearError(): void {
    this.error = null;
  }

  startSimulation(): void {
    this.backendDataService.startSimulation().subscribe({
      next: (response) => {
        console.log('Simulation started:', response);
        this.simulationRunning = true;
        this.checkSimulationStatus();
      },
      error: (error) => {
        console.error('Error starting simulation:', error);
        this.error = 'Failed to start simulation';
      }
    });
  }

  stopSimulation(): void {
    this.backendDataService.stopSimulation().subscribe({
      next: (response) => {
        console.log('Simulation stopped:', response);
        this.simulationRunning = false;
        this.checkSimulationStatus();
      },
      error: (error) => {
        console.error('Error stopping simulation:', error);
        this.error = 'Failed to stop simulation';
      }
    });
  }

  checkSimulationStatus(): void {
    this.backendDataService.getSimulationStatus().subscribe({
      next: (status) => {
        this.simulationStatus = status;
        this.simulationRunning = status.toLowerCase().includes('running') || status.toLowerCase().includes('started');
      },
      error: (error) => {
        console.error('Error checking simulation status:', error);
        this.simulationStatus = 'Unknown';
      }
    });
  }

  getSimulationInfo(): void {
    this.backendDataService.getSimulationInfo().subscribe({
      next: (info) => {
        this.simulationInfo = info;
      },
      error: (error) => {
        console.error('Error getting simulation info:', error);
        this.simulationInfo = 'Unknown';
      }
    });
  }
}
