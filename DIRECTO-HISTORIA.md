# Mi Historia con MS-Event-Processor

## De donde nacio este proyecto

Todo comenzo con una pregunta simple: Como se ve realmente un microservicio event-driven en 2024? No el ejemplo simplificado de un tutorial, sino algo que podria estar en produccion.

Asi nacio **MS-Event-Processor**. Un proyecto que demuestra la arquitectura moderna de microservicios, pero con algo extra: un frontend real que lo hace tangible.

---

## Lo que vamos a mostrar hoy

### El problema que resuelve

Imagina una tienda online. Cada vez que alguien hace un pedido:
- El sistema necesita guardar la orden
- Enviar confirmaciones
- Actualizar inventario
- Notificar al equipo de envio

Todo esto no puede pasar de forma sincrona porque seria muy lento. La solucion? Eventos.

### La arquitectura en palabras simples

1. **Kafka** es el mensajero central. Recibe eventos como "nueva orden creada" o "orden cancelada"
2. **El backend** escucha esos eventos y decide que hacer con cada uno
3. **PostgreSQL** guarda toda la informacion de forma persistente
4. **SQS** es la cola de salida para mensajes procesados
5. **El frontend Angular** nos deja ver todo esto en accion

---

## Mi narrativa preferida para el directo

### Paso 1: Mostrar el problema (2 minutos)

"Estan viendo este frontend. Parece una lista simple de ordenes, pero atras hay algo muy interesante pasando."

- Abrir http://localhost:4200
- Mostrar la lista vacia (o con datos de prueba)
- "Lo interesante no es lo que ven, sino como llego hasta aqui"

### Paso 2: Crear una orden y ver que pasa (3 minutos)

"Hagamos click en 'Crear Orden Rapida' y vean lo que sucede..."

**Lo que esta pasando atras (contar mientras carga):**

1. El frontend hace POST al backend
2. El backend valida la orden
3. Guarda en PostgreSQL
4. Envia mensaje a SQS
5. Retorna la respuesta al frontend
6. **NUEVO**: Si activaron las notificaciones, reciben una notificacion push!

"En menos de un segundo, esto paso por 4 servicios diferentes."

### Paso 3: Mostrar el poder de Kafka (5 minutos)

"Pero esto es lo simple. Veamos lo interesante."

- Click en "Enviar a Kafka"
- "Acabo de enviar un mensaje a un topico de Kafka"
- "El consumer lo esta escuchando en este momento"
- "En 2-3 segundos, va a aparecer aqui automaticamente"

**Mientras esperamos, explicar:**

"Kafka me permite desacoplar todo. El frontend no necesita saber del inventario, ni del envio, ni de las notificaciones. Solo publica un evento y cada servicio hace lo que tiene que hacer."

### Paso 4: Mostrar el backend (3 minutos)

"Veamos que hay detras del curtain..."

- Abrir Swagger UI: http://localhost:8080/swagger-ui.html
- Mostrar los endpoints disponibles
- "Cada uno de estos endpoints replica la funcionalidad de los scripts de shell"
- "Pero ademas, tenemos consumers de Kafka ejecutandose en background"

### Paso 5: Notificaciones Push - El nuevo feature (3 minutos)

"Hoy agregue algo especial: Notificaciones push en el navegador"

- Click en "Activar Notificaciones"
- Aceptar el permiso del navegador
- Crear una nueva orden
- "Vieron? El navegador me notifico inmediatamente"

"Esto es lo que pasaria en produccion cuando un cliente hace un pedido. El equipo de operaciones podria recibir estas notificaciones en tiempo real."

### Paso 6: Ver los detalles (2 minutos)

- Click en una orden
- Mostrar la vista de detalle
- "Aqui vemos toda la informacion de la orden"
- "Y abajo, el audit trail - cada cosa que paso con esta orden"

---

## Los numeros que impresionan

Cuando quieras demostrar la robustez:

- **39+ tests** pasando
- **3 Kafka consumers** escuchando simultaneamente
- **5 servicios Docker** corriendo en paralelo
- **Java 21 Virtual Threads** manejando miles de conexiones
- **Idempotencia**: puedo enviar el mismo mensaje 10 veces y solo se procesa una

---

## Frases que uso para explicar conceptos

### Sobre Event-Driven
"En lugar de que A llame a B que llame a C, A publica un evento y B y C escuchan. Nadie sabe quien mas esta escuchando. Esto es desacoplamiento real."

### Sobre Virtual Threads
"Java 21 trajo los Virtual Threads. Antes, cada hilo consumia mucha memoria. Ahora puedo tener miles de hilos 'virtuales' que usan los recursos de forma eficiente. Es como tener un equipo pequeno que hace el trabajo de uno grande."

### Sobre Idempotencia
"Si el mismo mensaje llega dos veces, el sistema lo ignora elegantemente. Esto es critico en sistemas distribuidos donde los mensajes pueden duplicarse."

### Sobre Kafka
"Kafka es como un periodico. Publicas una noticia y cualquiera que este suscrito la recibe. La diferencia es que Kafka guarda todo ese historial."

### Sobre LocalStack
"No necesitan una cuenta de AWS para probar esto. LocalStack simula los servicios de AWS localmente. Es como tener AWS en tu laptop."

---

## Tips para el directo

### Antes de empezar
1. Ejecuta `docker compose up -d` 5 minutos antes
2. Verifica que todos los servicios esten "healthy"
3. Ten una terminal lista para mostrar logs

### Durante el directo
1. **No muestres codigo a menos que pregunten** - enfocate en la funcionalidad
2. **Usa analogias del mundo real** - hace que sea mas entendible
3. **Muestra los errores** - si algo falla, muestralo y explica como debuggear
4. **Repite los conceptos clave** - event-driven, idempotencia, desacoplamiento

### Si algo falla
1. Muestra los logs: `docker compose logs -f app`
2. Verifica health: `curl http://localhost:8080/actuator/health`
3. Reinicia el servicio: `docker compose restart app`
4. "En produccion, esto se resolveria automaticamente con health checks y restart policies"

---

## El mensaje final

"Este proyecto demuestra que construir microservicios modernos no tiene que ser complicado. Con las herramientas correctas y los patrones adecuados, puedes tener un sistema robusto, escalable y mantenible."

"Lo que ven aqui - Kafka, PostgreSQL, SQS, Virtual Threads, Angular - es el stack que muchas empresas usan en produccion hoy. Solo que aqui lo tienen funcionando en sus laptops con un solo comando: `docker compose up`"

---

## Preguntas frecuentes del publico

### "Por que Java 21?"
Por los Virtual Threads. Es un game-changer para aplicaciones concurrentes. Ademas, es LTS hasta 2031.

### "Por que Angular y no React?"
Angular da estructura. Para proyectos enterprise, tener opinion sobre como hacer las cosas reduce la friccion del equipo. Pero React funcionaria igual.

### "Puedo usar esto en produccion?"
Si, con algunas modificaciones:
- Configurar secrets correctamente
- Usar SQS real de AWS (no LocalStack)
- Agregar Kubernetes para orquestacion
- Implementar circuit breakers

### "Cuanto tardaste en construir esto?"
El backend: un fin de semana. El frontend: otro fin de semana. La integration: una tarde. Pero eso es porque ya conozco el stack. Si empezaras de cero, quizas 2-3 semanas.

---

## Despedida

"Gracias por ver este directo. El codigo esta en GitHub, la documentacion en DIRECTO-GUIA.md, y si tienen preguntas, pueden abrir un issue en el repo."

"Recuerden: la mejor forma de aprender es construyendo. Tomen este proyecto, rompanlo, agreguen features, y vean como se comporta. Esa es la verdadera ingenieria."
