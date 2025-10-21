# Vehicle Telemetry Dashboard - Angular Frontend

A modern, responsive Angular dashboard for visualizing real-time vehicle telemetry data. Built with Angular 17+ and Angular Material for a clean, professional interface.

## Features

- **Real-time Dashboard**: Live monitoring of vehicle telemetry data
- **Vehicle Overview**: Summary cards showing key metrics for all vehicles
- **Detailed Analytics**: Individual vehicle telemetry with trend analysis
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices
- **Modern UI**: Clean Material Design interface with intuitive navigation
- **Real-time Updates**: Automatic data refresh and live streaming capabilities

## Technology Stack

- **Angular 17+**: Latest Angular framework with standalone components
- **Angular Material**: Material Design components and theming
- **TypeScript**: Type-safe development
- **RxJS**: Reactive programming for data streams
- **SCSS**: Enhanced CSS with variables and mixins
- **Docker**: Containerized deployment
- **Nginx**: Production web server

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── dashboard/           # Main dashboard component
│   │   │   ├── vehicle-details/     # Vehicle detail view
│   │   │   └── telemetry-chart/     # Chart visualization component
│   │   ├── services/
│   │   │   └── telemetry.service.ts # API integration service
│   │   ├── app.component.*          # Root component
│   │   └── app.config.ts            # App configuration
│   ├── styles.css                   # Global styles and custom theme
│   └── main.ts                      # Application bootstrap
├── Dockerfile                       # Container configuration
├── nginx.conf                       # Nginx configuration
└── package.json                     # Dependencies and scripts
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Angular CLI 17+
- Backend services running (API Gateway on port 8080)

### Development Setup

1. **Install dependencies**:

   ```bash
   npm install
   ```

2. **Start development server**:

   ```bash
   npm start
   # or
   ng serve
   ```

3. **Access the application**:
   - Open http://localhost:4200 in your browser
   - The dashboard will connect to the backend API at http://localhost:8080

### Building for Production

```bash
# Build the application
npm run build

# The built files will be in dist/frontend/
```

## Docker Deployment

### Build and Run with Docker

```bash
# Build the Docker image
docker build -t vehicle-telemetry/frontend-dashboard .

# Run the container
docker run -p 4200:80 vehicle-telemetry/frontend-dashboard
```

### Using Docker Compose

The frontend is included in the main Docker Compose configuration:

```bash
# Start the entire system including frontend
cd infrastructure/docker
docker-compose up frontend-dashboard
```

## API Integration

The frontend integrates with the following backend endpoints:

- `GET /api/telemetry/vehicles` - Get all vehicles summary
- `GET /api/telemetry/vehicles/{id}` - Get vehicle telemetry data
- `GET /api/telemetry/vehicles/{id}/stream` - Real-time data stream (SSE)

### Configuration

Update the API URL in `src/app/services/telemetry.service.ts`:

```typescript
private apiUrl = 'http://localhost:8080/api/telemetry';
```

## Components Overview

### Dashboard Component

- **Purpose**: Main landing page showing vehicle overview
- **Features**:
  - Vehicle summary cards with key metrics
  - Real-time status indicators
  - Quick actions and navigation
  - Responsive grid layout

### Vehicle Details Component

- **Purpose**: Detailed view of individual vehicle telemetry
- **Features**:
  - Current status metrics
  - Historical data trends
  - Location information
  - Tabbed interface for different data views

### Telemetry Chart Component

- **Purpose**: Visual representation of telemetry trends
- **Features**:
  - Simple bar chart visualization
  - Real-time data updates
  - Statistical summaries (min, max, average)
  - Trend indicators

## Styling and Theming

The application uses Angular Material with a custom theme:

- **Primary Color**: Blue (#1976d2)
- **Accent Color**: Amber (#ff9800)
- **Warn Color**: Red (#f44336)
- **Typography**: Roboto font family
- **Responsive**: Mobile-first design approach

## Performance Optimizations

- **Lazy Loading**: Components loaded on demand
- **OnPush Change Detection**: Optimized change detection strategy
- **HTTP Interceptors**: Centralized error handling and loading states
- **Caching**: Intelligent data caching for better performance
- **Bundle Optimization**: Tree-shaking and code splitting

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Development Guidelines

### Code Style

- Use TypeScript strict mode
- Follow Angular style guide
- Implement proper error handling
- Write unit tests for components and services

### Component Architecture

- Use standalone components
- Implement proper lifecycle management
- Use reactive programming with RxJS
- Follow single responsibility principle

### Testing

```bash
# Run unit tests
npm test

# Run e2e tests
npm run e2e
```

## Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure backend API has proper CORS configuration
2. **API Connection**: Verify backend services are running on correct ports
3. **Build Errors**: Check Node.js and Angular CLI versions
4. **Docker Issues**: Ensure Docker daemon is running and has sufficient resources

### Debug Mode

Enable debug logging in the telemetry service:

```typescript
// In telemetry.service.ts
console.log('API Response:', data);
```

## Contributing

1. Follow the established code style
2. Write meaningful commit messages
3. Add tests for new features
4. Update documentation as needed

## License

This project is part of the Vehicle Telemetry System and follows the same licensing terms.
