-- =====================================================
-- Sembrado inicial para entorno Docker (desarrollo).
-- Se ejecuta automaticamente en /docker-entrypoint-initdb.d/
-- solo cuando el volumen postgres_data esta vacio.
--
-- Para re-seedear: docker compose down -v && docker compose up
-- =====================================================

-- ====== USUARIOS ======
INSERT INTO usuario (id, tipo_usuario, identificador_corporativo, nombre, apellidos, carrera_id, activo) VALUES
(1, 'ESTUDIANTE',     'u202010101', 'Ana',     'Perez Lopez',     1,    TRUE),
(2, 'ESTUDIANTE',     'u202010102', 'Bruno',   'Quispe Mamani',   1,    TRUE),
(3, 'ESTUDIANTE',     'u202010103', 'Carla',   'Rojas Vega',      1,    TRUE),
(4, 'ESTUDIANTE',     'u202010104', 'Diego',   'Salazar Nunez',   1,    TRUE),
(5, 'ESTUDIANTE',     'u202010105', 'Elena',   'Torres Flores',   1,    TRUE),
(6, 'DOCENTE',        'd201810101', 'Carlos',  'Mendoza Rivera',  NULL, TRUE),
(7, 'ADMINISTRATIVO', 'a201900001', 'Lucia',   'Vargas Soto',     NULL, TRUE);

-- ====== MATERIAS ======
INSERT INTO materia (id, codigo, nombre, departamento) VALUES
(1, 'MAT101', 'Calculo I',         'Matematicas'),
(2, 'FIS101', 'Fisica I',          'Fisica'),
(3, 'PRG101', 'Programacion I',    'Ing. Sistemas');

-- ====== ESPACIOS FISICOS ======
INSERT INTO espacio_fisico (id, codigo, tipo_espacio, aforo, permitir_prestamo_individual, permitir_reserva_completa) VALUES
(1, 'A-302',     'AULA_NORMAL',  30, TRUE,  TRUE),
(2, 'COMP-201',  'COMPUTO',      25, TRUE,  TRUE),
(3, 'LAB-FIS-1', 'LABORATORIO',  20, FALSE, TRUE);

-- ====== CATEGORIAS DE POLITICA ======
INSERT INTO categoria_politica (id, nombre_categoria, max_items_por_alumno, tiempo_maximo_horas) VALUES
(1, 'Laptops',     1, 24),
(2, 'Microscopios',2, 4);

-- ====== RECURSOS ======
INSERT INTO recurso (id, categoria_id, numero_serie, codigo_inventario, nombre, tipo_movilidad, espacio_actual_id, estado, requiere_ubicacion_fisica) VALUES
(1, 1, 'LAP-001', 'INV-LAP-0001', 'Laptop Dell 1', 'PORTATIL_ALMACEN', NULL, 'DISPONIBLE', FALSE),
(2, 2, 'MIC-001', 'INV-MIC-0001', 'Microscopio 1', 'FIJO_EN_AULA',     3,    'DISPONIBLE', TRUE);

-- ====== DOCENTE_MATERIA (preferencias) ======
INSERT INTO docente_materia (id, docente_id, materia_id, tipo_aula_requerida, modalidad_asignacion, fecha_alta, activo) VALUES
(1, 6, 1, 'AULA_NORMAL',  'AUTOMATICA', NOW(), TRUE),
(2, 6, 3, 'COMPUTO',      'AUTOMATICA', NOW(), TRUE),
(3, 6, 2, 'LABORATORIO',  'MANUAL',     NOW(), TRUE);