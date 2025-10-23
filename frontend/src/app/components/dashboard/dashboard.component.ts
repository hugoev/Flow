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
  
  // Alert management
  private alerts: Map<string, VehicleAlert> = new Map();
  private dismissedAlerts: Set<string> = new Set();
  
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

  // Total fleet statistics (not just current page)
  getTotalFleetOnlineCount(): number {
    // Calculate offline vehicles across entire fleet (max 5)
    const time = Date.now();
    const timeSlot = Math.floor(time / 20000); // 20-second slots
    const maxOffline = 5; // Max 5 vehicles offline across entire fleet
    
    // Generate all possible vehicle IDs (VH001 to VH100)
    const allVehicleIds: string[] = [];
    for (let i = 1; i <= 100; i++) {
      allVehicleIds.push(`VH${i.toString().padStart(3, '0')}`);
    }
    
    // Calculate which vehicles are offline across entire fleet
    let offlineCount = 0;
    for (let i = 0; i < maxOffline && i < allVehicleIds.length; i++) {
      const rotationIndex = (timeSlot + i) % allVehicleIds.length;
      const selectedVehicle = allVehicleIds[rotationIndex];
      
      // Add some randomness to make it more realistic
      const vehicleHash = this.hashString(selectedVehicle);
      const shouldBeOffline = (Math.sin(time / 15000 + vehicleHash) + 1) / 2;
      
      // 60-80% chance of being offline when selected
      if (shouldBeOffline > 0.2) {
        offlineCount++;
      }
    }
    
    return this.totalVehicles - offlineCount;
  }

  getTotalFleetOfflineCount(): number {
    return this.totalVehicles - this.getTotalFleetOnlineCount();
  }

  getTotalFleetAverageSpeed(): number {
    // For now, use current page average as proxy for fleet average
    // In a real implementation, you'd want to get this from the backend
    return this.getAverageSpeed();
  }

  getTotalFleetAverageFuel(): number {
    // For now, use current page average as proxy for fleet average
    // In a real implementation, you'd want to get this from the backend
    return this.getAverageFuelLevel();
  }

  getTotalFleetAverageTemp(): number {
    // For now, use current page average as proxy for fleet average
    // In a real implementation, you'd want to get this from the backend
    return this.getAverageEngineTemp();
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

  getCurrentPageEnd(): number {
    return Math.min((this.currentPage + 1) * 15, this.totalVehicles);
  }

  // Alert management methods
  getActiveAlerts(): VehicleAlert[] {
    // Generate simulated alerts based on vehicle conditions
    this.updateAlerts();
    const activeAlerts = Array.from(this.alerts.values())
      .filter(alert => !this.dismissedAlerts.has(alert.vehicleId));
    
    // Sort alerts by severity (critical first) and timestamp (newest first)
    activeAlerts.sort((a, b) => {
      if (a.severity === 'critical' && b.severity !== 'critical') return -1;
      if (a.severity !== 'critical' && b.severity === 'critical') return 1;
      return b.timestamp.getTime() - a.timestamp.getTime();
    });
    
    // Show more alerts if there are critical offline vehicles
    const criticalAlerts = activeAlerts.filter(alert => alert.severity === 'critical');
    const maxAlerts = criticalAlerts.length > 0 ? 8 : 5; // Show up to 8 if there are critical alerts
    
    return activeAlerts.slice(0, maxAlerts);
  }

  private updateAlerts(): void {
    // Get offline vehicles across entire fleet (not just current page)
    const offlineVehicles = this.getFleetOfflineVehicles();
    
    // Create alerts for all offline vehicles in the fleet
    offlineVehicles.forEach(vehicleId => {
      this.alerts.set(vehicleId, {
        vehicleId: vehicleId,
        type: 'Vehicle Offline',
        severity: 'critical',
        timestamp: new Date()
      });
    });
    
    // Check current page vehicles for other alert conditions
    this.vehicles.forEach(vehicle => {
      // Skip offline vehicles (already handled above)
      if (vehicle.status === 'offline') {
        return;
      }
      
      // Low fuel alerts (only for online vehicles)
      if (vehicle.fuelLevel < 20) {
        this.alerts.set(vehicle.vehicleId, {
          vehicleId: vehicle.vehicleId,
          type: 'Low Fuel',
          severity: vehicle.fuelLevel < 10 ? 'critical' : 'warning',
          timestamp: new Date()
        });
      }
      // High temperature alerts (only for online vehicles)
      else if (vehicle.engineTemp > 95) {
        this.alerts.set(vehicle.vehicleId, {
          vehicleId: vehicle.vehicleId,
          type: 'High Temperature',
          severity: vehicle.engineTemp > 100 ? 'critical' : 'warning',
          timestamp: new Date()
        });
      }
      // Remove alert only if vehicle is online and all conditions are resolved
      else if (vehicle.status === 'online') {
        this.alerts.delete(vehicle.vehicleId);
      }
    });
    
    // Clean up alerts for vehicles that are no longer offline
    const currentOfflineVehicles = new Set(offlineVehicles);
    this.alerts.forEach((alert, vehicleId) => {
      if (alert.type === 'Vehicle Offline' && !currentOfflineVehicles.has(vehicleId)) {
        this.alerts.delete(vehicleId);
      }
    });
  }

  /**
   * Get offline vehicles across entire fleet (not just current page)
   */
  private getFleetOfflineVehicles(): string[] {
    const time = Date.now();
    const timeSlot = Math.floor(time / 20000); // 20-second slots
    const maxOffline = 5; // Max 5 vehicles offline across entire fleet
    
    // Generate all possible vehicle IDs (VH001 to VH100)
    const allVehicleIds: string[] = [];
    for (let i = 1; i <= 100; i++) {
      allVehicleIds.push(`VH${i.toString().padStart(3, '0')}`);
    }
    
    // Calculate which vehicles are offline across entire fleet
    const offlineVehicles: string[] = [];
    for (let i = 0; i < maxOffline && i < allVehicleIds.length; i++) {
      const rotationIndex = (timeSlot + i) % allVehicleIds.length;
      const selectedVehicle = allVehicleIds[rotationIndex];
      
      // Add some randomness to make it more realistic
      const vehicleHash = this.hashString(selectedVehicle);
      const shouldBeOffline = (Math.sin(time / 15000 + vehicleHash) + 1) / 2;
      
      // 60-80% chance of being offline when selected
      if (shouldBeOffline > 0.2) {
        offlineVehicles.push(selectedVehicle);
      }
    }
    
    return offlineVehicles;
  }

  dismissAlert(vehicleId: string): void {
    this.dismissedAlerts.add(vehicleId);
    this.cdr.detectChanges();
  }

  dismissAllAlerts(): void {
    this.getActiveAlerts().forEach(alert => {
      this.dismissedAlerts.add(alert.vehicleId);
    });
    this.cdr.detectChanges();
  }

  getAlertTime(timestamp: Date): string {
    const now = new Date();
    const diff = Math.floor((now.getTime() - timestamp.getTime()) / 1000);
    
    if (diff < 60) return 'Just now';
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    return `${Math.floor(diff / 3600)}h ago`;
  }

  /**
   * Simple hash function for deterministic "random" behavior
   */
  private hashString(str: string): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32-bit integer
    }
    return Math.abs(hash);
  }
}

// Alert interface
interface VehicleAlert {
  vehicleId: string;
  type: string;
  severity: 'warning' | 'critical';
  timestamp: Date;
}
