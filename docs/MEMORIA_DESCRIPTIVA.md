# Memoria Descriptiva — EcoDepuradora: Misión Agua Limpia

## 1. Visión del producto

EcoDepuradora es un simulador de construcción y gestión ("tycoon") pensado
para niños de 8 a 12 años, en el que el jugador asume el rol de aprendiz de
ingeniero ambiental junto a **Berto**, un castor ingeniero con casco. El
objetivo declarado no es enseñar mediante cuestionarios, sino a través de
**construir, ajustar y observar**: el niño arma físicamente (drag & drop)
una planta de tratamiento, maneja sus controles y ve el resultado como agua
que cambia de color de marrón turbio a azul cristalino.

## 2. Evaluación de experiencia (regla 45 de la Especificación Maestra)

Aplicando el checklist obligatorio con un usuario ficticio de 10 años:

| Pregunta | Respuesta de diseño |
|---|---|
| ¿Me gustaría abrir esta app mañana? | Sí: el Mapa de la Región muestra zonas nuevas por limpiar y un indicador de Salud del Agua Global que invita a seguir subiendo. |
| ¿Entiendo qué debo hacer? | Sí: Berto presenta cada zona con una frase breve ("¡Demasiada basura plástica!") antes de cada reto. |
| ¿Tengo algo que descubrir? | Sí: piezas nuevas, planos de ingeniería y zonas del mapa se desbloquean progresivamente. |
| ¿Me siento recompensado? | Sí: feedback visual inmediato (tanque de agua cambiando de color), planos e insignias ligados a logros reales. |
| ¿Hay algo que puedo coleccionar? | Sí: 10 Planos de Ingeniería y 8 Insignias Eco-Ingeniero en la Oficina del Castor. |
| ¿La app parece hecha para mí? | Sí: estética "tycoon" isométrica/2D, sin infantilización excesiva, con retos de ingeniería reales adaptados. |

## 3. Los primeros 30 segundos

1. Splash: una gota de agua sucia recorre una tubería y sale brillante,
   formando el logotipo "EcoDepuradora".
2. Onboarding de 3 pantallas (bienvenida de Berto → cómo se juega → elegir
   apodo y avatar), sin pedir datos personales reales.
3. El niño llega directamente al Mapa de la Región y ve la primera zona
   contaminada, "Río del Bosque", disponible para jugar.

## 4. Identidad visual

- **Paleta:** marrón río (`#8D6E45`) → azul cristalino (`#1CA9C9`), acentos
  verdes (`#4CAF7D`) y amarillo sol (`#FFC94D`). Paleta propia, no
  reutilizada de otros proyectos educativos.
- **Personaje guía:** Berto, castor con casco de ingeniero, dibujado
  íntegramente con Jetpack Compose Canvas (sin imágenes externas).
- **Iconografía diferenciada por pieza de maquinaria:** cada una de las 12
  piezas (rejas, desarenador, decantadores, biorreactor, cámara UV, etc.)
  tiene su propio glifo vectorial generado por código.
- **Mundo visual:** Mapa de la Región como tablero principal con tarjetas de
  zona (no lista de botones plana), Panel de Ingeniería con la planta en la
  mitad superior y el inventario de piezas arrastrables en la inferior.

## 5. Mecánicas educativas (regla 3 del prompt específico)

Ninguna mecánica principal es un cuestionario de opción múltiple:

1. **Constructor de Planta (drag & drop real):** el niño arrastra con el
   dedo cada pieza desde el inventario hasta la línea de ensamblaje. Si el
   orden no es el correcto, el agua se estanca visualmente y Berto explica
   qué pieza va antes.
2. **Operador de Válvulas:** dos sliders (oxígeno, velocidad del agua)
   dibujados sobre un Canvas con burbujas animadas; el motor de simulación
   (`SimulateWaterFlowUseCase`) penaliza tanto el exceso como el defecto.
3. **Laboratorio de Bacterias:** vista de "microscopio" en la que se toca la
   materia orgánica para que las bacterias la consuman antes de que se
   acabe el cronómetro.

La calidad final del agua de cada nivel combina las tres mecánicas
(`CalculateFinalQualityUseCase`), y determina el color del agua, si se
desbloquea el Plano de Ingeniería de la zona y qué insignias se otorgan.

## 6. Progresión y contenido semilla

- 3 etapas: Primario, Secundario, Terciario.
- 12 piezas de maquinaria (4 por etapa).
- 10 niveles/retos distribuidos en las 3 etapas (4 + 3 + 3).
- 10 Planos de Ingeniería coleccionables.
- 8 Insignias Eco-Ingeniero, ligadas a condiciones reales de progreso
  (completar niveles, dominar una etapa, lograr 100% de calidad, etc.).

Todo el contenido se siembra una única vez en Room la primera vez que se
abre la app (`GameRepositoryImpl.ensureSeeded()`), de forma 100% local.

## 7. Lo que se simplificó y por qué (regla de simplificación)

- El "microscopio" del laboratorio de bacterias coloca la materia orgánica
  en posiciones semi-aleatorias dentro de un contenedor circular en lugar de
  una simulación física de partículas con colisiones; se prioriza la
  claridad y el rendimiento en gama baja de dispositivos sobre el realismo
  físico. La mecánica de "tocar antes de que se acabe el tiempo" se
  mantiene íntegra y con lógica real y testeada.
- No se implementó sonido con archivos de audio reales (serían binarios
  fuera del alcance de este entregable de código fuente); la arquitectura
  ya contempla el ajuste `soundEnabled` en el perfil del jugador para que
  un desarrollador añada los `SoundPool`/`MediaPlayer` correspondientes sin
  cambiar la lógica de juego.
