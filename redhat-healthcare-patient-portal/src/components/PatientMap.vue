<script setup lang="ts">
import { onMounted, onUnmounted, watch, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

interface Patient {
  id?: string
  name?: Array<{
    family?: string
    given?: string[]
  }>
  address?: Array<{
    city?: string
    state?: string
    line?: string[]
    postalCode?: string
  }>
}

interface CityData {
  coords: [number, number]
  county: string
}

interface Props {
  patients: Patient[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  selectCity: [city: string]
  selectCounty: [cities: string[]]
}>()

const mapContainer = ref<HTMLElement | null>(null)
let map: L.Map | null = null
let countiesLayer: L.GeoJSON | null = null
let layerControl: L.Control.Layers | null = null

// NC city coordinates with county mappings
const NC_COORDINATES: Record<string, CityData> = {
  'Charlotte': { coords: [35.2271, -80.8431], county: 'Mecklenburg' },
  'Raleigh': { coords: [35.7796, -78.6382], county: 'Wake' },
  'Durham': { coords: [35.9940, -78.8986], county: 'Durham' },
  'Greensboro': { coords: [36.0726, -79.7920], county: 'Guilford' },
  'Winston-Salem': { coords: [36.0999, -80.2442], county: 'Forsyth' },
  'Fayetteville': { coords: [35.0527, -78.8784], county: 'Cumberland' },
  'Cary': { coords: [35.7915, -78.7811], county: 'Wake' },
  'Wilmington': { coords: [34.2257, -77.9447], county: 'New Hanover' },
  'Asheville': { coords: [35.5951, -82.5515], county: 'Buncombe' },
  'Chapel Hill': { coords: [35.9132, -79.0558], county: 'Orange' },
  'Boston': { coords: [42.3601, -71.0589], county: 'Out of State' } // For Vincent
}

function getPatientName(patient: Patient): string {
  if (!patient.name || patient.name.length === 0) return 'Unknown'
  const name = patient.name[0]
  if (!name) return 'Unknown'
  return `${name.given?.join(' ') || ''} ${name.family || ''}`.trim()
}

async function loadCountyBoundaries(): Promise<any | null> {
  try {
    const response = await fetch('/data/nc-counties.geojson')
    if (!response.ok) {
      console.error('Failed to load county boundaries:', response.statusText)
      return null
    }
    return await response.json()
  } catch (error) {
    console.error('Error loading county boundaries:', error)
    return null
  }
}

function getCitiesInCounty(countyName: string): string[] {
  return Object.entries(NC_COORDINATES)
    .filter(([_, data]) => data.county === countyName)
    .map(([city]) => city)
}

function handleCountyClick(countyName: string) {
  const cities = getCitiesInCounty(countyName)
  if (cities.length > 0) {
    emit('selectCounty', cities)
  }
}

function getCountyPatientCount(countyName: string): number {
  return props.patients.filter(patient => {
    const city = patient.address?.[0]?.city
    if (!city || !NC_COORDINATES[city]) return false
    return NC_COORDINATES[city].county === countyName
  }).length
}

function createCountyLayer(geojsonData: any): L.GeoJSON {
  return L.geoJSON(geojsonData, {
    style: {
      color: '#667eea',
      weight: 2,
      opacity: 0.6,
      fillColor: '#667eea',
      fillOpacity: 0.05
    },
    onEachFeature: (feature, layer) => {
      const countyName = feature.properties.NAME || feature.properties.name
      const patientCount = getCountyPatientCount(countyName)

      // Bind tooltip with patient count
      const tooltipContent = patientCount > 0
        ? `${countyName}<br><strong>${patientCount} patient${patientCount !== 1 ? 's' : ''}</strong>`
        : countyName

      layer.bindTooltip(tooltipContent, {
        permanent: false,
        direction: 'center',
        className: 'county-tooltip'
      })

      // Click handler
      layer.on('click', () => handleCountyClick(countyName))

      // Hover effects (cast to Path to access setStyle)
      const pathLayer = layer as L.Path
      layer.on('mouseover', () => {
        pathLayer.setStyle({ fillOpacity: 0.15, weight: 3 })
      })
      layer.on('mouseout', () => {
        pathLayer.setStyle({ fillOpacity: 0.05, weight: 2 })
      })
    }
  })
}

async function initializeMap() {
  if (!mapContainer.value || map) return

  // Create map centered on North Carolina
  map = L.map(mapContainer.value).setView([35.7596, -79.0193], 7)

  // Add OpenStreetMap tiles
  const osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
  }).addTo(map)

  // Load and add county boundaries
  const countyData = await loadCountyBoundaries()
  if (countyData) {
    countiesLayer = createCountyLayer(countyData)
    countiesLayer.addTo(map)

    // Add layer control with only county boundaries
    const overlays = {
      'County Boundaries': countiesLayer
    }
    layerControl = L.control.layers(undefined, overlays, {
      collapsed: false
    }).addTo(map)
  }
}

onMounted(() => {
  initializeMap()
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
    countiesLayer = null
    layerControl = null
  }
})

// Watch for patient changes and refresh county layer
watch(() => props.patients, async () => {
  if (!map || !countiesLayer) return

  // Remove old county layer
  map.removeLayer(countiesLayer)

  // Reload county boundaries with updated patient counts
  const countyData = await loadCountyBoundaries()
  if (countyData) {
    countiesLayer = createCountyLayer(countyData)
    countiesLayer.addTo(map)

    // Update layer control
    if (layerControl) {
      map.removeControl(layerControl)
      const overlays = {
        'County Boundaries': countiesLayer
      }
      layerControl = L.control.layers(undefined, overlays, {
        collapsed: false
      }).addTo(map)
    }
  }
}, { deep: true })
</script>

<template>
  <div class="map-container">
    <div ref="mapContainer" class="map"></div>
  </div>
</template>

<style scoped>
.map-container {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
}

.map {
  width: 100%;
  height: 100%;
}

:deep(.county-tooltip) {
  background: white;
  border: 2px solid #667eea;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 0.875rem;
  font-weight: 600;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  text-align: center;
}

:deep(.leaflet-control-layers) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
