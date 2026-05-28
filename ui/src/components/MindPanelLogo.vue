<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  size?: 'small' | 'medium' | 'large'
  showText?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  showText: true
})

const dimensions = computed(() => {
  switch (props.size) {
    case 'small':
      return { width: 32, height: 32, fontSize: '1.25rem' }
    case 'large':
      return { width: 64, height: 64, fontSize: '2.5rem' }
    default:
      return { width: 40, height: 40, fontSize: '1.5rem' }
  }
})
</script>

<template>
  <div class="mindpanel-logo" :class="`logo-${size}`">
    <svg
      :width="dimensions.width"
      :height="dimensions.height"
      viewBox="0 0 100 100"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <!-- Gradient Definitions -->
      <defs>
        <linearGradient id="brainGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color: #11c6c6; stop-opacity: 1" />
          <stop offset="100%" style="stop-color: #229bb6; stop-opacity: 1" />
        </linearGradient>
        <linearGradient id="brainGlow" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color: #11c6c6; stop-opacity: 0.3" />
          <stop offset="100%" style="stop-color: #229bb6; stop-opacity: 0.1" />
        </linearGradient>
      </defs>

      <!-- Cooler Gehirn-Umriss mit organischen Kurven -->
      <!-- Linke Hemisphäre -->
      <path
        d="M 30 25
           Q 20 25, 15 35
           Q 10 45, 12 55
           Q 14 65, 20 72
           Q 25 78, 35 82
           Q 40 84, 45 83
           L 45 80
           Q 38 78, 33 72
           Q 28 66, 26 58
           Q 25 50, 28 42
           Q 30 35, 35 30
           Z"
        fill="url(#brainGradient)"
        stroke="url(#brainGradient)"
        stroke-width="1.5"
      />

      <!-- Rechte Hemisphäre -->
      <path
        d="M 70 25
           Q 80 25, 85 35
           Q 90 45, 88 55
           Q 86 65, 80 72
           Q 75 78, 65 82
           Q 60 84, 55 83
           L 55 80
           Q 62 78, 67 72
           Q 72 66, 74 58
           Q 75 50, 72 42
           Q 70 35, 65 30
           Z"
        fill="url(#brainGradient)"
        stroke="url(#brainGradient)"
        stroke-width="1.5"
      />

      <!-- Gehirn-Furchen (Gyri) - linke Seite -->
      <path
        d="M 22 38 Q 25 35, 28 38"
        stroke="url(#brainGradient)"
        stroke-width="2"
        fill="none"
        opacity="0.6"
        stroke-linecap="round"
      />
      <path
        d="M 20 48 Q 24 45, 30 48"
        stroke="url(#brainGradient)"
        stroke-width="2.5"
        fill="none"
        opacity="0.7"
        stroke-linecap="round"
      />
      <path
        d="M 24 58 Q 28 54, 32 58"
        stroke="url(#brainGradient)"
        stroke-width="2"
        fill="none"
        opacity="0.6"
        stroke-linecap="round"
      />
      <path
        d="M 28 68 Q 32 65, 36 68"
        stroke="url(#brainGradient)"
        stroke-width="2"
        fill="none"
        opacity="0.5"
        stroke-linecap="round"
      />

      <!-- Gehirn-Furchen (Gyri) - rechte Seite -->
      <path
        d="M 78 38 Q 75 35, 72 38"
        stroke="url(#brainGradient)"
        stroke-width="2"
        fill="none"
        opacity="0.6"
        stroke-linecap="round"
      />
      <path
        d="M 80 48 Q 76 45, 70 48"
        stroke="url(#brainGradient)"
        stroke-width="2.5"
        fill="none"
        opacity="0.7"
        stroke-linecap="round"
      />
      <path
        d="M 76 58 Q 72 54, 68 58"
        stroke="url(#brainGradient)"
        stroke-width="2"
        fill="none"
        opacity="0.6"
        stroke-linecap="round"
      />
      <path
        d="M 72 68 Q 68 65, 64 68"
        stroke="url(#brainGradient)"
        stroke-width="2"
        fill="none"
        opacity="0.5"
        stroke-linecap="round"
      />

      <!-- Corpus Callosum (Verbindung zwischen Hemisphären) -->
      <ellipse
        cx="50"
        cy="55"
        rx="8"
        ry="25"
        fill="url(#brainGlow)"
        opacity="0.4"
      />

      <!-- Neuronale Verbindungen / Synapsen -->
      <circle cx="35" cy="45" r="3" fill="url(#brainGradient)" opacity="0.8">
        <animate attributeName="opacity" values="0.5;1;0.5" dur="2s" repeatCount="indefinite" />
      </circle>
      <circle cx="65" cy="45" r="3" fill="url(#brainGradient)" opacity="0.8">
        <animate attributeName="opacity" values="1;0.5;1" dur="2s" repeatCount="indefinite" />
      </circle>
      <circle cx="50" cy="35" r="2.5" fill="url(#brainGradient)" opacity="0.7">
        <animate attributeName="opacity" values="0.4;0.9;0.4" dur="1.5s" repeatCount="indefinite" />
      </circle>

      <!-- Äußerer Glow-Effekt -->
      <ellipse
        cx="50"
        cy="55"
        rx="45"
        ry="35"
        fill="none"
        stroke="url(#brainGlow)"
        stroke-width="3"
        opacity="0.2"
      />
    </svg>

    <span v-if="showText" class="logo-text" :style="{ fontSize: dimensions.fontSize }">
      MindPanel
    </span>
  </div>
</template>

<style scoped>
.mindpanel-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-text {
  font-weight: 700;
  background: linear-gradient(135deg, #11c6c6 0%, #229bb6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.logo-small .logo-text {
  font-size: 1.25rem;
}

.logo-medium .logo-text {
  font-size: 1.5rem;
}

.logo-large .logo-text {
  font-size: 2.5rem;
}

svg {
  flex-shrink: 0;
}
</style>
