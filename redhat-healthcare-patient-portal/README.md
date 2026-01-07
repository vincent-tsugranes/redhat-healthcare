# Red Hat Healthcare Patient Portal

A modern Vue.js patient portal application that allows patients to view their healthcare information, including personal details, insurance coverage, and medical claims. Built with Vue 3, TypeScript, Pinia, and Vue Router, this application integrates with FHIR R4 compliant backend services.

## Features

- **Patient Selection**: Browse and select from multiple patient profiles
- **Personal Information**: View comprehensive patient demographic data including:
  - Personal details (name, gender, birth date, marital status)
  - Identifiers and medical record numbers
  - Contact information (addresses, phone numbers, emails)
  - Preferred languages
- **Insurance Coverage**: Display detailed coverage information including:
  - Coverage status and validity periods
  - Subscriber and payor information
  - Plan details and coverage classes
  - Cost-sharing information
- **Claims Management**: View submitted claims with:
  - Claim status and lifecycle tracking
  - Financial totals and breakdowns
  - Diagnoses and procedures
  - Line-item details
  - Provider and insurer information

## Technology Stack

- **Frontend Framework**: Vue 3 with Composition API
- **Language**: TypeScript
- **State Management**: Pinia
- **Routing**: Vue Router
- **HTTP Client**: Axios
- **Build Tool**: Vite
- **Standards**: FHIR R4

## Architecture

The application follows a layered architecture pattern:

```
src/
├── views/           # Page components (HomeView, PatientView, CoverageView, ClaimsView)
├── stores/          # Pinia stores for state management
├── services/        # API service layer
│   ├── api.ts       # Axios instance configuration
│   └── fhirService.ts  # FHIR resource services
├── router/          # Vue Router configuration
└── App.vue          # Main application layout
```

### API Integration

The portal integrates with three FHIR R4 backend services:

- **Patient Service** (http://localhost:8080/fhir) - Patient demographic data
- **Coverage Service** (http://localhost:8081/fhir) - Insurance coverage information
- **Claims Service** (http://localhost:8082/fhir) - Medical claims data

## Prerequisites

- Node.js 22+ and npm
- Running FHIR backend services:
  - redhat-healthcare-patients (port 8080)
  - redhat-healthcare-patients-coverage (port 8081)
  - redhat-healthcare-claims (port 8082)

## Project Setup

Install dependencies:

```sh
npm install
```

## Development

### Start Development Server

Run the application in development mode with hot-reload:

```sh
npm run dev
```

The application will be available at http://localhost:5173

### Type Checking

Run TypeScript type checking:

```sh
npm run type-check
```

### Linting

Lint and fix code:

```sh
npm run lint
```

## Production Build

Build for production:

```sh
npm run build
```

The optimized production build will be in the `dist/` directory.

Preview production build locally:

```sh
npm run preview
```

## Usage

1. **Start Backend Services**: Ensure all three FHIR backend services are running
2. **Start Portal**: Run `npm run dev`
3. **Select Patient**: Navigate to the home page and select a patient profile
4. **View Information**: Use the navigation menu to switch between:
   - Home - Patient selection
   - My Information - Personal details
   - My Coverage - Insurance coverage
   - My Claims - Medical claims

## API Configuration

API endpoints are configured in `src/services/api.ts`. To change the backend URLs, update:

```typescript
export const PATIENT_API_URL = 'http://localhost:8080/fhir'
export const COVERAGE_API_URL = 'http://localhost:8081/fhir'
export const CLAIMS_API_URL = 'http://localhost:8082/fhir'
```

## State Management

The application uses Pinia for state management. The main store (`patient.ts`) manages:

- Current patient selection
- Patient list
- Coverage data
- Claims data
- Loading states
- Error handling

## Development Tools

### Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (disable Vetur if installed)

### Browser Extensions

- Chromium (Chrome, Edge, Brave):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)
  - [Custom Object Formatter](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Custom Object Formatter](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Troubleshooting

### Backend Connection Issues

If the portal cannot connect to backend services:

1. Verify all three backend services are running
2. Check the console for CORS errors
3. Ensure backend URLs in `api.ts` match your setup
4. Verify PostgreSQL databases are running for each service

### No Patients Displayed

1. Check that the Patient service has data
2. Verify the API endpoint is correct
3. Check browser console for errors
4. Ensure the backend service is running on port 8080

## License

Copyright 2024 Red Hat Healthcare
