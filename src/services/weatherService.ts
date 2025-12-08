import axios from 'axios'

const GEOCODING_API = 'https://geocoding-api.open-meteo.com/v1/search'
const FORECAST_API = 'https://api.open-meteo.com/v1/forecast'

export interface GeocodingResult {
  id: number
  name: string
  latitude: number
  longitude: number
  country: string
  admin1?: string
}

export interface WeatherData {
  temperature: number
  weatherCode: number
  windSpeed: number
}

const weatherCodeDescriptions: Record<number, string> = {
  0: 'Klar',
  1: 'Hauptsächlich klar',
  2: 'Teilweise bewölkt',
  3: 'Bewölkt',
  45: 'Nebelig',
  48: 'Nebelig',
  51: 'Leichter Nieselregen',
  53: 'Nieselregen',
  55: 'Starker Nieselregen',
  61: 'Leichter Regen',
  63: 'Regen',
  65: 'Starker Regen',
  71: 'Leichter Schnee',
  73: 'Schnee',
  75: 'Starker Schnee',
  77: 'Schneegriesel',
  80: 'Leichte Regenschauer',
  81: 'Regenschauer',
  82: 'Starke Regenschauer',
  85: 'Leichte Schneeschauer',
  86: 'Schneeschauer',
  95: 'Gewitter',
  96: 'Gewitter mit Hagel',
  99: 'Gewitter mit Hagel',
}

export function getWeatherDescription(code: number): string {
  return weatherCodeDescriptions[code] || 'Unbekannt'
}

export async function searchCity(query: string): Promise<GeocodingResult[]> {
  try {
    const response = await axios.get(GEOCODING_API, {
      params: {
        name: query,
        count: 5,
        language: 'de',
        format: 'json',
      },
    })
    return response.data.results || []
  } catch (error) {
    console.error('Geocoding error:', error)
    throw new Error('Stadt konnte nicht gefunden werden')
  }
}

export async function getWeather(latitude: number, longitude: number): Promise<WeatherData> {
  try {
    const response = await axios.get(FORECAST_API, {
      params: {
        latitude,
        longitude,
        current_weather: true,
        timezone: 'auto',
      },
    })

    const current = response.data.current_weather
    return {
      temperature: Math.round(current.temperature),
      weatherCode: current.weathercode,
      windSpeed: Math.round(current.windspeed),
    }
  } catch (error) {
    console.error('Weather fetch error:', error)
    throw new Error('Wetter konnte nicht abgerufen werden')
  }
}
