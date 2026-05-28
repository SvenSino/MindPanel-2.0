- Komponenten werden in Views geladen, Views werden im Router View angeziegt, Router konfiguiert, welcher Pfad für
  welche Route zuständig ist und lädt dynamisch rein
- Stores speichern den globalen Zustand, damit die Zustände der einzelnen Seiten nicht verloren gehen, wenn man wechselt
- directives -> spezielle attribute mit v-prefix
- erlauben itereieren, und conditional renderting etc
- ref() reaktive var
  Ohne :is (schlecht):
  <TodosWidget v-if="element.type === 'todos'" />
  <NotesWidget v-if="element.type === 'notes'" />
  <WeatherWidget v-if="element.type === 'weather'" />
  <CalendarWidget v-if="element.type === 'calendar'" />
  <PomodoroWidget v-if="element.type === 'pomodoro'" />
  <!-- Viel Code, schwer wartbar! -->

  Mit :is (gut):
  <component :is="widgetComponents[element.type]" />
  <!-- Eine Zeile, super clean! -->