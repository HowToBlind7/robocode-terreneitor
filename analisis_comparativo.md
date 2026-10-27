# Análisis Comparativo: Java vs Kotlin — Competencia de Bots

## 1. Expresividad del código

La expresividad se refiere a cuánta intención se puede comunicar con la menor cantidad de código posible. Kotlin supera a Java en este aspecto de forma consistente en todo el proyecto.

### Sealed interfaces

En Java, el `sealed interface` requiere declarar explícitamente todos los subtipos permitidos con la cláusula `permits`:

```java
// Java — Strategy.java
public sealed interface Strategy permits SimpleRandomStrategy, TurretStrategy, WallStrategy {
    // ...
}
```

En Kotlin, el compilador infiere automáticamente los subtipos permitidos dentro del mismo paquete/módulo, eliminando redundancia:

```kotlin
// Kotlin — Strategy.kt
sealed interface Strategy {
    // ...
}
```

Ambas versiones logran el mismo objetivo (restringir las implementaciones posibles), pero Kotlin lo hace sin necesidad de enumerar manualmente cada subtipo. Esto también reduce el riesgo de errores al agregar una nueva implementación y olvidar actualizar la cláusula `permits`.

### Singleton (patrón Singleton)

La diferencia más notoria en expresividad se encuentra en los estrategas. En Java, implementar un Singleton requiere un patrón de 4 partes: campo estático, constructor privado, método `getInstance()` y la propia clase:

```java
// Java — Strategist.java (11 líneas de boilerplate por singleton)
class MainStrategist implements Strategist {
    private static final MainStrategist INSTANCE = new MainStrategist();
    private MainStrategist() {}
    public static MainStrategist getInstance() { return INSTANCE; }

    @Override
    public Strategy decideStrategy(JuniorRobot robot) { ... }
}
```

En Kotlin, la palabra clave `object` reemplaza todo ese boilerplate con una declaración nativa del lenguaje:

```kotlin
// Kotlin — Strategist.kt (sin boilerplate + expression body)
object MainStrategist : Strategist {
    private val wallStrategy = WallStrategy()

    override fun decideStrategy(robot: JuniorRobot) =
        if (robot.energy <= 5) TurretStrategy else wallStrategy
}
```

Kotlin reduce el Singleton de ~11 líneas a ~3 líneas, sin sacrificar funcionalidad. El `object` de Kotlin es thread-safe por diseño y garantizado por la JVM, al igual que el patrón `static final` de Java. Además, al usar **expression body** (`= if ...` en lugar de `{ return if ... }`), las funciones cortas se expresan de forma más directa.

### Records vs Data Classes / Clases simples

Java introduce los `record` como clases inmutables con `equals()`, `hashCode()` y `toString()` generados automáticamente. Kotlin ofrece `data class` para el mismo propósito, pero además permite usar clases simples con valores por defecto en el constructor primario, eliminando la necesidad de constructores secundarios.

En este proyecto, `SimpleRandomStrategy` no necesita comparación por valor ni `copy()`, por lo que se modela como una clase regular con un parámetro por defecto:

```java
// Java — SimpleRandomStrategy.java (requiere constructor secundario)
public record SimpleRandomStrategy(Random rng) implements Strategy {
    public SimpleRandomStrategy() {
        this(new Random());
    }
}
```

```kotlin
// Kotlin — SimpleRandomStrategy.kt (clase simple con valor por defecto)
class SimpleRandomStrategy(private val rng: Random = Random()) : Strategy {
    // No se necesita constructor secundario ni data class
}
```

Por su parte, `TurretStrategy` no tiene estado alguno, por lo que en Kotlin se modela directamente como `object` (singleton), evitando tanto el `record` de Java como el hack de usar `data class` con un parámetro ficticio:

```java
// Java — TurretStrategy.java (record sin estado real)
public record TurretStrategy() implements Strategy { ... }
```

```kotlin
// Kotlin — TurretStrategy.kt (singleton natural)
object TurretStrategy : Strategy { ... }
```

---

## 2. Simplicidad sintáctica

La simplicidad sintáctica se refiere a cuánta ceremonia o "ruido" sintáctico exige el lenguaje para expresar una idea.

### Inferencia de tipos

Java requiere declarar el tipo en ambos lados de una asignación (o usar `var` desde Java 10, pero solo en variables locales). Kotlin infiere el tipo automáticamente en todos los contextos:

```java
// Java
private final Strategy wallStrategy = new WallStrategy();
int currentEnergy = robot.energy;
boolean enemyInFront = relativeBearing < 20 || relativeBearing > 340;
```

```kotlin
// Kotlin — el tipo se infiere automáticamente
private val wallStrategy = WallStrategy()
val currentEnergy = robot.energy
val enemyInFront = relativeBearing < 20 || relativeBearing > 340
```

En Kotlin, `val` (inmutable) y `var` (mutable) reemplazan la combinación `final` + tipo de Java, reduciendo la verbosidad sin perder claridad. Nótese que ya no es necesario declarar `: Strategy` explícitamente, el compilador lo infiere.

### Expresiones de control como expresiones (no sentencias)

En Java, `if`, `switch` y el operador ternario son sentencias independientes. En Kotlin, `if` y `when` son expresiones que devuelven valores directamente:

```java
// Java — operador ternario
int currentHeading = movingForward ? robot.heading : (robot.heading + 180) % 360;
```

```kotlin
// Kotlin — if como expresión (sin operador ternario, que no existe en Kotlin)
val currentHeading = if (movingForward) robot.heading else (robot.heading + 180) % 360
```

### `when` vs `switch`

El `when` de Kotlin es más versátil que el `switch` de Java. Puede usarse con comparaciones arbitrarias, no solo con constantes:

```java
// Java
if (heading >= 315 || heading < 45) {
    return robot.fieldHeight - robot.robotY - WALL_MARGIN;
} else if (heading >= 45 && heading < 135) {
    return robot.fieldWidth - robot.robotX - WALL_MARGIN;
} // ...
```

```kotlin
// Kotlin — when con rangos y condiciones arbitrarias
return when {
    heading >= 315 || heading < 45  -> robot.fieldHeight - robot.robotY - WALL_MARGIN
    heading >= 45  && heading < 135 -> robot.fieldWidth - robot.robotX - WALL_MARGIN
    // ...
}
```

### Eliminación de punto y coma y `new`

Kotlin elimina dos fuentes frecuentes de ruido visual: los punto y coma al final de cada línea y la palabra clave `new` para instanciar objetos.

```java
// Java
private final Strategy wallStrategy = new WallStrategy();
robot.fire(3);
```

```kotlin
// Kotlin
private val wallStrategy: Strategy = WallStrategy()
robot.fire(3.0)
```

---

## 3. Legibilidad y claridad

La legibilidad mide qué tan fácil es para un programador leer y entender el código sin necesidad de contexto adicional.

### Propiedades vs getters

En Java, acceder a un campo de un objeto externo requiere llamar al método getter. Kotlin traduce automáticamente los getters de Java a propiedades, lo cual se lee de forma más natural:

```java
// Java
Color.RED.getRGB()
```

```kotlin
// Kotlin
Color.RED.rgb
```

Esta diferencia, aunque menor individualmente, se acumula a lo largo del código y hace que el flujo de lectura sea más fluido. En `Terreneitor3000`, donde se llama a `getRGB()` cinco veces, la versión Kotlin es visualmente más limpia.

### Acceso directo al Singleton

En Java, el acceso a un Singleton requiere una llamada al método estático `getInstance()`, lo cual interrumpe el flujo de lectura. En Kotlin, se accede directamente al `object`:

```java
// Java
private final Strategist strategist = Strategist.MainStrategist.getInstance();
```

```kotlin
// Kotlin
private val strategist: Strategist = Strategist.MainStrategist
```

El código Kotlin lee como prosa: "el estratega es el MainStrategist". El de Java agrega una capa de indirección con `getInstance()`.

### `when` como tabla de decisión

El uso de `when` en `WallStrategy` para seleccionar el muro más cercano genera una estructura visual similar a una tabla de decisión, lo cual facilita la comprensión inmediata:

```kotlin
when (minDist) {
    distN -> moveToWall(robot, 0, distN)
    distS -> moveToWall(robot, 180, distS)
    distE -> moveToWall(robot, 90, distE)
    else  -> moveToWall(robot, 270, distW)
}
```

Comparado con la cadena de `if-else if-else` en Java, esta forma es más escaneable visualmente y deja claro que se trata de una decisión entre cuatro opciones mutuamente excluyentes.

---

## 4. Experiencia general de programación

### Reducción de líneas de código

| Archivo | Java (líneas) | Kotlin (líneas) | Reducción |
|---------|:---:|:---:|:---:|
| Strategy | 12 | 16 | +4 (más KDoc) |
| Terreneitor3000 | 43 | 47 | +4 (más KDoc) |
| Strategist | 55 | 29 | **-26** |
| WallStrategy | 190 | 179 | -11 |
| TurretStrategy | 32 | 27 | **-5** |
| SimpleRandomStrategy | 54 | 40 | **-14** |

La reducción total es de **52 líneas menos** que Java. El uso de `object` para singletons sin estado (`TurretStrategy`), expression body en `Strategist`, y clases simples con valores por defecto (`SimpleRandomStrategy`) contribuyen significativamente a esta reducción.

### Seguridad de tipos y null-safety

Kotlin posee null-safety integrado en el sistema de tipos. Si bien en este proyecto no se explota esta característica (los campos de `JuniorRobot` son tipos primitivos de Java), en un proyecto más grande, la capacidad de Kotlin de distinguir entre `Int` e `Int?` (nullable) en tiempo de compilación previene `NullPointerException` sin necesidad de anotaciones externas como `@Nullable` o `@NotNull`.

### Interoperabilidad con Java

La migración demuestra que Kotlin es 100% interoperable con Java: las clases Kotlin extienden `JuniorRobot` (una clase Java de Robocode) sin ningún adaptador ni wrapper. Los campos públicos de `JuniorRobot` (`energy`, `scannedAngle`, `heading`, etc.) se acceden directamente como propiedades de Kotlin.

### Facilidad de mantenimiento

La eliminación de patrones manuales (como el Singleton) y la reducción de boilerplate tiene un impacto directo en el mantenimiento: menos código que mantener implica menos oportunidades de introducir bugs. Por ejemplo, si se necesitara un nuevo estratega, en Java hay que escribir ~11 líneas de patrón Singleton; en Kotlin, solo se declara un nuevo `object`. Del mismo modo, una estrategia sin estado como `TurretStrategy` se modela como `object` en vez de una clase con constructor vacío, dejando clara la intención de diseño.

---

## Conclusión

Kotlin ofrece una mejora tangible en las cuatro dimensiones analizadas respecto a Java para este proyecto:

- **Expresividad**: `object` para singletons sin estado, `sealed interface` sin `permits`, constructor primario con valores por defecto.
- **Simplicidad sintáctica**: inferencia de tipos, sin `;`, sin `new`, `when` como expresión, expression body para funciones cortas.
- **Legibilidad**: propiedades en lugar de getters, acceso directo a singletons, `when` como tabla de decisión.
- **Experiencia general**: menos boilerplate (-52 líneas totales), null-safety integrado, interoperabilidad transparente con Java.

La migración conserva la misma lógica y estructura del código Java original, pero el resultado en Kotlin es más conciso, legible y mantenible.
