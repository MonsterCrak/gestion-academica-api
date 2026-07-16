package edu.upc.sistema.gestionacademicaapi.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import edu.upc.sistema.gestionacademicaapi.dto.ReporteDashboardResponse;
import edu.upc.sistema.gestionacademicaapi.entity.PrestamoIndividual;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoReserva;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSesionTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.repository.DemandaTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.PenalizacionRepository;
import edu.upc.sistema.gestionacademicaapi.repository.PrestamoIndividualRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RecursoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.ReservaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.SesionTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Panel de indicadores operativos y exportación a Excel/PDF (HU-21).
 */
@Service
@RequiredArgsConstructor
public class ReporteDashboardService {

    private final RecursoRepository recursoRepository;
    private final PrestamoIndividualRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PenalizacionRepository penalizacionRepository;
    private final ReservaRepository reservaRepository;
    private final SesionTutoriaRepository sesionRepository;
    private final DemandaTutoriaRepository demandaRepository;
    private final EspacioFisicoRepository espacioRepository;
    private final CurrentUserService currentUser;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public ReporteDashboardResponse dashboard() {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        return construir();
    }

    private ReporteDashboardResponse construir() {
        long totalEq = recursoRepository.count();
        long disp = recursoRepository.countByEstado(EstadoRecurso.DISPONIBLE);
        long prest = recursoRepository.countByEstado(EstadoRecurso.PRESTADO);
        long baja = recursoRepository.countByEstado(EstadoRecurso.DADO_DE_BAJA);
        long operables = totalEq - baja;
        double ocupacion = operables > 0 ? redondear(prest * 100.0 / operables) : 0;

        List<PrestamoIndividual> devueltos = prestamoRepository.findByEstado(EstadoPrestamo.DEVUELTO);
        long aTiempo = devueltos.stream()
                .filter(p -> p.getFechaDevolucion() != null && p.getFechaFin() != null
                        && !p.getFechaDevolucion().isAfter(p.getFechaFin()))
                .count();
        double devATiempo = !devueltos.isEmpty() ? redondear(aTiempo * 100.0 / devueltos.size()) : 100;

        List<PrestamoIndividual> activos = prestamoRepository.findByEstado(EstadoPrestamo.ACTIVO);
        LocalDateTime ahora = LocalDateTime.now();
        long vencidos = activos.stream()
                .filter(p -> p.getFechaFin() != null && p.getFechaFin().isBefore(ahora))
                .count();

        long totalUsr = usuarioRepository.count();
        long conDeuda = usuarioRepository.countByTieneDeudaTrue();
        double morosidad = totalUsr > 0 ? redondear(conDeuda * 100.0 / totalUsr) : 0;

        return ReporteDashboardResponse.builder()
                .totalEquipos(totalEq)
                .equiposDisponibles(disp)
                .equiposPrestados(prest)
                .ocupacionEquiposPct(ocupacion)
                .prestamosActivos(activos.size())
                .prestamosVencidos(vencidos)
                .devolucionesATiempoPct(devATiempo)
                .totalUsuarios(totalUsr)
                .usuariosConDeuda(conDeuda)
                .penalizacionesActivas(penalizacionRepository.countByActivaTrue())
                .tasaMorosidadPct(morosidad)
                .aulasActivas(espacioRepository.countByActivoTrue())
                .reservasAprobadas(reservaRepository.countByEstado(EstadoReserva.APROBADA))
                .sesionesTutoriaConfirmadas(sesionRepository.countByEstado(EstadoSesionTutoria.CONFIRMADA))
                .demandaTutoriasPendiente(demandaRepository.countBySesionIsNull())
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel() {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        ReporteDashboardResponse d = construir();
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Indicadores");
            CellStyle titulo = wb.createCellStyle();
            var fuente = wb.createFont();
            fuente.setBold(true);
            titulo.setFont(fuente);

            int r = 0;
            fila(hoja, r++, "Indicador", "Valor", titulo);
            for (String[] kv : filasIndicadores(d)) {
                fila(hoja, r++, kv[0], kv[1], null);
            }
            hoja.setColumnWidth(0, 12000);
            hoja.setColumnWidth(1, 6000);
            wb.write(out);
            auditoriaService.registrar(AuditoriaService.GENERA_REPORTE, "Reporte", null,
                    AuditoriaService.OK, "Exportacion Excel del panel operativo");
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf() {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        ReporteDashboardResponse d = construir();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font h = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(179, 20, 31));
            doc.add(new Paragraph("Panel de Administracion y Analitica Operativa", h));
            doc.add(new Paragraph("Sistema de Gestion Academica — UPC"));
            doc.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            celdaEncabezado(tabla, "Indicador");
            celdaEncabezado(tabla, "Valor");
            for (String[] kv : filasIndicadores(d)) {
                tabla.addCell(kv[0]);
                tabla.addCell(kv[1]);
            }
            doc.add(tabla);
            doc.close();
            auditoriaService.registrar(AuditoriaService.GENERA_REPORTE, "Reporte", null,
                    AuditoriaService.OK, "Exportacion PDF del panel operativo");
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    private List<String[]> filasIndicadores(ReporteDashboardResponse d) {
        return List.of(
                new String[]{"Total de equipos", String.valueOf(d.getTotalEquipos())},
                new String[]{"Equipos disponibles", String.valueOf(d.getEquiposDisponibles())},
                new String[]{"Equipos prestados", String.valueOf(d.getEquiposPrestados())},
                new String[]{"Ocupacion de equipos (%)", String.valueOf(d.getOcupacionEquiposPct())},
                new String[]{"Prestamos activos", String.valueOf(d.getPrestamosActivos())},
                new String[]{"Prestamos vencidos", String.valueOf(d.getPrestamosVencidos())},
                new String[]{"Devoluciones a tiempo (%)", String.valueOf(d.getDevolucionesATiempoPct())},
                new String[]{"Total de usuarios", String.valueOf(d.getTotalUsuarios())},
                new String[]{"Usuarios con deuda", String.valueOf(d.getUsuariosConDeuda())},
                new String[]{"Penalizaciones activas", String.valueOf(d.getPenalizacionesActivas())},
                new String[]{"Tasa de morosidad (%)", String.valueOf(d.getTasaMorosidadPct())},
                new String[]{"Aulas activas", String.valueOf(d.getAulasActivas())},
                new String[]{"Reservas aprobadas", String.valueOf(d.getReservasAprobadas())},
                new String[]{"Tutorias confirmadas", String.valueOf(d.getSesionesTutoriaConfirmadas())},
                new String[]{"Demanda de tutorias pendiente", String.valueOf(d.getDemandaTutoriasPendiente())});
    }

    private void fila(Sheet hoja, int idx, String k, String v, CellStyle estilo) {
        Row row = hoja.createRow(idx);
        Cell c0 = row.createCell(0);
        c0.setCellValue(k);
        Cell c1 = row.createCell(1);
        c1.setCellValue(v);
        if (estilo != null) {
            c0.setCellStyle(estilo);
            c1.setCellStyle(estilo);
        }
    }

    private void celdaEncabezado(PdfPTable tabla, String texto) {
        PdfPCell cell = new PdfPCell(new Paragraph(texto,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        cell.setBackgroundColor(new Color(179, 20, 31));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(6);
        tabla.addCell(cell);
    }

    private double redondear(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
