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
  private ingestionApiUrl = 'http://localhost:8081/api';
  private streamingApiUrl = 'http://localhost:8083/api/telemetry';
  
  private vehiclesSubject = new BehaviorSubject<VehicleSummary[]>([]);
  private telemetrySubject = new BehaviorSubject<BackendTelemetryData[]>([]);
  
  public vehicles$ = this.vehiclesSubject.asObservable();
  public telemetryData$ = this.telemetrySubject.asObservable();

  constructor(private http: HttpClient) {
    console.log('BackendDataService constructor called');
    // Start polling for real-time updates
    this.startPolling();
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
        map(data => {
          this.telemetrySubject.next(data);
          return data;
        }),
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
   * Start polling for real-time updates
   */
  private startPolling(): void {
    console.log('Starting polling for vehicle data...');
    // Poll every 10 seconds for vehicle summaries
    setInterval(() => {
      console.log('Polling for vehicle data...');
      this.getAllVehicles().subscribe({
        next: (vehicles) => console.log('Received vehicles:', vehicles.length),
        error: (error) => console.error('Error polling vehicles:', error)
      });
    }, 10000);
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
}
