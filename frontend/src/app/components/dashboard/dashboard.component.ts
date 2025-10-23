import { CommonModule, DatePipe, TitleCasePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { BackendDataService, VehicleSummary } from '../../services/backend-data.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    TitleCasePipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Essential display state
  vehicles: VehicleSummary[] = [];
  loading = true;
  error: string | null = null;
  lastUpdate: Date = new Date();
  simulationRunning = false;
  simulationStatus = 'Unknown';
  simulationInfo = '';
  connectionStatus: string = 'disconnected';
  
  // Simple pagination state (synced from service)
  currentPage: number = 0;
  totalVehicles: number = 0;
  totalPages: number = 0;
  searchQuery: string = '';
  isSearching: boolean = false;
  
  private subscription: Subscription = new Subscription();

  constructor(private backendDataService: BackendDataService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadVehicles();
    this.syncPaginationState();
    this.subscribeToConnectionStatus();
    this.checkSimulationStatus();
    this.getSimulationInfo();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  loadVehicles(): void {
    // Subscribe to vehicles (ESSENTIAL)
    this.subscription.add(
      this.backendDataService.vehicles$.subscribe(vehicles => {
        console.log('🔄 Dashboard received vehicles update:', vehicles.length, 'vehicles');
        this.vehicles = vehicles;
        this.lastUpdate = new Date();
        this.cdr.detectChanges(); // Force change detection
      })
    );
    
    // Subscribe to loading state (ESSENTIAL)
    this.subscription.add(
      this.backendDataService.loading$.subscribe(loading => {
        this.loading = loading;
        this.cdr.detectChanges(); // Force change detection
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

  subscribeToConnectionStatus(): void {
    this.subscription.add(
      this.backendDataService.connectionStatus$.subscribe(status => {
        this.connectionStatus = status;
      })
    );
  }

  syncPaginationState(): void {
    // Sync pagination state every time vehicles update (SIMPLE)
    this.subscription.add(
      this.backendDataService.vehicles$.subscribe(() => {
        this.currentPage = this.backendDataService.getCurrentPage();
        this.totalVehicles = this.backendDataService.getTotalVehicles();
        this.totalPages = this.backendDataService.getTotalPages();
        this.isSearching = this.backendDataService.getIsSearching();
      })
    );
  }

  // Pagination methods
  nextPage(): void {
    this.backendDataService.nextPage();
  }

  previousPage(): void {
    this.backendDataService.previousPage();
  }

  goToPage(page: number): void {
    this.backendDataService.goToPage(page);
  }

  // Search methods
  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.backendDataService.searchVehicles(this.searchQuery);
    } else {
      this.clearSearch();
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.backendDataService.clearSearch();
  }

  onSearchInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchQuery = target.value;
  }

  onSearchKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.onSearch();
    }
  }

  // Utility methods for pagination UI
  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    const startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  canGoToPreviousPage(): boolean {
    return this.currentPage > 0;
  }

  canGoToNextPage(): boolean {
    return this.currentPage < this.totalPages - 1;
  }
}
