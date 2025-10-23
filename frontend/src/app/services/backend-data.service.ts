import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

export interface BackendTelemetryData {
  key: {
    vehicleId: string;
    timestamp: string;
  };
  latitude: number;
  longitude: number;
  speed: number;
  fuelLevel: number;
  engineTemp: number;
  tirePressure: number;
  timestamp: string;
  vehicleId: string;
  totalDistance?: number; // Optional since it might not be in all records
}

export interface StreamingTelemetryData {
  vehicleId: string;
  timestamp: string;
  latitude: number;
  longitude: number;
  speedKmh: number;
  fuelLevelPercentage: number;
  engineTemperatureCelsius: number;
  tirePressurePsi: number;
  totalDistanceKm: number;
}

export interface VehicleSummary {
  vehicleId: string;
  lastUpdate: Date;
  currentSpeed: number;
  fuelLevel: number;
  engineTemp: number;
  status: 'online' | 'offline';
  totalDistance: number;
  latitude?: number;
  longitude?: number;
  tirePressure?: number;
}

@Injectable({
  providedIn: 'root'
})
export class BackendDataService {
  private processingApiUrl = 'http://localhost:8082/api/processing';
  private ingestionApiUrl = 'http://localhost:8081';
  private streamingApiUrl = 'http://localhost:8083/api/telemetry';
  
  // Essential state only
  private vehiclesSubject = new BehaviorSubject<VehicleSummary[]>([]);
  private connectionStatusSubject = new BehaviorSubject<'connected' | 'disconnected' | 'error'>('disconnected');
  private loadingSubject = new BehaviorSubject<boolean>(true);
  
  // Simple pagination state
  private currentPage = 0;
  private totalVehicles = 0;
  private isSearching = false;
  
  // SSE connection management
  private currentEventSource: EventSource | null = null;
  
  // Essential observables only
  public vehicles$ = this.vehiclesSubject.asObservable();
  public connectionStatus$ = this.connectionStatusSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {
    // Load total vehicle count and start with first page
    this.loadTotalVehicleCount();
    this.loadPage(0); // Use the new pagination method
  }

  // Simple getters for pagination state
  getCurrentPage(): number { return this.currentPage; }
  getTotalVehicles(): number { return this.totalVehicles; }
  getTotalPages(): number { return Math.ceil(this.totalVehicles / 15); }
  getIsSearching(): boolean { return this.isSearching; }

  /**
   * Transform backend telemetry data to VehicleSummary format
   */
  private transformBackendDataToVehicleSummary(data: BackendTelemetryData): VehicleSummary {
    const now = Date.now();
    const dataTime = new Date(Number(data.key.timestamp) * 1000);
    const timeDiff = now - dataTime.getTime();
    
    // More realistic offline logic
    const isOnline = this.determineVehicleStatus(data.key.vehicleId, timeDiff);

    return {
      vehicleId: data.key.vehicleId,
      lastUpdate: dataTime,
      currentSpeed: data.speed,
      fuelLevel: data.fuelLevel,
      engineTemp: data.engineTemp,
      status: isOnline ? 'online' : 'offline',
      totalDistance: data.totalDistance || 0, // Default to 0 if not available
      latitude: data.latitude,
      longitude: data.longitude,
      tirePressure: data.tirePressure
    };
  }

  /**
   * Get all vehicle IDs from the backend
   */
  getAllVehicleIds(): Observable<string[]> {
    return this.http.get<string[]>(`${this.processingApiUrl}/vehicles`)
      .pipe(
        catchError(error => {
          console.error('Error fetching vehicle IDs:', error);
          return of([]);
        })
      );
  }

  /**
   * Get latest telemetry data for all vehicles
   */
  getAllVehicles(): Observable<VehicleSummary[]> {
    return this.getAllVehicleIds().pipe(
      switchMap(vehicleIds => {
        if (vehicleIds.length === 0) {
          return of([]);
        }
        
        // Get latest data for each vehicle
        const requests = vehicleIds.map(vehicleId => 
          this.getLatestVehicleTelemetry(vehicleId)
        );
        
        return Promise.all(requests).then(results => {
          const vehicles: VehicleSummary[] = [];
          
          results.forEach((data, index) => {
            if (data && data.length > 0) {
              const latest = data[0]; // Get the most recent record
              const now = new Date();
              // Convert Unix timestamp (seconds) to milliseconds for JavaScript Date
              const timestamp = Number(latest.key.timestamp);
              const dataTime = new Date(timestamp * 1000);
              const timeDiff = now.getTime() - dataTime.getTime();
              const isOnline = timeDiff < 300000; // 5 minutes threshold
              
              vehicles.push({
                vehicleId: latest.key.vehicleId,
                lastUpdate: dataTime,
                currentSpeed: latest.speed,
                fuelLevel: latest.fuelLevel,
                engineTemp: latest.engineTemp,
                status: isOnline ? 'online' : 'offline',
                totalDistance: 0, // Not available in current data model
                latitude: latest.latitude,
                longitude: latest.longitude,
                tirePressure: latest.tirePressure
              });
            }
          });
          
          this.vehiclesSubject.next(vehicles);
          return vehicles;
        });
      }),
      catchError(error => {
        console.error('Error fetching vehicles:', error);
        return of([]);
      })
    );
  }

  /**
   * Get latest telemetry data for a specific vehicle
   */
  getLatestVehicleTelemetry(vehicleId: string): Promise<BackendTelemetryData[]> {
    return this.http.get<BackendTelemetryData[]>(`${this.processingApiUrl}/vehicles/${vehicleId}/latest`)
      .pipe(
        catchError(error => {
          console.error(`Error fetching latest telemetry for vehicle ${vehicleId}:`, error);
          return of([]);
        })
      )
      .toPromise() as Promise<BackendTelemetryData[]>;
  }

  /**
   * Get telemetry data for a specific vehicle within a time range
   */
  getVehicleTelemetry(vehicleId: string, startTime?: string, endTime?: string): Observable<BackendTelemetryData[]> {
    let url = `${this.processingApiUrl}/vehicles/${vehicleId}`;
    const params = new URLSearchParams();
    
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    if (params.toString()) {
      url += `?${params.toString()}`;
    }

    return this.http.get<BackendTelemetryData[]>(url)
      .pipe(
        catchError(error => {
          console.error('Error fetching telemetry data:', error);
          return of([]);
        })
      );
  }

  /**
   * Start data simulation
   */
  startSimulation(): Observable<string> {
    return this.http.post<string>(`${this.ingestionApiUrl}/simulation/start`, {})
      .pipe(
        catchError(error => {
          console.error('Error starting simulation:', error);
          return of('Failed to start simulation');
        })
      );
  }

  /**
   * Stop data simulation
   */
  stopSimulation(): Observable<string> {
    return this.http.post<string>(`${this.ingestionApiUrl}/simulation/stop`, {})
      .pipe(
        catchError(error => {
          console.error('Error stopping simulation:', error);
          return of('Failed to stop simulation');
        })
      );
  }

  /**
   * Get simulation status
   */
  getSimulationStatus(): Observable<string> {
    return this.http.get<{status: string}>(`${this.ingestionApiUrl}/simulation/status`)
      .pipe(
        map(response => response.status),
        catchError(error => {
          console.error('Error getting simulation status:', error);
          return of('Unknown');
        })
      );
  }

  /**
   * Get detailed simulation info
   */
  getSimulationInfo(): Observable<string> {
    return this.http.get<string>(`${this.ingestionApiUrl}/simulation/info`)
      .pipe(
        catchError(error => {
          console.error('Error getting simulation info:', error);
          return of('Unknown');
        })
      );
  }

  /**
   * Connect to real-time SSE stream for specific vehicles (ESSENTIAL METHOD)
   */
  private connectToRealTimeStream(vehicleIds: string[]): void {
    if (!vehicleIds || vehicleIds.length === 0) {
      console.warn('⚠ No vehicle IDs provided for streaming');
      return;
    }
    
    // Create query string for vehicle IDs
    const vehicleIdsParam = vehicleIds.map(id => `vehicleIds=${encodeURIComponent(id)}`).join('&');
    const streamUrl = `${this.streamingApiUrl}/stream/vehicles?${vehicleIdsParam}`;
    
    console.log('✓ Connecting SSE to', vehicleIds.length, 'vehicles:', vehicleIds.slice(0, 3).join(', '), '...');
    this.currentEventSource = new EventSource(streamUrl);
    
    this.currentEventSource.onopen = () => {
      this.connectionStatusSubject.next('connected');
      console.log('✓ SSE connected to', vehicleIds.length, 'vehicles');
    };
    
    this.currentEventSource.onmessage = (event) => {
      try {
        const data: StreamingTelemetryData = JSON.parse(event.data);
        console.log('📡 SSE received data for vehicle:', data.vehicleId, 'speed:', data.speedKmh);
        this.transformAndUpdateVehicles(data);
      } catch (error) {
        console.error('✗ SSE parse error:', error);
        this.connectionStatusSubject.next('error');
      }
    };
    
    this.currentEventSource.onerror = (error) => {
      console.error('✗ SSE error:', error);
      this.connectionStatusSubject.next('error');
    };
  }

  /**
   * Transform streaming data to VehicleSummary and update vehicles (ESSENTIAL METHOD)
   */
  private transformAndUpdateVehicles(data: StreamingTelemetryData): void {
    const now = new Date();
    const dataTime = new Date(data.timestamp);
    const timeDiff = now.getTime() - dataTime.getTime();
    
    // More realistic offline logic
    const isOnline = this.determineVehicleStatus(data.vehicleId, timeDiff);
    
    const vehicleSummary: VehicleSummary = {
      vehicleId: data.vehicleId,
      lastUpdate: dataTime,
      currentSpeed: data.speedKmh,
      fuelLevel: data.fuelLevelPercentage,
      engineTemp: data.engineTemperatureCelsius,
      status: isOnline ? 'online' : 'offline',
      totalDistance: data.totalDistanceKm,
      latitude: data.latitude,
      longitude: data.longitude,
      tirePressure: data.tirePressurePsi
    };
    
    // Update existing vehicle in current page
    const currentVehicles = this.vehiclesSubject.value;
    const existingIndex = currentVehicles.findIndex(v => v.vehicleId === data.vehicleId);
    
    console.log('🔄 Updating vehicle:', data.vehicleId, 'found at index:', existingIndex, 'current vehicles:', currentVehicles.length);
    
    if (existingIndex >= 0) {
      // Update existing vehicle (this should always be the case for paginated view)
      currentVehicles[existingIndex] = vehicleSummary;
      console.log('✅ Updated vehicle at index', existingIndex, 'new speed:', vehicleSummary.currentSpeed);
      this.vehiclesSubject.next([...currentVehicles]);
    } else {
      console.log('⚠️ Vehicle', data.vehicleId, 'not found in current page, ignoring update');
    }
    // Ignore if vehicle not in current page
  }

  /**
   * Get real-time telemetry stream for a vehicle
   */
  getRealTimeStream(vehicleId: string): Observable<BackendTelemetryData> {
    return new Observable(observer => {
      const eventSource = new EventSource(`${this.streamingApiUrl}/vehicles/${vehicleId}/stream`);
      
      eventSource.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          observer.next(data);
        } catch (error) {
          console.error('Error parsing SSE data:', error);
        }
      };

      eventSource.onerror = (error) => {
        console.error('SSE error:', error);
        observer.error(error);
      };

      return () => {
        eventSource.close();
      };
    });
  }

  /**
   * Load total vehicle count for pagination
   */
  private loadTotalVehicleCount(): void {
    this.http.get<number>(`${this.processingApiUrl}/vehicles/count`).subscribe({
      next: (count) => {
        console.log('Total vehicle count:', count);
        this.totalVehicles = count;
      },
      error: (error) => {
        console.error('Error loading total vehicle count:', error);
        this.totalVehicles = 0;
      }
    });
  }

  /**
   * Load a specific page of vehicles (ESSENTIAL METHOD)
   */
  loadPage(pageNumber: number): void {
    console.log('Loading page:', pageNumber, 'with 15 vehicles');
    this.currentPage = pageNumber;
    this.isSearching = false;
    this.loadingSubject.next(true);
    
    this.http.get<BackendTelemetryData[]>(`${this.processingApiUrl}/vehicles/all/latest?page=${pageNumber}&size=15`).subscribe({
      next: (data) => {
        console.log('✓ Loaded page', pageNumber, ':', data.length, 'vehicles');
        const vehicles = data.map(item => this.transformBackendDataToVehicleSummary(item));
        this.vehiclesSubject.next(vehicles);
        this.loadingSubject.next(false);
        
        // Close old SSE and connect to ONLY these 15 vehicles
        this.closeCurrentSSEConnection();
        this.connectToRealTimeStream(vehicles.map(v => v.vehicleId));
      },
      error: (error) => {
        console.error('✗ Error loading page:', error);
        this.vehiclesSubject.next([]);
        this.loadingSubject.next(false);
      }
    });
  }

  /**
   * Search for vehicles by ID pattern (ESSENTIAL METHOD)
   */
  searchVehicles(vehicleId: string): void {
    if (!vehicleId.trim()) {
      this.clearSearch();
      return;
    }

    console.log('Searching for vehicles:', vehicleId);
    this.isSearching = true;
    this.loadingSubject.next(true);
    
    this.http.get<BackendTelemetryData[]>(`${this.processingApiUrl}/vehicles/search?vehicleId=${encodeURIComponent(vehicleId)}`).subscribe({
      next: (data) => {
        console.log('✓ Search found:', data.length, 'vehicles');
        const vehicles = data.map(item => this.transformBackendDataToVehicleSummary(item));
        this.vehiclesSubject.next(vehicles);
        this.loadingSubject.next(false);
        
        // Close old SSE and connect to ONLY search results
        this.closeCurrentSSEConnection();
        this.connectToRealTimeStream(vehicles.map(v => v.vehicleId));
      },
      error: (error) => {
        console.error('✗ Search error:', error);
        this.vehiclesSubject.next([]);
        this.loadingSubject.next(false);
      }
    });
  }

  /**
   * Clear search and return to paginated view
   */
  clearSearch(): void {
    console.log('Clearing search');
    this.isSearching = false;
    this.loadPage(this.currentPage);
  }

  /**
   * Navigate to next page (ESSENTIAL METHOD)
   */
  nextPage(): void {
    const totalPages = this.getTotalPages();
    if (this.currentPage < totalPages - 1) {
      this.loadPage(this.currentPage + 1);
    }
  }

  /**
   * Navigate to previous page (ESSENTIAL METHOD)
   */
  previousPage(): void {
    if (this.currentPage > 0) {
      this.loadPage(this.currentPage - 1);
    }
  }

  /**
   * Navigate to specific page
   */
  goToPage(page: number): void {
    const totalPages = this.getTotalPages();
    if (page >= 0 && page < totalPages) {
      this.loadPage(page);
    }
  }

  /**
   * Close current SSE connection (ESSENTIAL METHOD)
   */
  private closeCurrentSSEConnection(): void {
    if (this.currentEventSource) {
      console.log('✓ Closing SSE connection');
      this.currentEventSource.close();
      this.currentEventSource = null;
      this.connectionStatusSubject.next('disconnected');
    }
  }

  /**
   * Determine realistic vehicle status with max 5 vehicles offline at any time
   */
  private determineVehicleStatus(vehicleId: string, timeDiff: number): boolean {
    // Base timeout: 2 minutes (more realistic than 5 minutes)
    const baseTimeout = 120000; // 2 minutes
    
    // Create a deterministic "random" pattern based on vehicle ID
    const vehicleHash = this.hashString(vehicleId);
    const time = Date.now();
    
    // Check if data is too old first
    if (timeDiff > baseTimeout) {
      return false;
    }
    
    // Calculate which vehicles should be offline (max 5 at a time)
    const offlineVehicles = this.calculateOfflineVehicles(time);
    
    // Check if this specific vehicle should be offline
    return !offlineVehicles.includes(vehicleId);
  }

  /**
   * Calculate which vehicles should be offline (max 5 at any time across entire fleet)
   */
  private calculateOfflineVehicles(time: number): string[] {
    // Generate all possible vehicle IDs (VH001 to VH100)
    const allVehicleIds: string[] = [];
    for (let i = 1; i <= 100; i++) {
      allVehicleIds.push(`VH${i.toString().padStart(3, '0')}`);
    }
    
    // Create a deterministic rotation of offline vehicles across entire fleet
    const timeSlot = Math.floor(time / 20000); // 20-second slots
    const maxOffline = 5; // Max 5 vehicles offline across entire fleet
    
    // Select which vehicles should be offline in this time slot
    const offlineVehicles: string[] = [];
    
    for (let i = 0; i < maxOffline && i < allVehicleIds.length; i++) {
      // Use time-based rotation to select vehicles from entire fleet
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
