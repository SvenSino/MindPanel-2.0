<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { searchCity, getWeather, getWeatherDescription } from '@/services/weatherService'
import type { GeocodingResult, WeatherData } from '@/services/weatherService'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'

const STORAGE_KEY = 'mindpanel_weather_city'

const citySearch = ref('')
const selectedCity = ref<GeocodingResult | null>(null)
const weatherData = ref<WeatherData | null>(null)
const searchResults = ref<GeocodingResult[]>([])
const loading = ref(false)
const error = ref('')

async function search() {
  if (!citySearch.value.trim()) return

  loading.value = true
  error.value = ''
  try {
    searchResults.value = await searchCity(citySearch.value)
  } catch (err) {
    error.value = 'Stadt konnte nicht gefunden werden'
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

async function selectCity(city: GeocodingResult) {
  selectedCity.value = city
  searchResults.value = []
  citySearch.value = ''

  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(city))
  }

  await loadWeather()
}

async function loadWeather() {
  if (!selectedCity.value) return

  loading.value = true
  error.value = ''
  try {
    weatherData.value = await getWeather(selectedCity.value.latitude, selectedCity.value.longitude)
  } catch (err) {
    error.value = 'Wetter konnte nicht geladen werden'
    weatherData.value = null
  } finally {
    loading.value = false
  }
}

function getWeatherIcon(code: number): string {
  // TODO: Custom Logos, eigentlich existieren deutlich mehr WetterCodes
  switch (true) {
    case code === 0:
      return 'pi-sun'
    case code >= 95:
      return 'pi-bolt'
    default:
      return 'pi-cloud'
  }
}


onMounted(async () => {
  if (typeof window !== 'undefined') {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      try {
        selectedCity.value = JSON.parse(saved)
        await loadWeather()
      } catch (e) {
        console.warn('Could not load saved city')
      }
    } else {
      // Set Dortmund as default city
      try {
        const dortmund = await searchCity('Dortmund')
        if (dortmund.length > 0) {
          await selectCity(dortmund[0])
        }
      } catch (e) {
        console.warn('Could not load default city')
      }
    }
  }
})
</script>

<template>
  <Card class="widget-card h-full">
    <template #header>
      <div class="widget-header drag-handle">
        <div class="flex align-items-center gap-2">
          <i class="pi pi-sun text-xl"></i>
          <h3 class="m-0 font-semibold">Wetter</h3>
        </div>
        <Button
          v-if="selectedCity"
          icon="pi pi-refresh"
          rounded
          text
          @click="loadWeather"
          v-tooltip.top="'Aktualisieren'"
        />
      </div>
    </template>

    <template #content>
      <div class="widget-content">
        <div v-if="!selectedCity" class="mb-3">
          <div class="flex gap-2">
            <InputText
              v-model="citySearch"
              placeholder="Stadt suchen..."
              @keyup.enter="search"
              class="flex-1"
            />
            <Button
              icon="pi pi-search"
              @click="search"
              :loading="loading"
            />
          </div>

          <div v-if="searchResults.length > 0" class="mt-3">
            <div
              v-for="city in searchResults"
              :key="city.id"
              class="search-result p-3 border-round cursor-pointer mb-2"
              @click="selectCity(city)"
            >
              <div class="font-semibold">{{ city.name }}</div>
              <div class="text-sm text-color-secondary">
                {{ city.country }}{{ city.admin1 ? `, ${city.admin1}` : '' }}
              </div>
            </div>
          </div>

          <div v-if="error" class="text-red-500 text-sm mt-2">
            <i class="pi pi-exclamation-triangle mr-1"></i>
            {{ error }}
          </div>
        </div>

        <div v-if="selectedCity && weatherData" class="text-center">
          <div class="mb-3">
            <h3 class="text-2xl font-bold m-0">{{ selectedCity.name }}</h3>
            <p class="text-sm text-color-secondary m-0">
              {{ selectedCity.country }}
            </p>
          </div>

          <div class="mb-4">
            <i :class="`pi ${getWeatherIcon(weatherData.weatherCode)}`" class="text-6xl text-primary mb-3"></i>
            <div class="text-5xl font-bold mb-2">{{ weatherData.temperature }}°C</div>
            <div class="text-lg text-color-secondary">
              {{ getWeatherDescription(weatherData.weatherCode) }}
            </div>
          </div>

          <div class="flex justify-content-center gap-4 text-sm">
            <div>
              <i class="pi pi-send mr-1"></i>
              {{ weatherData.windSpeed }} km/h
            </div>
          </div>

          <Button
            label="Stadt ändern"
            text
            size="small"
            class="mt-4"
            @click="selectedCity = null"
          />
        </div>

        <div v-if="loading && selectedCity" class="text-center py-6">
          <ProgressSpinner style="width: 50px; height: 50px" />
        </div>

        <div v-if="!selectedCity && !loading && searchResults.length === 0" class="text-center py-6">
          <i class="pi pi-map-marker text-4xl text-color-secondary mb-3"></i>
          <p class="text-color-secondary m-0">Suche nach einer Stadt</p>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.search-result {
  transition: all 0.2s ease;
  border: 1px solid var(--surface-border);
  background: var(--surface-card);
}

.search-result:hover {
  border-color: var(--primary-color);
  background: var(--surface-hover);
  transform: translateY(-1px);
}
</style>
